// Copy app.js and items.js into a Classic iOS app's Resources directory.
// Add <module platform="iphone" version="1.1.0">ti.pilltabs</module> to tiapp.xml.
var PillTabs = require('ti.pilltabs');
var items = require('items');
var window = Ti.UI.createWindow({ title: 'Inbox', backgroundColor: '#000000' });
var navigation = Ti.UI.createNavigationWindow({ window: window });
var tabs = PillTabs.createView({
    left: 15, right: 0, rightPadding: 15, height: 38,
    items: items, selectedId: 'primary', aggregateId: 'all',
    gestureEnabled: true, toggleOnReselect: true,
    spacing: 8, trailingVisibility: 5
});
// Explicit height and opaque background keep rows from showing through the header.
var header = Ti.UI.createView({ height: 60, backgroundColor: '#000000' });
header.add(tabs);
var section = Ti.UI.createTableViewSection({ headerView: header });
var rows = [];
for (var i = 0; i < 60; i++) {
    var row = Ti.UI.createTableViewRow({
        title: 'Primary — message ' + (i + 1), height: 64,
        color: '#FFFFFF', backgroundColor: '#000000'
    });
    rows.push(row);
    section.add(row);
}
var table = Ti.UI.createTableView({
    style: Ti.UI.iOS.TableViewStyle.PLAIN, sectionHeaderTopPadding: 0,
    backgroundColor: '#000000', separatorColor: '#28282A', data: [section]
});
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
