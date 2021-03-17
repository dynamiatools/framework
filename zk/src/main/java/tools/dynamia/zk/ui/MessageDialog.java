/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.zkoss.zul.Messagebox;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.zk.util.ZKUtil;

import java.util.function.Consumer;

public class MessageDialog implements MessageDisplayer {

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

        String icon = Messagebox.NONE;

        switch (type) {
            case NORMAL:
                icon = Messagebox.NONE;
                break;
            case ERROR:
            case CRITICAL:
                icon = Messagebox.ERROR;
                break;
            case INFO:
            case SPECIAL:
                icon = Messagebox.INFORMATION;
                break;
            case WARNING:
                icon = Messagebox.EXCLAMATION;
                break;
        }

        Messagebox.show(message, title, Messagebox.OK, icon);

    }

    @Override
    public void showQuestion(String message, String title, final Callback onYesResponse) {

        title = fixTitle(title);

        ZKUtil.showQuestion(message, title, t -> {
            if (t.getButton() == Messagebox.Button.YES) {
                onYesResponse.doSomething();
            }
        });
    }

    @Override
    public void showQuestion(String message, String title, final Callback onYesResponse,
                             final Callback onNoResponseCallback) {
        title = fixTitle(title);

        ZKUtil.showQuestion(message, title, t -> {
            if (t.getButton() == Messagebox.Button.YES) {
                onYesResponse.doSomething();
            } else if (t.getButton() == Messagebox.Button.NO) {
                onNoResponseCallback.doSomething();
            }
        });
    }

    private String fixTitle(String title) {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            return Messages.get(MessageDialog.class, "areyousure");
        }
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
        showInput(title, valueClass, null, onValue);
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
        ZKUtil.showInputDialog(title, valueClass, defaultValue, evt -> onValue.accept((T) evt.getData()));
    }

}
