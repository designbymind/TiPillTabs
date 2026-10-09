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
        check('initial rightPadding bridge', tabs.rightPadding === 15);
        var initialWidth = tabs.rect.width;
        tabs.rightPadding = 24;
        check('live rightPadding bridge', tabs.rightPadding === 24);
        check('padding preserves outer width', tabs.rect.width === initialWidth);
        tabs.rightPadding = -5;
        check('negative padding clamps to zero', tabs.rightPadding === 0);
        tabs.rightPadding = Infinity;
        check('nonfinite padding resets to zero', tabs.rightPadding === 0);
        tabs.rightPadding = 24;
        check('padding preserves selection/events', tabs.selectedId === 'primary' && events.length === 0);
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
                        tabs.selectedId = 'all'; // Visual QA: trailing selected pill must leave the padded gap.
                        var report = { checks: checks, events: events };
                        Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'pilltabs-results.json').write(JSON.stringify(report));
                        Ti.API.info('[PILLTEST] FINISHED ' + JSON.stringify(report));
                        // Capture settled native geometry for both selection modes.
                        setTimeout(function () {
                            tabs.toImage(function (allImage) {
                                Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'padding-all.png').write(allImage);
                                tabs.animated = false;
                                tabs.selectedId = 'primary';
                                setTimeout(function () {
                                    tabs.toImage(function (categoryImage) {
                                        Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'padding-category.png').write(categoryImage);
                                        tabs.selectedId = 'all';
                                        tabs.animated = true;
                                        Ti.API.info('[PILLTEST] Geometry captures complete');
                                    }, true);
                                }, 200);
                            }, true);
                        }, 500);
                    }, 700);
                }, 400);
            }, 400);
        }, 400);
    }, 700);
});
