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
