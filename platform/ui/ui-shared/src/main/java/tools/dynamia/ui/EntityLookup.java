/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.ui;

import java.util.List;
import java.util.Optional;

/**
 * A named, server side source of records the user can search and pick (customers, items...), for catalogs too big to send
 * as a list of options. It is declared as a bean, so a remote client can search it through
 * {@code GET /apps/api/lookups/{name}} while the action waits for the answer.
 *
 * <p><strong>Security contract.</strong> {@link #search} and {@link #find} must apply the same filters and the same tenant
 * rules (use the tenant aware {@code CrudService}). Every pass of a headless run calls {@link #find} again with the key the
 * client sent, so a key that is not in the searchable set is refused.
 *
 * <pre>{@code
 * @Component
 * class ActiveCustomers implements EntityLookup<Customer> {
 *     public String name() { return "customers.active"; }
 *     public List<Customer> search(String text, int limit) { ... }
 *     public Optional<Customer> find(String key) { ... }
 *     public String key(Customer c) { return String.valueOf(c.getId()); }
 *     public String label(Customer c) { return c.getName(); }
 * }
 * UIChoices.pickEntity("customers.active", "Customer", customer -> ...);
 * }</pre>
 *
 * @param <T> the record type
 */
public interface EntityLookup<T> {

    /** Largest page {@code search} is asked for. */
    int MAX_LIMIT = 50;

    /**
     * @return the unique name clients use to search this lookup
     */
    String name();

    /**
     * Searches the records the user may pick.
     *
     * @param text  what the user typed; may be empty
     * @param limit most records to return, at most {@link #MAX_LIMIT}
     * @return the matches
     */
    List<T> search(String text, int limit);

    /**
     * Finds one pickable record by its key. Empty when it does not exist or is not in the searchable set.
     *
     * @param key the key given by {@link #key(Object)}
     * @return the record, if it can be picked
     */
    Optional<T> find(String key);

    /**
     * @param value a record
     * @return its stable key
     */
    String key(T value);

    /**
     * @param value a record
     * @return what to show for it
     */
    String label(T value);
}
