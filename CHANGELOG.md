# Changelog

## 1.1.0 (reissued)

- Add live `rightPadding`, applied only when the final item is selected. Other selections retain the full-width trailing preview.
- Animate the inner clipping edge with the pill geometry; preserve zero-padding behavior and selection events.
- Replace the badge border with an icon cutout that exposes the actual pill background and avoids translucent-color halos.
- Draw dots as display-scale-aware circles with space for smooth edges, retaining their size and position.
- Update Classic/Alloy examples, documentation, and native light/dark appearance checks.

This reissue replaces the original 1.1.0 binary. Clean the consuming app after replacing a same-version module to avoid cached native code.

## 1.0.0

Initial iOS release:

- Animated Mail-style pill tabs built in Swift and UIKit.
- Configurable symbols, titles, active/inactive tint and background colors.
- Stable-ID selection, change events, and optional tap/swipe aggregate toggling.
- Per-tab attention dots with tint overrides and live show/hide methods.
- Plain TableViewSection header integration and Classic/Alloy examples.
- iOS 17+; packaged using Titanium SDK 13.4.1.GA for device and simulator.

See [validation notes](VALIDATION.md) for tested behavior and remaining device checks.
