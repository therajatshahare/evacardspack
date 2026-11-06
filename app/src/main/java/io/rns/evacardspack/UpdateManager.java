package io.rns.evacardspack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/therajatshahare/evacardspack/releases/latest";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void checkForUpdates(Activity activity) {
        checkForUpdates(activity, false);
    }

    public void checkForUpdates(Activity activity, boolean isManualCheck) {
        if (isManualCheck) {
            Toast.makeText(activity, "Checking for updates...", Toast.LENGTH_SHORT).show();
        }

        executor.execute(() -> {
            // Force IPv4 socket resolution on Android TV stacks to prevent socket EPERM
            try {
                System.setProperty("java.net.preferIPv4Stack", "true");
                System.setProperty("java.net.preferIPv6Addresses", "false");
            } catch (Exception ignored) {
            }

            try {
                URL url = new URL(LATEST_RELEASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) conn).setInstanceFollowRedirects(true);
                }
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    String tagName = json.optString("tag_name", "");
                    String body = json.optString("body", "");
                    String currentVersion = BuildConfig.VERSION_NAME;

                    String latestVersion = tagName.replace("v", "").trim();
                    String currentClean = currentVersion.replace("v", "").trim();

                    if (isNewerVersionAvailable(latestVersion, currentClean)) {
                        String downloadUrl = "";
                        JSONArray assets = json.optJSONArray("assets");
                        if (assets != null && assets.length() > 0) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String assetName = asset.optString("name", "");
                                if (assetName.endsWith(".apk")) {
                                    downloadUrl = asset.optString("browser_download_url", "");
                                    break;
                                }
                            }
                        }

                        final String finalDownloadUrl = downloadUrl;
                        final String finalTagName = tagName;
                        final String finalBody = body;

                        if (!downloadUrl.isEmpty()) {
                            activity.runOnUiThread(() -> showUpdateDialog(activity, finalTagName, finalBody, finalDownloadUrl));
                        }
                    } else if (isManualCheck) {
                        activity.runOnUiThread(() ->
                            Toast.makeText(activity, "You are on the latest version (v" + currentVersion + ")", Toast.LENGTH_LONG).show()
                        );
                    }
                } else if (isManualCheck) {
                    activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Server returned " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                if (isManualCheck) {
                    activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Update check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private boolean isNewerVersionAvailable(String latest, String current) {
        if (latest == null || latest.isEmpty() || current == null || current.isEmpty()) return false;
        try {
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");
            int length = Math.max(latestParts.length, currentParts.length);

            for (int i = 0; i < length; i++) {
                int latestNum = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("\\D+", "")) : 0;
                int currentNum = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("\\D+", "")) : 0;
                if (latestNum > currentNum) return true;
                if (latestNum < currentNum) return false;
            }
        } catch (Exception e) {
            return !latest.equals(current);
        }
        return false;
    }

    private void showUpdateDialog(Activity activity, String version, String changelog, String downloadUrl) {
        if (activity == null || activity.isFinishing()) return;

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_available);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView versionText = dialog.findViewById(R.id.updateVersionText);
        TextView changelogText = dialog.findViewById(R.id.updateChangelogText);
        Button btnLater = dialog.findViewById(R.id.btnLater);
        Button btnUpdateNow = dialog.findViewById(R.id.btnUpdateNow);

        if (versionText != null) {
            versionText.setText("Version " + version + " is ready for install");
        }
        if (changelogText != null && changelog != null && !changelog.isEmpty()) {
            changelogText.setText(changelog);
        }

        if (btnLater != null) {
            btnLater.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnUpdateNow != null) {
            btnUpdateNow.setOnClickListener(v -> {
                dialog.dismiss();
                downloadAndInstallApk(activity, downloadUrl);
            });
            btnUpdateNow.requestFocus();
        }

        dialog.show();
    }

    private void downloadAndInstallApk(Activity activity, String downloadUrl) {
        Toast.makeText(activity, "Downloading update...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) conn).setInstanceFollowRedirects(true);
                }
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.connect();

                // Handle redirects if HTTP 301/302
                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String redirectUrl = conn.getHeaderField("Location");
                    url = new URL(redirectUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    if (conn instanceof HttpsURLConnection) {
                        ((HttpsURLConnection) conn).setInstanceFollowRedirects(true);
                    }
                    conn.setRequestMethod("GET");
                    conn.connect();
                }

                File cacheDir = activity.getExternalFilesDir(null);
                if (cacheDir == null) cacheDir = activity.getCacheDir();
                File apkFile = new File(cacheDir, "update.apk");

                InputStream inputStream = conn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(apkFile);

                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }

                outputStream.close();
                inputStream.close();

                if (apkFile.exists() && apkFile.length() > 1000) {
                    activity.runOnUiThread(() -> installApk(activity, apkFile));
                } else {
                    activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Downloaded APK is invalid or incomplete.", Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e) {
                activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void installApk(Context context, File apkFile) {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            Toast.makeText(context, "APK file is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(context, "Enable 'Install Unknown Apps' permission for Eva Cards Pack", Toast.LENGTH_LONG).show();
                try {
                    Intent manageIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    manageIntent.setData(Uri.parse("package:" + context.getPackageName()));
                    manageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(manageIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Please allow unknown app installation in TV Settings", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot launch installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
