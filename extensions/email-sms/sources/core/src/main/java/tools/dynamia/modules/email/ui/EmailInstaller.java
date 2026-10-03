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

package tools.dynamia.modules.email.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailAddress;
import tools.dynamia.modules.email.domain.EmailMessageLog;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.SMSMessageLog;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.navigation.PageGroup;

/**
 * The EmailInstaller class is responsible for providing the email module for navigation.
 */
@Component("EmailModule")
public class EmailInstaller implements ModuleProvider {


    @Override
    public Module getModule() {
        Module email = Module.getRef("system");

        PageGroup group = new PageGroup("email", "Email");
        group.addPage(new CrudPage("accounts", "Accounts", EmailAccount.class).longName("Email / SMS Accounts"));
        group.addPage(new CrudPage("templates", "Templates", EmailTemplate.class));
        group.addPage(new CrudPage("addresses", "Email Addresses", EmailAddress.class));
        group.addPage(new CrudPage("emailLog", "Email Log", EmailMessageLog.class));
        group.addPage(new CrudPage("smsLog", "SMS Log", SMSMessageLog.class));
        email.addPageGroup(group);
        return email;
    }

}
