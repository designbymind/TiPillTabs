// SF Symbols and colors from the supplied MTabBar demo.
module.exports = [
    { id: 'primary', title: 'Primary', systemImage: 'person.fill', activeBackgroundColor: '#007AFF' },
    { id: 'transactions', badge: true, title: 'Transactions', systemImage: 'cart.fill', activeBackgroundColor: '#34C759' },
    { id: 'updates', badge: true, title: 'Updates', systemImage: 'text.bubble.fill', activeBackgroundColor: '#5856D6' },
    { id: 'promotions', title: 'Promotions', systemImage: 'megaphone.fill', activeBackgroundColor: '#FF2D55' },
    { id: 'all', title: 'All Mail', systemImage: 'tray.fill', activeBackgroundColor: '#FFFFFF', activeTintColor: '#000000' }
].map(function (item) {
    item.tintColor = '#8E8E93';
    item.backgroundColor = '#28282A';
    item.activeTintColor = item.activeTintColor || '#FFFFFF';
    return item;
});
