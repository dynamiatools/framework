/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Registry used by the MicroFrontend ZK component (tools.dynamia.zk.ui.MicroFrontend) to cache
 * bundle loading promises across component instances sharing the same "src".
 */
var DynamiaMicrofrontends = window.DynamiaMicrofrontends || {scripts: {}};

/**
 * Loads a JavaScript bundle once and caches the loading promise so multiple microfrontend
 * instances pointing to the same src share a single <script> tag/network request.
 * @param {string} src - Bundle URL.
 * @param {string} type - Script type, e.g. "module" or "text/javascript".
 * @returns {Promise<void>} Resolves once the bundle has loaded.
 */
function dynamiaLoadScript(src, type) {
    if (!DynamiaMicrofrontends.scripts[src]) {
        DynamiaMicrofrontends.scripts[src] = new Promise(function (resolve, reject) {
            var script = document.createElement('script');
            script.src = src;
            script.type = type || 'module';
            script.onload = function () {
                resolve();
            };
            script.onerror = function () {
                delete DynamiaMicrofrontends.scripts[src];
                reject(new Error('Dynamia Microfrontend: failed to load bundle ' + src));
            };
            document.head.appendChild(script);
        });
    }
    return DynamiaMicrofrontends.scripts[src];
}

/**
 * Loads (if needed) and mounts a microfrontend bundle into the container element of a
 * MicroFrontend ZK component.
 * @param {string} containerId - Id of the component's container element (its ZK uuid).
 * @param {object} config - {src, type, mode, tag, mountFn, unmountFn, props}.
 */
function dynamiaMountMicrofrontend(containerId, config) {
    dynamiaLoadScript(config.src, config.type).then(function () {
        var container = document.getElementById(containerId);
        if (!container) {
            return;
        }
        var props = config.props || {};
        container.innerHTML = '';
        if (config.mode === 'mount-fn') {
            var mountFn = window[config.mountFn];
            if (typeof mountFn === 'function') {
                mountFn(container, props);
            } else {
                console.error('Dynamia Microfrontend: mount function "' + config.mountFn + '" not found on window');
            }
        } else {
            var el = document.createElement(config.tag);
            Object.keys(props).forEach(function (key) {
                var value = props[key];
                el[key] = value;
                el.setAttribute(key, typeof value === 'object' ? JSON.stringify(value) : value);
            });
            container.appendChild(el);
        }
    }).catch(function (err) {
        console.error(err);
    });
}

/**
 * Unmounts a microfrontend previously mounted with {@link dynamiaMountMicrofrontend} in
 * "mount-fn" mode. Components using "custom-element" mode are cleaned up automatically by the
 * browser when their DOM node is removed (disconnectedCallback), so this is a no-op for them.
 * @param {string} containerId - Id of the component's container element (its ZK uuid).
 * @param {object} config - {mode, unmountFn}.
 */
function dynamiaUnmountMicrofrontend(containerId, config) {
    if (config.mode !== 'mount-fn' || !config.unmountFn) {
        return;
    }
    var container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    var unmountFn = window[config.unmountFn];
    if (typeof unmountFn === 'function') {
        try {
            unmountFn(container);
        } catch (err) {
            console.error(err);
        }
    }
}

/**
 * Changes the browser URL hash or history state.
 * @param {string} value - The new hash value or page identifier.
 */
function changeHash(value) {
    if (window.history && window.history.pushState) {
        history.pushState({}, "Page " + value, getContextPath() + "/page/" + value);
    } else {
        window.location.hash = value;
    }
}

/**
 * Sends the current URL hash to a ZK component.
 * @param {string} uuid - The UUID of the ZK component.
 */
function sendMeHash(uuid) {
    var hash = window.location.hash;
    if (hash) {
        hash = hash.substring(1, hash.length);
        if (typeof zAu !== 'undefined' && typeof zk !== 'undefined') {
            zAu.send(new zk.Event(zk.Widget.$(uuid), 'onHash', hash));
        }
    }
}

/**
 * Gets the context path of the application.
 * @returns {string} The context path.
 */
function getContextPath() {
    var path = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
    if (path === '/page') {
        path = '';
    }
    return path;
}

/**
 * Gets the server path (protocol + domain + port).
 * @returns {string} The server path.
 */
function getServerPath() {
    if (window.location.origin) {
        return window.location.origin;
    }
    return window.location.href.substring(0, window.location.href.indexOf(window.location.pathname));
}

/**
 * Gets the full context path (server path + context path).
 * @returns {string} The full context path.
 */
function getFullContextPath() {
    return getServerPath() + getContextPath();
}

/**
 * Opens a URL in a new tab.
 * @param {string} url - The URL to open.
 */
function openURL(url) {
    window.open(url, "_blank");
}

/**
 * Copies text to the clipboard.
 * Tries to use the modern Clipboard API, falls back to execCommand, and finally to prompt.
 * @param {string} txt - The text to copy.
 */
function copyToClipboard(txt) {
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(txt).catch(function(err) {
            fallbackCopyToClipboard(txt);
        });
    } else {
        fallbackCopyToClipboard(txt);
    }
}

function fallbackCopyToClipboard(txt) {
    var textArea = document.createElement("textarea");
    textArea.value = txt;

    // Avoid scrolling to bottom
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        var successful = document.execCommand('copy');
        if (!successful) {
             window.prompt("Press Ctrl+C and then Enter", txt);
        }
    } catch (err) {
        window.prompt("Press Ctrl+C and then Enter", txt);
    }

    document.body.removeChild(textArea);
}

/**
 * Toggles full screen mode.
 */
function toggleFullScreen() {
    if (document.fullScreenElement ||
        (!document.mozFullScreen && !document.webkitIsFullScreen)) {
        if (document.documentElement.requestFullScreen) {
            document.documentElement.requestFullScreen();
        } else if (document.documentElement.mozRequestFullScreen) {
            document.documentElement.mozRequestFullScreen();
        } else if (document.documentElement.webkitRequestFullScreen) {
            document.documentElement.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        }
    } else {
        if (document.cancelFullScreen) {
            document.cancelFullScreen();
        } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        } else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
        }
    }
}

/**
 * Changes the browser URI using history.pushState.
 * @param {string} title - The title of the new state.
 * @param {string} uri - The new URI.
 */
function changeURI(title, uri) {
    if (window.history && window.history.pushState) {
        history.pushState({}, title, uri);
    }
}