# WeatherApp

Android app (Java) that shows the current weather from the OpenWeatherMap API for your GPS location or for one of a few preset Polish cities (Zakopane, Wrocław, Sosnowiec, Warszawa, Grybów).

- `MainActivity`: city spinner, shows city, temperature, pressure, humidity, coordinates and a weather icon
- `LocationService`: bound service that sends GPS updates to the activity
- `GsonRequest`, `VolleySingelton`: Volley request queue that parses JSON with Gson into `Weather`

## Build & run
Requires Android Studio / Android SDK 27, device or emulator with API 26+.

1. Set your own OpenWeatherMap API key in the `ApiKey` string in `app/src/main/res/values/strings.xml`.
2. `./gradlew installDebug`, then grant the location permission on first launch.
