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
package tools.dynamia.app.lookups;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.ui.EntityLookup;
import tools.dynamia.ui.EntityLookups;

import java.util.List;
import java.util.Objects;
import java.util.Map;

/**
 * Lets a remote client search the {@link EntityLookup} named by a {@code CHOICE} step of {@code UIChoices.pickEntity}:
 * {@code GET /apps/api/lookups/{name}?q=text&limit=20} answers {@code [{key, label}]}. It only exposes what the lookup
 * returns, so the lookup is responsible for the tenant and the filters; the page is capped at
 * {@link EntityLookup#MAX_LIMIT}. An unknown lookup, or one that is not {@link EntityLookup#isSearchable() searchable}, is
 * {@code 404}.
 */
@RestController
@RequestMapping(LookupsController.PATH)
public class LookupsController {

    /** Base path of the endpoint. */
    public static final String PATH = "/apps/api/lookups";

    /** Page size when the client does not ask for one. */
    public static final int DEFAULT_LIMIT = 20;

    /**
     * Searches a lookup.
     *
     * @param name  the name of the lookup
     * @param query what the user typed
     * @param limit most results; clamped to 1..{@link EntityLookup#MAX_LIMIT}
     * @return the matches as {@code {key, label}}, or 404
     */
    @GetMapping(value = "/{name}", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> search(@PathVariable("name") String name,
                                                            @RequestParam(value = "q", defaultValue = "") String query,
                                                            @RequestParam(value = "limit", required = false) Integer limit) {
        var found = EntityLookups.find(name);
        if (found.isEmpty() || !found.get().isSearchable()) {
            return ResponseEntity.notFound().build();
        }
        int page = Math.max(1, Math.min(limit == null ? DEFAULT_LIMIT : limit, EntityLookup.MAX_LIMIT));
        var lookup = found.get();
        var results = lookup.search(query, page).stream()
                .limit(page)
                .map(value -> Map.of("key", Objects.toString(lookup.key(value), ""), "label", Objects.toString(lookup.label(value), "")))
                .toList();
        return ResponseEntity.ok(results);
    }
}
