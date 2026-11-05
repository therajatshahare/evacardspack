# Eva Cards Pack (`EvaCardsPack`)

A standalone Module Card Addon Pack for **Eva Launcher** on Android TV.
It provides custom TV-optimized cards/widgets that display on the Eva Launcher Home screen.

---

## 🚀 Features & Included Cards

This pack includes 3 pre-built Module Cards:

| Card Name | Activity | Spec XML | Accent Color | Caption Example |
| :--- | :--- | :--- | :--- | :--- |
| **🌤️ Weather Live** | `WeatherCardActivity` | `card_weather_spec.xml` | `#FF3B82F6` (Sky Blue) | `26°C Sunny · Nagpur` |
| **⏰ Minimal Clock** | `ClockCardActivity` | `card_clock_spec.xml` | `#FF4ADE80` (Emerald) | `07:30 PM · Sunday` |
| **🎬 Trending Movies** | `TrendingMoviesCardActivity` | `card_trending_spec.xml` | `#FFEAB308` (Gold Accent) | `Top Pick: Interstellar` |

---

## 🛠️ Eva Launcher Technical Integration Standard

Eva Launcher detects cards by querying installed activities that declare the intent category `io.rns.evalauncher.category.DOCK_CARD` and specify card meta-data `io.rns.evalauncher.card`.

### 1. Card Specification XML (`res/xml/card_<name>_spec.xml`)

Every card specifies its properties in an XML resource file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<card xmlns:app="http://schemas.android.com/apk/res-auto"
    app:spec="1"
    app:label="Weather Live"
    app:caption="26°C Sunny · Nagpur"
    app:accent="#FF3B82F6" />
```

- `app:spec`: Protocol version (`"1"`).
- `app:label`: Short title of the card (max 24 chars).
- `app:caption`: Sub-caption or live status line (max 32 chars).
- `app:accent`: Hex color for card accent glow.

### 2. Activity Manifest Declaration (`AndroidManifest.xml`)

Activities are exported and registered with the intent category and metadata:

```xml
<activity
    android:name=".WeatherCardActivity"
    android:exported="true"
    android:icon="@drawable/ic_weather_banner"
    android:label="Weather Live">
    
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="io.rns.evalauncher.category.DOCK_CARD" />
    </intent-filter>
    
    <meta-data
        android:name="io.rns.evalauncher.card"
        android:resource="@xml/card_weather_spec" />
</activity>
```

---

## 📦 Building the Project

### Prerequisites
- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 37 (Min SDK 26)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/therajatshahare/evacardspack.git
cd evacardspack

# Build Debug APK
./gradlew assembleDebug
```

---

## 📄 License
Licensed under the [MIT License](LICENSE).
