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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import tools.dynamia.ui.MessageType;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
public class MessagePopup extends MessageDialog {

    private String posicion = "right,bottom";
    private int timeout = 4000;

    public MessagePopup() {
    }

    public MessagePopup(Map<String, Object> config) {

        Object pos = config.get("position");
        if (pos != null) {
            posicion = "" + pos;
        }
        Object time = config.get("timeout");
        if (time != null) {
            try {
                timeout = Integer.parseInt("" + time);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void showMessage(String message) {
        showMessage(message, "Mensaje", MessageType.NORMAL);
    }

    @Override
    public void showMessage(String message, MessageType type) {
        showMessage(message, "Message", type);
    }

    @Override
    public void showMessage(String message, String title, MessageType type) {

        Timer timer = new Timer(timeout);
        final Window window = new Window();
        window.setPosition(posicion);
        window.setSclass("msg-popup msg-popup-" + type.name().toLowerCase());
        window.setPage(ZKUtil.getFirstPage());
        window.setClosable(true);
        window.setVisible(true);
        window.doPopup();
        timer.setParent(window);
        timer.addEventListener("onTimer", (EventListener) event -> window.detach());

        Vbox contenido = new Vbox();
        contenido.setStyle("margin-left: 10px;margin-top: 5px");
        contenido.setParent(window);
        contenido.setWidth("98%");
        contenido.setHeight("100%");

        {
            Hbox superior = new Hbox();
            superior.setParent(contenido);
            superior.setWidth("100%");

            {
                if (title == null) {
                    title = "Mensage";
                }
                Label titulo = new Label(title);
                titulo.setParent(superior);
                titulo.setSclass("msg-popup-title");
                Div close = new Div();
                close.setParent(superior);
                close.setSclass("msg-popup-close");
                close.addEventListener("onClick", (EventListener) event -> window.detach());
            }

            Hbox inferior = new Hbox();
            inferior.setParent(contenido);
            inferior.setWidth("100%");
            inferior.setWidth("100%");

            {
                Div icon = new Div();
                icon.setParent(inferior);
                icon.setSclass("msg-popup-icon msg-popup-icon-" + type.name().toLowerCase());
                Label msg = new Label("" + message);
                msg.setParent(inferior);
                msg.setMultiline(true);
                msg.setSclass("msg-popup-message");
            }
        }

    }

}
