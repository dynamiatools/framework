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

package tools.dynamia.modules.saas.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.modules.saas.domain.*;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

/**
 * @author Mario Serrano Leones
 */
@Component
public class SaasModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        Module module = new Module("saas", "SaaS");
        module.setIcon("globe");
        module.setPosition(-1);
        module.addPage(new CrudPage("accounts", "Accounts", Account.class).icon("network").featured(1));
        module.addPage(new CrudPage("accountRegions", "Regions", AccountRegion.class));
        module.addPage(new CrudPage("accountsType", "Accounts Type", AccountType.class).icon("list").featured(2));
        module.addPage(new CrudPage("accountProfile", "Profiles", AccountProfile.class).icon("diagram").featured(5));
        module.addPage(new CrudPage("accountPayments", "Payments", AccountPayment.class).icon("payment").featured(3));
        module.addPage(new CrudPage("accountPaymentsMethods", "Payments Methods", AccountPaymentMethod.class));
        module.addPage(new CrudPage("accountPaymentProviders", "Payments Providers", AccountPaymentProvider.class));
        module.addPage(new CrudPage("accountCategories", "Categories", AccountCategory.class));
        module.addPage(new CrudPage("accountResellers", "Resellers", AccountReseller.class).icon("users").featured(4));
        module.addPage(new CrudPage("accountChannels", "Sale Channels", AccountChannelSale.class).icon("tags"));

        return module;
    }

}
