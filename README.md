# VIP and Non-VIP Bank Demo

Two demo apps (iOS SwiftUI, Android Jetpack Compose) with the same flow: Login -> static Home page.

Home has a **Services** list with 5 static pages: Transfer, Pay Bills, Top Up, Transaction History, Profile. Transfer, Pay Bills and Top Up have a button that only shows a success message; nothing is sent.

## Versions
| Version | Branch / tag | What it adds |
|---|---|---|
| base | `main`, tag `base` | Plain apps, no monitoring |
| v1 | branch `v1`, tag `v1` | Base Dynatrace mobile instrumentation (auto-start, user opt-in, startup load balancing; Android also session replay), user tag = username, and the VIP/Regular category as a user property. |
| v2 | branch `v2`, tag `v2.0` | Only VIP users are monitored. The agent starts manually on a VIP login; Regular users never start it, so they create no sessions and use no license. |
| v3 | branch `v3`, tag `v3.0` | Same as v2, but Firebase Remote Config decides which categories are monitored, so you can change it without releasing a new app. |

### v1 Dynatrace setup
- Application ID `0723d7db-6587-4356-ba64-a3082d0452db`, beacon `https://bf97052cfu.bf.dynatrace.com/mbeacon`.
- Android: Dynatrace Gradle plugin `8.+` in `android/build.gradle.kts`; privacy opt-in in `MainActivity.onCreate`.
- iOS: Swift package `swift-mobile-sdk` (product `Dynatrace`); DTX keys in `ios/Info.plist`; privacy opt-in in `BankDemoApp.init`.
- Uses classic (gen2) RUM only.
- On login the app tags the session with the username (`identifyUser`) and reports the category (`VIP` or `Regular`) as value `category` on a custom action `Login`.
- To see it as a user session property, add it in the mobile app settings under **Session and user action properties**: expression type *SDK reported value*, name and key `category`, type String, store as session property. Only sessions after that change get the property.
- Logout ends the Dynatrace session (`endVisit`), so the next login starts a new session with its own tag and category.
- The privacy opt-in is applied automatically for the demo. A real app must ask the user first.
- For full session replay (not only on crashes), set session replay traffic to 100% in the mobile app settings in Dynatrace.

### v2: monitor VIP users only
Goal: cut license cost by not monitoring Regular users. Builds on v1 (same tag, category property and session replay).
- Auto-start is off: `autoStart.enabled(false)` in `android/build.gradle.kts`, `DTXAutoStart = false` in `ios/Info.plist`.
- The `Monitoring` helper (`MainActivity.kt`, `BankDemoApp.swift`) decides on login:
  - **VIP**: start the agent (first time only), opt in to `USER_BEHAVIOR`, then tag the session and report `category` as in v1.
  - **Regular**: do nothing. The agent is never started, so nothing is sent.
- Logout calls `Dynatrace.shutdown()`. This ends the session and stops the agent, so a Regular user who logs in next sends nothing. The next VIP login starts the agent again.
- Don't switch to data collection `OFF` to "pause" monitoring. An `OFF` agent still sends a "Loading <app>" event, and Dynatrace shows each one as an anonymous session.
- The first VIP login on a device sends one such anonymous session, because the agent starts at `OFF` (user opt-in mode) before the app opts in. The opt-in is saved, so later VIP logins start straight at `USER_BEHAVIOR`.
- iOS applies the opt-in asynchronously, so the app sets the user tag and the Login action only after the opt-in lands. Otherwise the first VIP login on a device sends an anonymous session with no tag or category.
- `shutdown()` is deprecated on iOS (since 8.303, no replacement) but still works.
- On Android the agent starts from the activity (`Dynatrace.startup(activity, ...)`), so it misses the app start before login. Same on iOS.

### v3: choose monitored categories with Firebase Remote Config
Builds on v2: no auto-start, manual start at login, shutdown on logout.
- Remote Config parameter `monitored_categories` (String), a comma-separated list:
  - `VIP`: only VIP users, same as v2. This is also the built-in default, used offline or before the first fetch.
  - `VIP,Regular`: everyone.
  - empty: nobody.
- The app fetches on every launch (minimum fetch interval 0, demo setting) and listens for real-time updates, so a value published in the console applies from the next login. Users already logged in keep their current state until they log out.
- Remote Config conditions (platform, app version, country, random percentage) also work, for example VIP plus 10% of Regular users.
- Remote Config is free on the Firebase Spark plan. Google Analytics is not needed.

Setup (the Firebase files are per project and not in git):
1. Create a Firebase project and register Android app `com.demo.bankdemo` and iOS app `com.demo.BankDemo`.
2. Put `google-services.json` in `android/app/` and `GoogleService-Info.plist` in `ios/BankDemo/`.
3. In Remote Config, add `monitored_categories` (String, default `VIP`) and publish.

## Login rule
| Username | Password | Category |
|---|---|---|
| `userN` (N = integer, e.g. `user1`) | same as username | Regular |
| `vipN` (e.g. `vip7`) | same as username | VIP |

Anything else shows "Invalid username or password". Home shows "Welcome, <username>" and the category badge (gold for VIP, blue for Regular).

## iOS
Open `ios/BankDemo.xcodeproj` in Xcode and run, or:
```
cd ios && xcodebuild -project BankDemo.xcodeproj -scheme BankDemo -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build CODE_SIGNING_ALLOWED=NO build
```
Login logic: `ios/BankDemo/Auth.swift`.

## Android
Open `android/` in Android Studio and run, or with an Android SDK installed:
```
cd android && JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug testDebugUnitTest
```
Use JDK 17. Android Studio's bundled Java 25 is too new for Gradle 8.11 (the build fails with "25.0.4").
Login logic: `android/app/src/main/java/com/demo/bankdemo/Auth.kt` (unit test in `app/src/test`).
