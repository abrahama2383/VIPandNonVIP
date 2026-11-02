# VIP and Non-VIP Bank Demo

Two demo apps (iOS SwiftUI, Android Jetpack Compose) with the same flow: Login -> static Home page.

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
