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

package tools.dynamia.modules.saas.ui.controllers;

import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountFeatureProvider;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountFeature;
import tools.dynamia.zk.crud.SubcrudController;

import java.util.ArrayList;
import java.util.List;

public class AccountFeatureCrudController extends SubcrudController<AccountFeature> {

    public AccountFeatureCrudController(Object parent, String parentName, String childrenName) {
        super(parent, parentName, childrenName);
    }

    public AccountFeatureCrudController(Class<AccountFeature> entityClass, Object parent, String parentName, String childrenName) {
        super(entityClass, parent, parentName, childrenName);
    }

    @Override
    public void query() {
        List<AccountFeature> result = new ArrayList<>();
        Containers.get().findObjects(AccountFeatureProvider.class).forEach(p -> {
            AccountFeature feature = crudService.findSingle(AccountFeature.class, QueryParameters.with("account", getParentEntity())
                    .add("providerId", QueryConditions.eq(p.getId())));
            if (feature == null) {
                feature = new AccountFeature();
                feature.setAccount((Account) getParentEntity());
                feature.setProviderId(p.getId());
                feature.setName(p.getName());
                feature.save();
            }

            result.add(feature);

        });
        setQueryResult(result);
    }
}
