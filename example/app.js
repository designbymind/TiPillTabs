// Copy app.js and items.js into the Resources directory of a Classic app.
// For Android, also copy the fonts directory into the Resources directory.
// Add <module platform="iphone" version="1.0.0">ti.pilltabs</module> to tiapp.xml for iOS.
// Add <module platform="android" version="1.0.0">ti.pilltabs</module> to tiapp.xml for Android.
var PillTabs = require('ti.pilltabs');
var items = require('items');
var isAndroid = Ti.Platform.osname === 'android';
var window = Ti.UI.createWindow({
    title: 'Inbox', backgroundColor: '#000000', layout: isAndroid ? 'vertical' : 'composite'
});
var navigation = Ti.UI.createNavigationWindow({ window: window });
var tabs = PillTabs.createView({
    left: 15, right: 15, height: 38,
    items: items, selectedId: 'primary', aggregateId: 'all',
    gestureEnabled: true, toggleOnReselect: true,
    spacing: 8, trailingVisibility: 5,
    // Android draws the `icon` of each item with this font. Copy the fonts directory into Resources.
    iconFamily: 'MaterialIcons-Regular'
});
// Explicit height and opaque background keep rows from showing through the header.
var header = Ti.UI.createView({ height: 60, backgroundColor: '#000000' });
header.add(tabs);
// iOS pins the header of a section in a plain TableView. Android scrolls it away with the rows.
// On Android, the header is a fixed view above the table.
var section = Ti.UI.createTableViewSection(isAndroid ? {} : { headerView: header });
var rows = [];
for (var i = 0; i < 60; i++) {
    var row = Ti.UI.createTableViewRow({
        title: 'Primary — message ' + (i + 1), height: 64,
        color: '#FFFFFF', backgroundColor: '#000000'
    });
    rows.push(row);
    section.add(row);
}
var tableOptions = { backgroundColor: '#000000', separatorColor: '#28282A', data: [section] };
if (isAndroid) {
    window.add(header);
} else {
    // Ti.UI.iOS does not exist on Android.
    tableOptions.style = Ti.UI.iOS.TableViewStyle.PLAIN;
    tableOptions.sectionHeaderTopPadding = 0;
}
var table = Ti.UI.createTableView(tableOptions);
window.add(table);
function selectionChanged(e) {
    var item = items[e.index];
    Ti.API.info('[TiPillTabs] ' + JSON.stringify({ id: e.id, previousId: e.previousId, reason: e.reason }));
    // Preserve the same header, section and rows when changing categories.
    rows.forEach(function (row, index) { row.title = item.title + ' — message ' + (index + 1); });
}
// Runtime attention updates, e.g. from a new-mail notification:
// tabs.showBadge('promotions');
// tabs.hideBadge('updates');
// tabs.setBadgeTintColor('transactions', '#FF9500');
tabs.addEventListener('change', selectionChanged);
window.addEventListener('close', function () { tabs.removeEventListener('change', selectionChanged); });
navigation.open();
