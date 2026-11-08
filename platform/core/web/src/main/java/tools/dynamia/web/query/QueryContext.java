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
package tools.dynamia.web.query;

import tools.dynamia.viewers.ViewDescriptor;

/**
 * What a {@link QuerySpecFilter} may need to know about a request. The account of the caller is not here: read it from
 * {@code AccountTenants}, which every layer shares.
 *
 * @param path        virtual path of the page being queried
 * @param entityClass entity that is queried
 * @param descriptor  view descriptor whose fields are the only ones a query may use
 * @param operation   what the query is for
 */
public record QueryContext(String path, Class<?> entityClass, ViewDescriptor descriptor, QueryOperation operation) {
}
