# TermoHome Android Things and Client APP #

Proyecto para mantener la temperatura de la casa en unos rangos

### What is this repository for? ###

* Quick summary
* Version

Obtener historico:
https://enhanced-layout-677.firebaseio.com/historic.json

### Instalación Módulo Android Things ###

* adb uninstall es.atrujillo.iot.android
* adb push termohome-iot/release/termohome-iot-release.apk /data/local/tmp/es.atrujillo.iot.android
* adb shell pm install -g -t -r "/data/local/tmp/es.atrujillo.iot.android"
* Reiniciar dispositivo