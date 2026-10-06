# Changelog

## 1.0.0

Initial iOS release:

- Animated Mail-style pill tabs built in Swift and UIKit.
- Configurable symbols, titles, active/inactive tint and background colors.
- Stable-ID selection, change events, and optional tap/swipe aggregate toggling.
- Per-tab attention dots with tint overrides and live show/hide methods.
- Plain TableViewSection header integration and Classic/Alloy examples.
- iOS 17+; packaged using Titanium SDK 13.4.1.GA for device and simulator.

## 1.0.0 (Android)

Initial Android release:

- Material chip group with single selection and the JavaScript API of the iOS module.
- Titles, and active and inactive tint and background colors with theme defaults.
- Only the selected chip shows its title. A selection change animates the widths of the chips, with `animated` and `animationDuration`.
- Optional icons from an icon font (`icon`, `iconFamily`) or from an image (`image`).
- Stable-ID selection and `change` events for taps, programmatic selection, and item updates.
- Badge state in `items` for parity with iOS. Android draws no attention dot.
- The aggregate toggle, the swipe gesture, and the trailing preview are accepted and ignored.
- Android 7.0+. The package uses Titanium SDK 13.4.1.GA.

See [validation notes](VALIDATION.md) for tested behavior and remaining device checks.
