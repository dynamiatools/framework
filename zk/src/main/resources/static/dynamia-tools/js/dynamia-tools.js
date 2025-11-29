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

function changeHash(value) {
    if (window.history && window.history.pushState) {

        history.pushState({}, "Page " + value, getContextPath() + "/page/"
                + value);
    } else {
        window.location.hash = value;
    }
}

function sendMeHash(uuid) {
    var hash = window.location.hash;
    if (hash) {
        hash = hash.substring(1, hash.length);

        zAu.send(new zk.Event(zk.Widget.$(uuid), 'onHash', hash));
    }
}

function getContextPath() {
    var path = window.location.pathname.substring(0, window.location.pathname
            .indexOf("/", 2));

    if (path == '/page') {
        path = '';
    }

    return path;
}

function getServerPath() {
    return window.location.href.substring(0, window.location.href
            .indexOf(window.location.pathname));
}

function getFullContextPath() {
    return getServerPath() + getContextPath();
}

function openURL(url) {
    window.open(url, "_blank");
}

function copyToClipboard(txt) {
    window.prompt("Presione Ctrl+C y luego Enter", txt);
}

function toggleFullScreen() {
    if ((document.fullScreenElement && document.fullScreenElement !== null)
            || (!document.mozFullScreen && !document.webkitIsFullScreen)) {
        if (document.documentElement.requestFullScreen) {
            document.documentElement.requestFullScreen();
        } else if (document.documentElement.mozRequestFullScreen) {
            document.documentElement.mozRequestFullScreen();
        } else if (document.documentElement.webkitRequestFullScreen) {
            document.documentElement
                    .webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
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

function changeURI(title,uri) {
    if (window.history && window.history.pushState) {
        history.pushState({}, title,  uri);
    }
}