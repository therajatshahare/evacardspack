package io.rns.evacardspack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/therajatshahare/evacardspack/releases/latest";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void checkForUpdates(Activity activity) {
        executor.execute(() -> {
            try {
                URL url = new URL(LATEST_RELEASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "EvaCardsPack");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
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
                    }
                }
            } catch (Exception ignored) {
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
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

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

                activity.runOnUiThread(() -> installApk(activity, apkFile));
            } catch (Exception e) {
                activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot launch installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
