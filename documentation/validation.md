# TiPillTabs validation

## 1.1.0 reissue — October 9, 2026

Built with Titanium SDK 13.4.1.GA and Xcode 27.1 beta. Minimum deployment target: iOS 17.0. Device arm64 and simulator arm64/x86_64 compilation and package integrity checks passed.

- A clean, same-version isolated consumer rebuilt and ran on iPhone 17 Pro / iOS 26.5 simulator.
- All 24 live bridge checks passed; see [conditional-padding results](https://github.com/designbymind/TiPillTabs/blob/main/tests/conditional-padding-results.json). Coverage includes padding initialization/live updates, normalization, unchanged outer width, badges, and selection/events.
- Native captures verified a 24-point right gap with the final item selected and no inset in category mode. See [trailing selection](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/conditional-padding-all.png) and [category selection](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/conditional-padding-category.png).
- Light/dark translucent-background captures verified separator pixels matching the actual pill fill. Hide/show restoration preserves selection. See [separator results](https://github.com/designbymind/TiPillTabs/blob/main/tests/badge-cutout-results.json) and [appearance test app](https://github.com/designbymind/TiPillTabs/blob/main/tests/badge-appearance-app.js).
- Round-dot captures at 3x display scale showed equal thresholded blue-circle bounds (17 x 17 pixels) and matching separator/background colors. See [round-dot results](https://github.com/designbymind/TiPillTabs/blob/main/tests/round-badge-results.json), [light preview](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/round-badge-light.png), and [dark preview](https://github.com/designbymind/TiPillTabs/blob/main/screenshots/round-badge-dark.png).
- Classic/smoke-test syntax and the Alloy example's iOS compilation passed. The dedicated simulator's original light appearance was restored after appearance checks.

The 1.1.0 version is intentionally retained for this reissue. No global installation or consuming-app integration was performed by these module checks. Clean the consuming app after installing the replacement same-version binary.

## Remaining interaction coverage

- Physical-device appearance and animation feel, including other display scales, rapid taps, and gesture transitions.
- Vertical touch dragging that begins over the pills and nested scroll gesture arbitration. The sticky-header check used accessibility scrolling.
- Rotation, close/reopen during animation, iPad, right-to-left layouts, extreme Dynamic Type, VoiceOver navigation, and Reduce Motion.
- Empty/single-item arrays, very narrow long-title layouts, and invalid/duplicate item updates have source guards but were not exhaustively exercised in the simulator.

Compilation and simulator checks do not establish physical-device interaction behavior.

## 1.0.0 baseline

The original module passed device/simulator builds, Classic syntax checks, Alloy compilation, and 18 live bridge checks. Simulator interaction covered section-header pinning, tap selection, aggregate toggling, and recorded swipes. Historical results are retained in [bridge results](https://github.com/designbymind/TiPillTabs/blob/main/tests/bridge-results.json) and [initial padding results](https://github.com/designbymind/TiPillTabs/blob/main/tests/padding-results.json).

## Repeat the checks

Create a disposable Classic iOS app, declare ti.pilltabs 1.1.0 in tiapp.xml, and extract the ZIP app-locally. Copy tests/smoke-app.js to Resources/app.js and example/items.js to Resources/items.js. It writes pilltabs-results.json and conditional-padding native captures to the application's data directory. For the appearance checks, use tests/badge-appearance-app.js and run the test simulator in light and dark appearances. Use example/app.js for the standard interactive demo.
