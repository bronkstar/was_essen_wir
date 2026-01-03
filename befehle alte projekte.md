# Befehle alte Projekte

Diese Befehle haben wir genutzt, um ein Android-Handy in WSL2 anzubinden
und die App im Debug-Modus zu starten.

  GRADLE_USER_HOME=/home/bjoern/was_essen_wir/.gradle \
  JAVA_HOME=/home/bjoern/android-studio/jbr \
  PATH=/home/bjoern/android-studio/jbr/bin:$PATH \
  ./gradlew assembleDebug


 /home/bjoern/Android/Sdk/platform-tools/adb -s RFCX10R039P install -r app/build/outputs/apk/debug/app-debug.apk

   /home/bjoern/Android/Sdk/platform-tools/adb -s RFCX10R039P shell am start -n com.wasessenwir.app/.MainActivity




## PowerShell (Windows)

USB-Devices anzeigen:
```powershell
& "C:\Program Files\usbipd-win\usbipd.exe" list
```

Geraet freigeben (BUSID aus der Liste nehmen):
```powershell
& "C:\Program Files\usbipd-win\usbipd.exe" bind --busid 1-6
```

Geraet an WSL anbinden:
```powershell
& "C:\Program Files\usbipd-win\usbipd.exe" attach --wsl --busid 1-6
```

Optional trennen:
```powershell
& "C:\Program Files\usbipd-win\usbipd.exe" detach --busid 1-6
```

Android Studio starten (WSL/Linux):
```bash
/home/bjoern/android-studio/bin/studio.sh
```

## WSL (Linux)

ADB Devices anzeigen:
```bash
/home/bjoern/Android/Sdk/platform-tools/adb devices
```

App starten (Beispiel mit Paketname):
```bash
/home/bjoern/Android/Sdk/platform-tools/adb shell am start -n com.example.dont_do_nothing/.MainActivity
```

Flutter Devices anzeigen:
```bash
/home/bjoern/flutter/bin/flutter devices
```

Flutter App starten (Device-ID aus adb devices):
```bash
/home/bjoern/flutter/bin/flutter run -d RFCX10R039P
```

Alternative per Monkey (mit Count):
```bash
/home/bjoern/Android/Sdk/platform-tools/adb shell monkey -p com.example.dont_do_nothing -c android.intent.category.LAUNCHER 1
```

## Hinweise
- Wenn "no permissions" kommt: udev/plugdev oder ADB-Rechte pruefen.
- Wenn Geraet in WSL nicht auftaucht: USBIPD erneut attachen.
