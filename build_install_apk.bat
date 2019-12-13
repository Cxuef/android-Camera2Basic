@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

gradlew.bat clean

gradlew.bat assembleDebug

echo "Please wait 5s to install apk ..."
timeout 5

adb install -r Application/build/outputs/apk/debug/Camera2Photo-debug_1.0.00.01.apk