# Board Game Helper

To use the real BoardGameGeek API, add your own token to `local.properties` as `BGG_TOKEN=your_token` (the file is gitignored, so it never gets committed).

If `BGG_TOKEN` is blank or missing, the app does not call BoardGameGeek at all and loads the hand written sample responses in `app/src/main/assets` instead.

## Checking the game night timer and the battery banner

These need a device or emulator on API 33 or higher with the app installed. Keep Home on screen while you run the battery commands, because the receiver only listens while Home is visible.

### Battery banner

```
adb shell dumpsys battery unplug
adb shell dumpsys battery set level 10
adb shell dumpsys battery set level 80
adb shell dumpsys battery reset
```

1. `unplug` makes the phone think it is on battery. A low battery warning is never sent while charging.
2. `set level 10` crosses the low line (15 or 20 percent, depending on the device). The "Low battery, background refresh will wait" banner appears at the top of Home.
3. `set level 80` crosses back over the okay line, and the banner goes away.
4. `reset` hands the battery back to the real hardware.

The broadcasts are not sticky. If you leave Home, change the level and come back, the banner will not have changed, because nothing was listening when the level crossed the line.

### Notification permission

```
adb shell pm revoke com.mathewhaug.boardgamehelper android.permission.POST_NOTIFICATIONS
adb shell pm grant com.mathewhaug.boardgamehelper android.permission.POST_NOTIFICATIONS
adb shell pm clear-permission-flags com.mathewhaug.boardgamehelper android.permission.POST_NOTIFICATIONS user-set user-fixed
```

* **Granted:** tap Start timer. The time counts up as mm:ss on Home, and a "Game night timer" notification with a live chronometer and a Stop button shows in the shade. Stop in the notification and Stop timer on Home both end it.
* **Revoked:** Android kills the app's process when a permission is revoked, so any running timer ends. Open the app again and tap Start timer. The timer still runs and counts on Home. No notification shows in the shade, and Home shows "Timer is running, but its notification is hidden". The service is still in the foreground and is listed in the shade's active apps (Task Manager).
* **Denied twice:** after revoking, run the `clear-permission-flags` line so the app may ask again. Tap Start timer and deny. Stop the timer, then tap Start timer and deny again. The timer starts after each answer. After the second no, the "Allow notifications in Settings" button appears, and tapping Start again never shows the dialog. The button opens the app's page in Settings.
* **Granted again:** run the `grant` line, or switch notifications on from that Settings page and come back. The hidden line and the Settings button go away once Home resumes. If a timer was already running, its notification may not come back on its own, so stop it and start it again to see the notification.
