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