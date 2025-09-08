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
package tools.dynamia.navigation;

/**
 * Abstract extension of {@link Page} for pages that can be rendered to a specific type.
 * <p>
 * Subclasses must implement {@link #renderPage()} to provide rendering logic for the page content.
 * </p>
 *
 * @param <T> the type of the rendered page content
 */
public abstract class RendereablePage<T> extends Page {

    /**
     * Default constructor.
     */
    public RendereablePage() {
        super();
    }

    /**
     * Constructs a RendereablePage with id, name, path, and closable flag.
     *
     * @param id the page id
     * @param name the page name
     * @param path the content path
     * @param closeable whether the page can be closed
     */
    public RendereablePage(String id, String name, String path, boolean closeable) {
        super(id, name, path, closeable);
    }

    /**
     * Constructs a RendereablePage with id, name, and path.
     *
     * @param id the page id
     * @param name the page name
     * @param path the content path
     */
    public RendereablePage(String id, String name, String path) {
        super(id, name, path);
    }

    /**
     * Renders the page content to the specified type.
     *
     * @return the rendered page content of type T
     */
    public abstract T renderPage();

}
