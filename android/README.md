# TiPillTabs Android sources

See ../README.md for the public API, the installation, and the Android differences.

- src/ti/pilltabs/TiPilltabsModule.java: module identity.
- src/ti/pilltabs/ViewProxy.java: Titanium view proxy. It owns the items, the selection, and the events.
- src/ti/pilltabs/PillTabsView.java: Material chip group in a horizontal scroll view. It also draws the icons and animates the chip widths.
- src/ti/pilltabs/PillItem.java: one accepted item.
- assets/README: keeps the media files of the repository out of the Android package.
- dist/ti.pilltabs-android-1.0.0.zip: build output.

Minimum Android version: 7.0 (API level 24). SDK used: 13.4.1.GA. MIT license.
