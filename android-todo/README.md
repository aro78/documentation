# Aufgaben - persistente To-Do-Alarme für Android

Eine kleine Android-App im Stil der alten Palm-Pilot-Erinnerungen: Aufgaben mit
Termin, deren Alarm sich nicht einfach wegwischen lässt. Der Alarm taucht so
lange immer wieder auf, bis er entweder **mit Erledigt quittiert** oder mit
einer von sieben festen Stufen **geschlummert** wird:

| Stufe | Dauer |
|------:|:------|
| 1     | 1 Min |
| 2     | 3 Min |
| 3     | 10 Min |
| 4     | 30 Min |
| 5     | 1 Std |
| 6     | 3 Std |
| 7     | 1 Tag |

## Wie die Persistenz funktioniert

Beim Fälligwerden plant der `AlarmReceiver` **automatisch den nächsten
Re-Alert** in einem konfigurierbaren Intervall (Default 3 Minuten) ein.
Wischt der Nutzer die Notification weg, kommt sie dadurch wieder. Nur das
explizite "Erledigt" (über `AcknowledgeReceiver`) bricht diese Kette ab.

Verwendet wird `AlarmManager.setAlarmClock` - die einzige AlarmManager-API,
die Doze und App-Standby auf modernen Android-Versionen umgeht.

## Tech-Stack

- Kotlin + Jetpack Compose (Material 3)
- Room (KSP)
- AlarmManager (`setAlarmClock`) + BroadcastReceiver
- Navigation Compose
- compileSdk 35, minSdk 26 (Android 8.0)

## Bauen & Installieren

```bash
cd android-todo
./gradlew assembleDebug
./gradlew installDebug      # bei angeschlossenem Gerät oder laufendem Emulator
```

Logs anschauen:

```bash
adb logcat -s AlarmReceiver:V SnoozeReceiver:V AcknowledgeReceiver:V BootReceiver:V
```

## Berechtigungen

Beim ersten Start zeigt die App den Berechtigungs-Screen:

1. **Benachrichtigungen** (`POST_NOTIFICATIONS`, ab Android 13)
2. **Exakte Wecker** (`SCHEDULE_EXACT_ALARM`, ab Android 12) - Settings-Deeplink
3. **Vollbild-Alarm** (`USE_FULL_SCREEN_INTENT`, ab Android 14) - Settings-Deeplink
4. **Akku-Optimierung deaktivieren** - bei OEM-ROMs mit aggressivem Battery-Saving
   (Xiaomi MIUI, Huawei EMUI, Samsung, OnePlus etc.) zusätzlich in den
   Hersteller-Einstellungen die App auf Autostart/Whitelist setzen.

Die App deklariert zusätzlich `USE_EXACT_ALARM` (Android 13+). Diese
Berechtigung ist für Alarm/Reminder-Apps laut Play-Policy zulässig und
ist nicht vom Nutzer abschaltbar.

## Manueller Test

1. App starten, Berechtigungen erteilen
2. „+" tippen, Aufgabe „Test" anlegen, Termin in 2 Minuten, speichern
3. Gerät sperren, warten
4. **Erwartet**: Bildschirm wacht auf, Vollbild-Alarm über Lockscreen mit
   sieben Schlummer-Buttons und einem „Erledigt"-Button
5. Notification wegwischen → nach 3 Min (Default) kommt sie wieder
6. „Schlummern 1 Min" → nach 1 Min feuert der Alarm erneut
7. „Erledigt" → Alarm hört auf, Aufgabe wandert in „Erledigt"-Sektion

### Reboot-Test

1. Aufgabe 5 Min in der Zukunft anlegen
2. Gerät komplett neu starten, App nicht öffnen
3. **Erwartet**: Alarm feuert trotzdem (`BootReceiver` plant alle offenen
   Aufgaben neu)

## Projektstruktur

```
android-todo/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/de/example/todoalarm/
│       │   ├── TodoApp.kt
│       │   ├── MainActivity.kt
│       │   ├── data/         # Task + Room
│       │   ├── alarm/        # Scheduler + Receiver-Kette
│       │   ├── notification/ # Channels + Notification-Builder
│       │   ├── ui/           # AlarmActivity, Screens, Theme, Nav
│       │   ├── viewmodel/    # TaskList + TaskEdit
│       │   └── util/         # SnoozeDurations, TimeFormat
│       └── res/
├── gradle/                   # Wrapper + Version Catalog
├── build.gradle.kts
└── settings.gradle.kts
```

## Bekannte Einschränkungen

- **OEM-Killer**: Manche Hersteller-ROMs stoppen Apps trotz `setAlarmClock`.
  Whitelisting durch den Nutzer ist nötig.
- **Force-Stop**: Wird die App vom Nutzer aktiv gestoppt, liefert das System
  keine Broadcasts mehr - auch keine `BOOT_COMPLETED`. Lösung: nach Reboot
  oder Force-Stop die App einmal kurz öffnen.
- **`USE_FULL_SCREEN_INTENT`** kann ab Android 14 vom Nutzer entzogen werden.
  Fallback: Heads-Up-Notification; die Re-Alert-Kette läuft weiter.
