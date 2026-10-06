# TiPillTabs 1.0.0 validation

Validated October 5, 2026 with Titanium SDK 13.4.1.GA, Xcode 27.1 beta, and an iPhone 17 Pro simulator running iOS 26.5. Minimum deployment target: iOS 17.0.

## Build and package checks

- Titanium module compilation and packaging passed.
- XCFramework metadata confirms device arm64 and simulator arm64/x86_64 slices.
- Distribution ZIP integrity, embedded manifest identity/version, and MIT license checked.
- Classic and smoke-test JavaScript syntax checks passed.
- Alloy companion example compiled successfully for iOS.
- An isolated Classic Titanium consumer was cleaned, rebuilt with the app-local 1.0.0 package, and launched in the simulator.

## Simulator checks

- The actual JavaScript require/createView path and native section header rendered successfully.
- All 18 live bridge checks passed. See [results](https://github.com/designbymind/TiPillTabs/blob/main/tests/bridge-results.json) and [test app](https://github.com/designbymind/TiPillTabs/blob/main/tests/smoke-app.js).
- Selection setters/getters, event payloads, duplicate/invalid selection suppression, and item/color updates passed.
- Attention-dot checks cover initial state, showBadge/hideBadge/setBadge, tint override/reset, unknown IDs, selection/event preservation, and state surviving items reassignment.
- A TableView accessibility Scroll Down action advanced the visible rows while the pill header remained pinned. Selection and All Mail toggling worked from the pinned header.
- Simulator logs recorded tap selections and swipe toggles between a category and All Mail.
- [Badge preview](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/badge-dots.png) shows inactive-tint dots at the cart/message icons' upper-right corners with pill-colored outlines. [Earlier header preview](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/sticky-header.png) uses orange inactive tints applied by the live color-update test.

## Remaining interaction coverage

- Physical-device spring/fade fidelity, rapid repeated taps, and horizontal gesture cancellation.
- Vertical touch dragging that begins over the pills and nested scroll gesture arbitration. The pinning check used accessibility scrolling.
- Rotation, close/reopen during animation, iPad, right-to-left layouts, extreme Dynamic Type, VoiceOver navigation, and Reduce Motion.
- Empty/single-item arrays, very narrow long-title layouts, and invalid/duplicate item updates have source guards but were not exhaustively exercised in the simulator.

Compilation and simulator checks do not establish physical-device interaction behavior.

## Repeat the bridge checks

Create a disposable Classic iOS app, declare ti.pilltabs 1.0.0 in tiapp.xml, and extract the ZIP app-locally. Copy tests/smoke-app.js to Resources/app.js and example/items.js to Resources/items.js. The test writes pilltabs-results.json to the application's data directory and logs PILLTEST checks. Use example/app.js for the standard interactive demo.

## Android 1.0.0

Validated October 6, 2026 with Titanium SDK 13.4.1.GA on a Pixel 11 Pro with Android 17. Minimum Android version: 7.0 (API level 24). The device checks ran on the physical device, in the default Titanium theme.

These checks passed:

- Titanium module compilation and packaging for arm64-v8a, armeabi-v7a, x86, and x86_64.
- All 18 bridge checks of the smoke test. See [results](https://github.com/designbymind/TiPillTabs/blob/main/tests/bridge-results-android.json) and [test app](https://github.com/designbymind/TiPillTabs/blob/main/tests/smoke-app.js).
- The Classic example and the Alloy example, with the tabs in a fixed view above the TableView.
- A tap fired one `change` event with the reason `tap`. A tap on the selected chip fired no event.
- The chip row scrolled horizontally. A selected chip outside the visible area scrolled into view, at creation and after a later selection.
- Icons from a PNG path and from a drawable resource ID. A missing image left a chip with its title only.
- Icons from the Material Icons font, with `iconFamily` on the view and on an item, with a code point and with a ligature name.
- A missing font logged a warning and the chip used `image`. A change of `iconFamily` on an existing view drew the icons again.
- Theme default colors in the light and the dark theme, and the black or white default tint on a custom background.
- A `spacing` change on a visible view.
- Items assigned after creation, invalid arrays, removal of the selected item, and an empty array.
- Items and selection stayed correct when Titanium created the activity and the native view again.

The module then got the title animation. These checks ran with that version and passed:

- All 18 bridge checks of the smoke test, and the Classic example. See the [screenshot](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/android-example.png).
- The selected chip showed its title. The other chips showed only their icon, as a circle with the height of the view.
- A chip without an icon showed its title in each selection state.
- A selection change animated the widths of the chips, after a tap and after a programmatic selection.
- A screen recording showed about 290 ms for the default duration and about 1400 ms for `animationDuration: 1500`.
- `animated: false` and `animationDuration: 0` changed the chips in one frame.
- A second tap during the animation started the next animation from the current widths.
- In a view that is narrower than the chips, the selected chip scrolled into view during the animation.
- Each chip had its title as its content description in the view hierarchy.

The Alloy example and the other checks in the first list ran only with the version before the title animation.

The icon font checks ran while the phone was locked. They used an image that the app drew of its own window, not a screen capture.

The Android results hold three events and the iOS results hold four. Android delivers the last `change` event after the test writes its report.

These checks did not run:

- A change of the system appearance while the app runs. The view has code for this case.
- The system configuration that removes animations.
- A Material 3 theme, right-to-left layouts, TalkBack, tablets, rotation, and a large font scale.
- Android versions before 17.

Repeat the bridge checks in the same way as on iOS, with a disposable Classic Android app and the Android ZIP.
