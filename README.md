LobbyApp - Android TV Kiosk Mode App

Overview
LobbyApp is an Android TV application designed to function in kiosk mode, providing an uninterrupted, fullscreen browsing experience for a specific URL. The app is built to auto-start on device boot, continuously display a web page, and prevent users from closing or minimizing it, ensuring the app remains the primary focus.

Features
- Auto-Start on Boot: The app automatically launches when the device is powered on or rebooted.
- Fullscreen WebView: Displays a specific URL in fullscreen mode, without any distractions or user interface elements.
- Page Refresh:** Automatically refreshes the page every hour to ensure the displayed content is up to date.
- Disable System Navigation: Blocks back buttons, home buttons, and any other system buttons to prevent users from exiting or minimizing the app.
- Foreground Service Monitoring: A service that constantly monitors the app, bringing it back to the foreground if minimized or switched away.
- Battery Optimization Disablement: Automatically disables battery optimization to ensure the app continues to run smoothly without interruptions.
- Overlay Permission Check: Requests permission to draw overlays for additional control over how the app appears on the screen.
- Usage Stats Permission: Monitors app usage to ensure that LobbyApp stays in the foreground.

Setup and Installation
1. Permissions: The app requests permissions for internet access, foreground service, usage stats, overlay, and boot completion to ensure uninterrupted operation.
2. Kotlin & Android Studio: Built with Kotlin, you can easily clone the repository and open it with Android Studio for customization or further development.

Technical Details
Target Platform: Android 11 (API Level 30) and above, optimized for Android TV.
- Language & Framework: Developed using Kotlin and Android SDK.
- WebView Configuration: The app uses a WebView component with JavaScript enabled and caching set to default for optimal performance.
- Service and Monitoring: A foreground service is used to monitor app activity, checking every 5 seconds to ensure the app stays active.
