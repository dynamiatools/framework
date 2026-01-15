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

package mybookstore.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.websocket.WebSocketPushSender;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static tools.dynamia.integration.scheduling.SchedulerUtil.sleep;

public class PushTestViewModel {

    private int steps = 10;
    private List<String> messages = new ArrayList<>();
    private int progress;

    @Init
    public void init() {
        ZKUtil.runLater(Duration.ofSeconds(3), () -> UIMessages.showMessageDialog("This view model will test WebSocket push notifications sending a notification from the server every second for " + steps + " seconds.<br/><br/>" +
                        "You can open multiple browser tabs to see that notifications are sent to all connected clients.", "WebSocket Push Test",
                MessageType.NORMAL));
    }


    @Command
    public void startTest() {
        Desktop desktop = Executions.getCurrent().getDesktop();
        progress = 0;
        SchedulerUtil.run(() -> IntStream.range(1, steps).forEach(s -> {
            WebSocketPushSender.sendPushCommand(desktop, "pushTest");
            sleep(Duration.ofSeconds(1));
        }));
    }

    @Command
    public void broadcast() {
        progress = 0;
        SchedulerUtil.run(() -> IntStream.range(1, steps).forEach(s -> {
            WebSocketPushSender.broadcastCommand("pushTest");
            sleep(Duration.ofSeconds(1));
        }));
    }


    @Command
    @NotifyChange("*")
    public void clear() {
        messages.clear();
        progress = 0;
    }


    @GlobalCommand
    @NotifyChange("*")
    public void pushTest() {
        messages.add("Notification send from the server - " + new Date());
        if (progress > 100) {
            progress = 0;
        }
        progress = progress + 100 / steps + 1;
    }

    public List<String> getMessages() {
        return messages;
    }

    public int getProgress() {
        return progress;
    }
}
