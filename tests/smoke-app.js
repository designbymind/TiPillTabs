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
tabs.addEventListener('change', selectionChanged);
window.addEventListener('close', function () { tabs.removeEventListener('change', selectionChanged); });
navigation.open();

// Test the actual JS bridge after native header layout is complete.
var checks = [];
function check(name, result) {
    checks.push({ name: name, passed: !!result });
    Ti.API.info('[PILLTEST] ' + name + ': ' + (result ? 'PASS' : 'FAIL'));
}
var events = [];
tabs.addEventListener('change', function (e) {
    events.push({ id: e.id, index: e.index, previousId: e.previousId, reason: e.reason });
});
window.addEventListener('open', function () {
    setTimeout(function () {
        function tabItem(id) { return tabs.items.filter(function (item) { return item.id === id; })[0]; }
        check('initial badge property', tabItem('updates').badge === true);
        tabs.hideBadge('updates');
        check('hideBadge bridge', tabItem('updates').badge === false);
        tabs.showBadge('updates');
        check('showBadge bridge', tabItem('updates').badge === true);
        tabs.setBadge('transactions', false);
        check('setBadge bridge', tabItem('transactions').badge === false);
        tabs.setBadgeTintColor('updates', '#FF9500');
        check('badge tint override', tabItem('updates').badgeTintColor === '#FF9500');
        tabs.setBadgeTintColor('updates', null);
        check('badge tint fallback reset', !tabItem('updates').badgeTintColor);
        tabs.showBadge('missing');
        check('unknown badge id ignored', tabs.items.length === items.length);
        check('badges preserve selection/events', tabs.selectedId === 'primary' && events.length === 0);
        tabs.items = tabs.items.map(function (item) { return Object.assign({}, item); });
        check('badge state survives items reassignment', tabItem('updates').badge === true && tabItem('transactions').badge === false);
        tabs.showBadge('transactions');
        check('initial selection', tabs.selectedId === 'primary');
        check('no initialization event', events.length === 0);
        tabs.selectedId = 'transactions';
        setTimeout(function () {
            check('setter/getter', tabs.selectedId === 'transactions');
            check('one programmatic event', events.length === 1 && events[0].reason === 'programmatic' && events[0].previousId === 'primary' && events[0].index === 1);
            tabs.selectedId = 'transactions';
            tabs.selectedId = 'missing';
            setTimeout(function () {
                check('no duplicate/invalid events', events.length === 1);
                check('invalid selection preserved', tabs.selectedId === 'transactions');
                tabs.items = items.map(function (item) {
                    return Object.assign({}, item, { tintColor: '#FF9500', backgroundColor: '#333333' });
                });
                tabs.selectedId = 'all';
                setTimeout(function () {
                    check('live items preserve selection', events.length === 2 && events[1].previousId === 'transactions');
                    check('aggregate programmatic selection', tabs.selectedId === 'all');
                    tabs.selectedId = 'primary';
                    table.scrollToIndex(25, { animated: false });
                    setTimeout(function () {
                        check('selection survives table scroll', tabs.selectedId === 'primary');
                        tabs.items = items; // Restore original Mail-style colors and dots for visual QA.
                        tabs.selectedId = 'promotions';
                        var report = { checks: checks, events: events };
                        Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'pilltabs-results.json').write(JSON.stringify(report));
                        Ti.API.info('[PILLTEST] FINISHED ' + JSON.stringify(report));
                    }, 700);
                }, 400);
            }, 400);
        }, 400);
    }, 700);
});
