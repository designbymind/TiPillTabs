var PillTabs = require('ti.pilltabs');
var dark = Ti.UI.userInterfaceStyle === Ti.UI.USER_INTERFACE_STYLE_DARK;
var mode = dark ? 'dark' : 'light';
var background = dark ? '#000000' : '#FFFFFF';
var window = Ti.UI.createWindow({ backgroundColor: background });
var header = Ti.UI.createView({ width: Ti.UI.FILL, height: 70, top: 180, backgroundColor: background });
var tabs = PillTabs.createView({
    left: 15, right: 0, height: 38, rightPadding: 24,
    selectedId: 'primary', aggregateId: 'all', animated: false,
    items: [
        { id: 'primary', title: 'Primary', systemImage: 'person.fill' },
        { id: 'updates', title: 'Updates', systemImage: 'text.bubble.fill', badge: true, badgeTintColor: '#0A84FF' },
        { id: 'all', title: 'All Mail', systemImage: 'tray.fill' }
    ]
});
header.add(tabs);
window.add(header);
window.addEventListener('open', function () {
    setTimeout(function () {
        header.toImage(function (image) {
            Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'badge-' + mode + '.png').write(image);
            tabs.hideBadge('updates');
            header.toImage(function (plainImage) {
                Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'badge-hidden-' + mode + '.png').write(plainImage);
                tabs.showBadge('updates');
                Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'badge-' + mode + '-result.json').write(JSON.stringify({
                    mode: mode, selectedId: tabs.selectedId,
                    badgeRestored: tabs.items[1].badge === true,
                    backgroundColor: background
                }));
                Ti.API.info('[BADGETEST] Captures complete for ' + mode);
            }, true);
        }, true);
    }, 700);
});
window.open();
