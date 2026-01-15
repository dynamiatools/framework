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

package tools.dynamia.modules.saas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.dynamia.domain.AutoEvictEntityCacheCrudListener;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.modules.saas.domain.*;

import java.util.List;

@Configuration
public class AccountConfig {

    public static final String CACHE_NAME = "saas";

    static {
        AutoEvictEntityCacheCrudListener.register(CACHE_NAME, Account.class, account -> List.of(
                "Account-" + account.getId(),
                "AccountDTO-" + account.getId(),
                "AccountByDomain-" + account.getSubdomain(),
                "AccountIdByDomain-" + account.getSubdomain(),
                "AccountByCustomDomain-" + account.getCustomDomain(),
                "AccountIdByCustomDomain-" + account.getCustomDomain(),
                "AccountByName-" + account.getName(),
                "AccountsDetails-" + account.getId(),
                "AccountStatus-" + account.getId()
        ));
    }

    @Bean
    public EntityReferenceRepository<Long> accountReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(Account.class, "name");
        repo.setCacheable(true);
        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> accountProfileReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(AccountProfile.class, "name");
        repo.setCacheable(true);
        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> accountTypeReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(AccountType.class, "name");
        repo.setCacheable(true);
        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> accountChannelReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(AccountChannelSale.class, "name");
        repo.setCacheable(true);
        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> accountResellerReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(AccountReseller.class, "name");
        repo.setCacheable(true);
        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> accountCategoryReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(AccountCategory.class, "name");
        repo.setCacheable(true);
        return repo;
    }

}
