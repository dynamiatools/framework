
/*
 * Copyright (C) 2009 - 2019 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia - South America
 * All Rights Reserved.
 *
 * DynamiaTools is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License (LGPL v3) as
 * published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DynamiaTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DynamiaTools.  If not, see <https://www.gnu.org/licenses/>.
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