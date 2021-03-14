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

function initWebSocket(uri) {

    var sock = new SockJS(uri);
    var desktopId = zk.Desktop.$().id;

    sock.onopen = function () {
        console.log('DynamiaTools WebSocket Connected - '+desktopId);
        sock.send(desktopId);
    };

    sock.onmessage = function (e) {
        console.log('DynamiaTools command received: ', e.data);
        zAu.send(new zk.Event(zk.Desktop.$(), 'fireGlobalCommand', {command: e.data}, {}));
    };

    sock.onclose = function () {
        console.log('DynamiaTools WebSocket connection closed');
    };
}

zk.afterMount(initWebSocket('/ws-commands'), 1000);