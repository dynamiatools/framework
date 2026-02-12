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

package tools.dynamia.modules.saas.api;

import java.util.List;

/**
 * Interface for providing statistical information about an account.
 * <p>
 * This interface allows modules to contribute custom statistics and metrics
 * that can be displayed in account dashboards, reports, or administrative interfaces.
 * Multiple providers can coexist, each providing different types of statistics.
 * <p>
 * Statistics can include metrics such as:
 * <ul>
 *   <li>Usage statistics (storage, bandwidth, API calls)</li>
 *   <li>Business metrics (sales, customers, transactions)</li>
 *   <li>Activity metrics (login counts, active users)</li>
 *   <li>Performance metrics (response times, error rates)</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Component
 * public class UserActivityStatsProvider implements AccountStatsProvider {
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     public List<AccountStats> getAccountStats(Long accountId) {
 *         List<AccountStats> stats = new ArrayList<>();
 *         stats.add(new AccountStats("Total Users",
 *                 userRepository.countByAccountId(accountId)));
 *         stats.add(new AccountStats("Active Users",
 *                 userRepository.countActiveByAccountId(accountId)));
 *         return stats;
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountStatsProvider {

    /**
     * Retrieves a list of statistics for the specified account.
     * <p>
     * Implementations should gather and return relevant statistical information
     * for the given account. Each statistic should be represented as an {@link AccountStats}
     * object containing a label and value.
     *
     * @param accountId the unique identifier of the account
     * @return a list of {@link AccountStats} objects representing various metrics
     */
    List<AccountStats> getAccountStats(Long accountId);
}
