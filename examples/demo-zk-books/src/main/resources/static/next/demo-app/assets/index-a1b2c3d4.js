/**
 * Fake hashed Vite chunk used to manually verify MicroFrontend's "app" auto-discovery + "auto"
 * mount mode: a plain self-mounting SPA bundle, like `createApp(App).mount('#demo-app-root')`
 * would produce, with no custom element and no window mount/unmount functions.
 */
(function () {
    var root = document.getElementById('demo-app-root');
    if (!root) {
        console.error('[demo-app] #demo-app-root not found, was the index.html body replicated?');
        return;
    }
    console.log('[demo-app] self-mounted into #demo-app-root');
    root.innerHTML =
        '<div class="demo-app-widget-box">' +
        '<strong>app auto-discovery mode</strong><br/>' +
        'self-mounted, no tag/mountFn needed' +
        '</div>';
})();
