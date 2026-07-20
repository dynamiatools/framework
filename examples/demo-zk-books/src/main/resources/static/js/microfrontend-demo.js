/**
 * Fake "microfrontend" bundle used to manually verify tools.dynamia.zk.ui.MicroFrontend.
 * Simulates what a Vue/React build would ship: a custom element AND a mount/unmount pair.
 */
(function () {
    class DemoWidget extends HTMLElement {
        connectedCallback() {
            console.log('[demo-widget] connectedCallback', {userId: this.userId, theme: this.theme, user: this.user});
            var userLine = '';
            if (this.user && typeof this.user === 'object') {
                userLine = 'user: ' + JSON.stringify(this.user) + ' (typeof ' + typeof this.user + ')<br/>';
            }
            this.innerHTML =
                '<div style="padding:1rem;border:2px dashed #6c5ce7;border-radius:8px">' +
                '<strong>custom-element mode</strong><br/>' +
                'userId: ' + this.userId + '<br/>' +
                'theme: ' + this.theme + '<br/>' +
                userLine +
                '<button id="inc">clicks: 0</button><br/>' +
                '<button id="emit" class="demo-widget-emit-btn">emit event to server</button>' +
                '</div>';
            var btn = this.querySelector('#inc');
            var clicks = 0;
            btn.addEventListener('click', function () {
                clicks++;
                btn.textContent = 'clicks: ' + clicks;
            });
            var dynamiaEmit = this.dynamiaEmit;
            this.querySelector('#emit').addEventListener('click', function () {
                if (typeof dynamiaEmit === 'function') {
                    dynamiaEmit({from: 'custom-element', clicks: clicks});
                }
            });
        }

        disconnectedCallback() {
            console.log('[demo-widget] disconnectedCallback (auto cleanup by browser)');
        }
    }

    if (!customElements.get('demo-widget')) {
        customElements.define('demo-widget', DemoWidget);
    }

    window.mountDemoApp = function (container, props) {
        container.innerHTML =
            '<div style="padding:1rem;border:2px solid #00b894;border-radius:8px">' +
            '<strong>mount-fn mode</strong><br/>userId: ' + props.userId + '<br/>' +
            '<button id="emit" class="demo-widget-emit-btn">emit event to server</button></div>';
        container.querySelector('#emit').addEventListener('click', function () {
            if (typeof props.dynamiaEmit === 'function') {
                props.dynamiaEmit({from: 'mount-fn', userId: props.userId});
            }
        });
        console.log('[demo-app] mounted via mount-fn', props);
    };

    window.unmountDemoApp = function (container) {
        container.innerHTML = '';
        console.log('[demo-app] unmounted via mount-fn');
    };
})();
