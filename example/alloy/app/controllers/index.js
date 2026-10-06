// Copy example/items.js into your app/lib/items.js as well.
// For Android, also copy example/fonts into your app/assets/fonts.
var PillTabs = require('ti.pilltabs');
var items = require('items');
var tabs = PillTabs.createView({
    left: 15, right: 15, height: 38, items: items,
    selectedId: 'primary', aggregateId: 'all', gestureEnabled: true,
    // Android draws the `icon` of each item with this font.
    iconFamily: 'MaterialIcons-Regular'
});
// iOS pins the header of a section in a plain TableView. Android scrolls it away with the rows.
// On Android, the header is the fixed view above the table.
var header = OS_ANDROID ? $.fixedHeader : Ti.UI.createView({ height: 60, backgroundColor: '#000000' });
header.add(tabs);
var section = Ti.UI.createTableViewSection(OS_ANDROID ? {} : { headerView: header });
var rows = [];
for (var i = 0; i < 60; i++) {
    var row = Ti.UI.createTableViewRow({
        title: 'Primary — message ' + (i + 1), height: 64,
        color: '#FFFFFF', backgroundColor: '#000000'
    });
    rows.push(row);
    section.add(row);
}
$.table.setData([section]);
function selectionChanged(e) {
    rows.forEach(function (row, index) { row.title = items[e.index].title + ' — message ' + (index + 1); });
}
// Runtime attention updates, e.g. from a new-mail notification:
// tabs.showBadge('promotions');
// tabs.hideBadge('updates');
// tabs.setBadgeTintColor('transactions', '#FF9500');
tabs.addEventListener('change', selectionChanged);
$.inbox.addEventListener('close', function () {
    tabs.removeEventListener('change', selectionChanged);
    $.destroy();
});
$.navigation.open();
