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

package tools.dynamia.zk.websocket;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import tools.dynamia.zk.util.ZKBindingUtil;

public class WebSocketAuService implements AuService {
    @Override
    public boolean service(AuRequest request, boolean everError) {

        if ("fireGlobalCommand".equalsIgnoreCase(request.getCommand())) {
            String globalCommand = (String) request.getData().get("command");
            if (globalCommand != null) {
                ZKBindingUtil.postGlobalCommand(globalCommand, request.getData());
                return true;
            }
        }


        return false;
    }
}
