// SF Symbols and colors from the supplied MTabBar demo.
// `icon` is for Android. It is the code point of the comparable glyph in the Material Icons font:
// person, shopping_cart, chat, campaign, inbox. The view sets the font with `iconFamily`.
module.exports = [
    { id: 'primary', title: 'Primary', systemImage: 'person.fill', icon: '\ue7fd', activeBackgroundColor: '#007AFF' },
    { id: 'transactions', badge: true, title: 'Transactions', systemImage: 'cart.fill', icon: '\ue8cc', activeBackgroundColor: '#34C759' },
    { id: 'updates', badge: true, title: 'Updates', systemImage: 'text.bubble.fill', icon: '\ue0b7', activeBackgroundColor: '#5856D6' },
    { id: 'promotions', title: 'Promotions', systemImage: 'megaphone.fill', icon: '\uef49', activeBackgroundColor: '#FF2D55' },
    { id: 'all', title: 'All Mail', systemImage: 'tray.fill', icon: '\ue156', activeBackgroundColor: '#FFFFFF', activeTintColor: '#000000' }
].map(function (item) {
    item.tintColor = '#8E8E93';
    item.backgroundColor = '#28282A';
    item.activeTintColor = item.activeTintColor || '#FFFFFF';
    return item;
});
