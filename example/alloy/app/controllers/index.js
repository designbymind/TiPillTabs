// Copy example/items.js into your app/lib/items.js as well.
var PillTabs = require('ti.pilltabs');
var items = require('items');
var tabs = PillTabs.createView({
    left: 15, right: 15, height: 38, items: items,
    selectedId: 'primary', aggregateId: 'all', gestureEnabled: true
});
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
