# VIP and Non-VIP Bank Demo

Two demo apps (iOS SwiftUI, Android Jetpack Compose) with the same flow: Login -> static Home page.

## Versions
| Version | Branch / tag | What it adds |
|---|---|---|
| base | `main`, tag `base` | Plain apps, no monitoring |
| v1 | branch `v1`, tag `v1` | Base Dynatrace mobile instrumentation (auto-start, user opt-in, startup load balancing). Android also has session replay. |

### v1 Dynatrace setup
- Application ID `0723d7db-6587-4356-ba64-a3082d0452db`, beacon `https://bf97052cfu.bf.dynatrace.com/mbeacon`.
- Android: Dynatrace Gradle plugin `8.+` in `android/build.gradle.kts`; privacy opt-in in `MainActivity.onCreate`.
- iOS: Swift package `swift-mobile-sdk` (product `Dynatrace`); DTX keys in `ios/Info.plist`; privacy opt-in in `BankDemoApp.init`.
- The privacy opt-in is applied automatically for the demo. A real app must ask the user first.
- To see sessions in Grail (`fetch user.sessions`) and full session replay, turn on the new RUM experience (gen3) and set session replay traffic to 100% in the mobile app settings in Dynatrace.

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
