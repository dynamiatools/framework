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
 * bundle/stylesheet/app-discovery loading promises across component instances sharing the same
 * "src"/"css"/"app".
 */
var DynamiaMicrofrontends = window.DynamiaMicrofrontends || {scripts: {}, styles: {}, styleTexts: {}, apps: {}};

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
 * Loads a stylesheet once and caches the loading promise, the same way {@link dynamiaLoadScript}
 * does for bundles.
 * @param {string} href - Stylesheet URL.
 * @returns {Promise<void>} Resolves once the stylesheet has loaded (or failed, which only logs).
 */
function dynamiaLoadStyle(href) {
    if (!DynamiaMicrofrontends.styles[href]) {
        DynamiaMicrofrontends.styles[href] = new Promise(function (resolve) {
            var link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = href;
            link.onload = function () {
                resolve();
            };
            link.onerror = function () {
                delete DynamiaMicrofrontends.styles[href];
                console.error('Dynamia Microfrontend: failed to load stylesheet ' + href);
                resolve();
            };
            document.head.appendChild(link);
        });
    }
    return DynamiaMicrofrontends.styles[href];
}

/**
 * Loads every stylesheet in a comma-separated list, e.g. a Vite/webpack build's separate CSS
 * chunk(s) shipped alongside the JS bundle.
 * @param {string} css - Comma-separated stylesheet URL(s), may be null/empty.
 * @returns {Promise<void>} Resolves once every stylesheet has loaded.
 */
function dynamiaLoadStyles(css) {
    if (!css) {
        return Promise.resolve();
    }
    var hrefs = css.split(',').map(function (href) {
        return href.trim();
    }).filter(Boolean);
    return Promise.all(hrefs.map(dynamiaLoadStyle));
}

/**
 * Fetches a stylesheet's text once and caches the promise, so it can be inlined as a
 * {@code <style>} into any number of shadow roots (a {@code <link>} element, unlike a fetched
 * text, can only ever live in one place in the DOM).
 * @param {string} href - Stylesheet URL.
 * @returns {Promise<string>} Resolves to the stylesheet text, or "" if it failed to load (logged).
 */
function dynamiaFetchStyleText(href) {
    if (!DynamiaMicrofrontends.styleTexts[href]) {
        DynamiaMicrofrontends.styleTexts[href] = fetch(href).then(function (res) {
            if (!res.ok) {
                throw new Error('Dynamia Microfrontend: failed to fetch stylesheet ' + href + ' (' + res.status + ')');
            }
            return res.text();
        }).catch(function (err) {
            delete DynamiaMicrofrontends.styleTexts[href];
            console.error(err);
            return '';
        });
    }
    return DynamiaMicrofrontends.styleTexts[href];
}

/**
 * Loads every stylesheet in a comma-separated list as inline {@code <style>} tags appended to a
 * shadow root, the shadow-DOM equivalent of {@link dynamiaLoadStyles}.
 * @param {string} css - Comma-separated stylesheet URL(s), may be null/empty.
 * @param {ShadowRoot} root - Shadow root to append the resulting <style> tags to.
 * @returns {Promise<void>} Resolves once every stylesheet has been inlined.
 */
function dynamiaLoadStylesIntoShadow(css, root) {
    if (!css) {
        return Promise.resolve();
    }
    var hrefs = css.split(',').map(function (href) {
        return href.trim();
    }).filter(Boolean);
    return Promise.all(hrefs.map(dynamiaFetchStyleText)).then(function (texts) {
        texts.forEach(function (text) {
            if (text) {
                var style = document.createElement('style');
                style.textContent = text;
                root.appendChild(style);
            }
        });
    });
}

/**
 * Resolves a URL found in a build's index.html against its app root: absolute (starting with "/"
 * or a scheme) URLs are used as-is, relative ones (e.g. "assets/x.js" or "./assets/x.js") are
 * joined to the app root, matching how the browser itself would resolve them relative to that
 * index.html.
 * @param {string} appRoot - App root URL, e.g. "/static/next/subscription" (no trailing slash).
 * @param {string} url - URL found in the index.html.
 * @returns {string} Resolved absolute-path URL.
 */
function dynamiaResolveAppUrl(appRoot, url) {
    if (/^([a-z]+:)?\//i.test(url)) {
        return url;
    }
    return appRoot + '/' + url.replace(/^\.\//, '');
}

/**
 * Auto-discovers the bundle, stylesheet(s) and self-mount target of a bundler production build
 * (e.g. Vite's {@code dist/}) by fetching its {@code index.html} and extracting the entry
 * {@code <script type="module">}, {@code <link rel="stylesheet">} tags, and {@code <body>} markup
 * (stripped of any {@code <script>} tags) it contains, so hashed output filenames never need to be
 * hardcoded and self-mounting bundles (e.g. {@code createApp(App).mount('#app')}) find their
 * target. Only verified against Vite output.
 * @param {string} appRoot - App root URL, e.g. "/static/next/subscription".
 * @returns {Promise<{src: string, css: string, bodyHtml: string}>} Discovered bundle/stylesheet URLs and body markup.
 */
function dynamiaResolveApp(appRoot) {
    if (!DynamiaMicrofrontends.apps[appRoot]) {
        var base = appRoot.replace(/\/$/, '');
        DynamiaMicrofrontends.apps[appRoot] = fetch(base + '/index.html').then(function (res) {
            if (!res.ok) {
                throw new Error('Dynamia Microfrontend: failed to fetch ' + base + '/index.html (' + res.status + ')');
            }
            return res.text();
        }).then(function (html) {
            var scriptTag = (html.match(/<script[^>]*type=["']module["'][^>]*>/i) || [])[0];
            var src = scriptTag && (scriptTag.match(/\ssrc=["']([^"']+)["']/i) || [])[1];
            if (!src) {
                throw new Error('Dynamia Microfrontend: no <script type="module" src="..."> found in ' + base + '/index.html');
            }
            var css = (html.match(/<link[^>]*rel=["']stylesheet["'][^>]*>/gi) || [])
                .map(function (tag) {
                    return (tag.match(/\shref=["']([^"']+)["']/i) || [])[1];
                })
                .filter(Boolean)
                .map(function (href) {
                    return dynamiaResolveAppUrl(base, href);
                })
                .join(',');
            var bodyMatch = html.match(/<body[^>]*>([\s\S]*)<\/body>/i);
            var bodyHtml = bodyMatch ? bodyMatch[1].replace(/<script[\s\S]*?<\/script>/gi, '') : '';
            return {src: dynamiaResolveAppUrl(base, src), css: css, bodyHtml: bodyHtml};
        }).catch(function (err) {
            delete DynamiaMicrofrontends.apps[appRoot];
            throw err;
        });
    }
    return DynamiaMicrofrontends.apps[appRoot];
}

/**
 * Sends a named event with a data payload from a MicroFrontend ZK component's container back to
 * its server-side component.
 * @param {string} containerId - Id of the component's container element (its ZK uuid).
 * @param {string} name - Event name, e.g. "onMicrofrontendReady"/"onMicrofrontendError".
 * @param {*} data - Event data, or null.
 */
function dynamiaFireHostEvent(containerId, name, data) {
    zAu.send(new zk.Event(zk.Widget.$(containerId), name, data));
}

/**
 * Loads (if needed) and mounts a microfrontend bundle into the container element of a
 * MicroFrontend ZK component. Injects a {@code dynamiaEmit(data)} function into the mounted
 * bundle's props so it can notify the server-side ZK component, which fires "onMicrofrontendEvent"
 * (bindable in MVVM with {@code onMicrofrontendEvent="@command(...)"}). In "auto" mode the bundle
 * self-mounts, so its discovered index.html body markup is placed into the container before the
 * bundle loads, and nothing further is done once it loads. Auto mode only supports one live
 * instance of a given "app" per page at a time (its bundle runs once and self-mounts via a
 * hardcoded id from its own index.html): remounting the same container (e.g. a ZK MVVM prop
 * change, or the page redrawing the same component) is allowed, but a genuinely different,
 * still-live container for the same app is refused with a console error instead of silently
 * rendering nothing. With {@code config.shadow}, "custom-element"/"mount-fn" mount inside
 * a shadow root attached to the container (its stylesheet(s) inlined into that root instead of
 * document head) for CSS/DOM isolation from the host page; "auto" cannot be combined with shadow
 * since its bundle looks up its mount target with a page-level document.getElementById() call.
 *
 * When a previous instance is already mounted in this container with the same shape (mode, resolved
 * bundle src, tag/mountFn, shadow), only props changed: the existing instance is updated in place
 * instead of destroyed and recreated (important once props are MVVM-bound, since every single
 * @bind-ed property change would otherwise wipe any state the mounted app holds). "custom-element"
 * just reassigns the existing element's properties; "mount-fn" calls "updateFn" if configured,
 * otherwise it tears the old instance down (via unmountFn) and mounts fresh, same as any other
 * structural change (src/app/tag/mountFn/shadow actually changing).
 *
 * Fires "onMicrofrontendReady" once mounting/updating succeeds, or "onMicrofrontendError" (with
 * {message} data) if loading or mounting fails for any reason, both bindable server-side with
 * onMicrofrontendReady/onMicrofrontendError="@command(...)".
 * @param {string} containerId - Id of the component's container element (its ZK uuid).
 * @param {object} config - {src, css, app, type, mode, tag, mountFn, unmountFn, updateFn, shadow, props}.
 */
function dynamiaMountMicrofrontend(containerId, config) {
    if (config.mode === 'auto') {
        if (config.shadow) {
            var shadowAutoMsg = 'Dynamia Microfrontend: shadow=true is not supported with "auto" mode — the bundle finds ' +
                'its mount target via a page-level document.getElementById() call it makes itself, which cannot see ' +
                'inside a shadow root. Use tag= (custom element) or mountFn=/unmountFn= instead.';
            console.error(shadowAutoMsg);
            dynamiaFireHostEvent(containerId, 'onMicrofrontendError', {message: shadowAutoMsg});
            return;
        }
        DynamiaMicrofrontends.autoMounted = DynamiaMicrofrontends.autoMounted || {};
        var owner = DynamiaMicrofrontends.autoMounted[config.app];
        if (owner && owner !== containerId && document.getElementById(owner)) {
            var duplicateAppMsg = 'Dynamia Microfrontend: "' + config.app + '" is already auto-mounted on this page. ' +
                'Auto mode self-mounts via a hardcoded id from the bundle\'s own index.html and its script ' +
                'only runs once, so it cannot support a second simultaneous instance of the same app. ' +
                'Rebuild it as a custom element (use tag=) or expose mount/unmount functions ' +
                '(mountFn=/unmountFn=) if you need more than one instance.';
            console.error(duplicateAppMsg);
            dynamiaFireHostEvent(containerId, 'onMicrofrontendError', {message: duplicateAppMsg});
            return;
        }
        DynamiaMicrofrontends.autoMounted[config.app] = containerId;
    }
    DynamiaMicrofrontends.instances = DynamiaMicrofrontends.instances || {};
    var resolved = config.app ? dynamiaResolveApp(config.app) : Promise.resolve({src: config.src, css: config.css, bodyHtml: ''});
    resolved.then(function (bundle) {
        var container = document.getElementById(containerId);
        if (!container) {
            return null;
        }
        if (config.mode === 'auto') {
            container.innerHTML = bundle.bodyHtml || '';
            return Promise.all([dynamiaLoadScript(bundle.src, config.type), dynamiaLoadStyles(bundle.css)]).then(function () {
                return {auto: true};
            });
        }

        var prev = DynamiaMicrofrontends.instances[containerId];
        var canUpdateInPlace = prev && prev.mode === config.mode && prev.src === bundle.src &&
            prev.shadow === !!config.shadow &&
            (config.mode === 'mount-fn'
                ? prev.mountFn === config.mountFn && !!config.updateFn
                : prev.tag === config.tag);
        if (canUpdateInPlace) {
            return {update: true, prev: prev};
        }

        if (prev && prev.mode === 'mount-fn' && prev.unmountFn) {
            var oldUnmount = window[prev.unmountFn];
            if (typeof oldUnmount === 'function') {
                try {
                    oldUnmount(prev.mountTarget);
                } catch (err) {
                    console.error(err);
                }
            }
        }

        var root = container;
        var cssPromise;
        if (config.shadow) {
            root = container.shadowRoot || container.attachShadow({mode: 'open'});
            cssPromise = dynamiaLoadStylesIntoShadow(bundle.css, root);
        } else {
            cssPromise = dynamiaLoadStyles(bundle.css);
        }
        root.innerHTML = '';
        var mountTarget = root;
        if (config.shadow) {
            mountTarget = document.createElement('div');
            root.appendChild(mountTarget);
        }
        return Promise.all([dynamiaLoadScript(bundle.src, config.type), cssPromise]).then(function () {
            return {update: false, root: root, mountTarget: mountTarget, src: bundle.src};
        });
    }).then(function (result) {
        if (!result) {
            return;
        }
        if (result.auto) {
            dynamiaFireHostEvent(containerId, 'onMicrofrontendReady', null);
            return;
        }
        var props = config.props || {};
        props.dynamiaEmit = function (data) {
            dynamiaFireHostEvent(containerId, 'onMicrofrontendEvent', data);
        };

        if (result.update) {
            var prev = result.prev;
            if (config.mode === 'mount-fn') {
                var updateFn = window[config.updateFn];
                if (typeof updateFn === 'function') {
                    updateFn(prev.mountTarget, props);
                } else {
                    var noUpdateFnMsg = 'Dynamia Microfrontend: update function "' + config.updateFn + '" not found on window';
                    console.error(noUpdateFnMsg);
                    dynamiaFireHostEvent(containerId, 'onMicrofrontendError', {message: noUpdateFnMsg});
                    return;
                }
            } else {
                Object.keys(props).forEach(function (key) {
                    var value = props[key];
                    prev.el[key] = value;
                    if (typeof value !== 'function') {
                        prev.el.setAttribute(key, typeof value === 'object' ? JSON.stringify(value) : value);
                    }
                });
            }
            prev.unmountFn = config.unmountFn;
            dynamiaFireHostEvent(containerId, 'onMicrofrontendReady', null);
            return;
        }

        var root = result.root, mountTarget = result.mountTarget;
        var instance = {
            mode: config.mode, src: result.src, tag: config.tag, mountFn: config.mountFn,
            unmountFn: config.unmountFn, shadow: !!config.shadow, root: root, mountTarget: mountTarget
        };
        if (config.mode === 'mount-fn') {
            var mountFn = window[config.mountFn];
            if (typeof mountFn === 'function') {
                mountFn(mountTarget, props);
            } else {
                var noMountFnMsg = 'Dynamia Microfrontend: mount function "' + config.mountFn + '" not found on window';
                console.error(noMountFnMsg);
                dynamiaFireHostEvent(containerId, 'onMicrofrontendError', {message: noMountFnMsg});
                return;
            }
        } else {
            var el = document.createElement(config.tag);
            Object.keys(props).forEach(function (key) {
                var value = props[key];
                el[key] = value;
                if (typeof value !== 'function') {
                    el.setAttribute(key, typeof value === 'object' ? JSON.stringify(value) : value);
                }
            });
            root.appendChild(el);
            instance.el = el;
        }
        DynamiaMicrofrontends.instances[containerId] = instance;
        dynamiaFireHostEvent(containerId, 'onMicrofrontendReady', null);
    }).catch(function (err) {
        console.error(err);
        dynamiaFireHostEvent(containerId, 'onMicrofrontendError', {message: err && err.message ? err.message : String(err)});
    });
}

/**
 * Unmounts a microfrontend previously mounted with {@link dynamiaMountMicrofrontend} and clears its
 * update-in-place tracking. In "mount-fn" mode it calls "unmountFn" explicitly. Components using
 * "custom-element" mode are cleaned up automatically by the browser when their DOM node is removed
 * (disconnectedCallback). With {@code config.shadow}, passes the same shadow-root-scoped <div> that
 * was originally passed to mountFn, not the container itself.
 * @param {string} containerId - Id of the component's container element (its ZK uuid).
 * @param {object} config - {mode, unmountFn, shadow}.
 */
function dynamiaUnmountMicrofrontend(containerId, config) {
    if (DynamiaMicrofrontends.instances) {
        delete DynamiaMicrofrontends.instances[containerId];
    }
    if (config.mode !== 'mount-fn' || !config.unmountFn) {
        return;
    }
    var container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    var target = container;
    if (config.shadow && container.shadowRoot) {
        target = container.shadowRoot.firstElementChild || container.shadowRoot;
    }
    var unmountFn = window[config.unmountFn];
    if (typeof unmountFn === 'function') {
        try {
            unmountFn(target);
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