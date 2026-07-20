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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.ext.DynamicPropertied;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import tools.dynamia.commons.StringPojoParser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ZK component that embeds an external JavaScript bundle ("microfrontend") built with any
 * framework (Vue, React, Svelte, plain JS, etc.) inside a ZUL page.
 * <p>
 * The bundle is loaded once per {@code src} (loads are cached/shared client-side across every
 * {@code MicroFrontend} instance on the page) and mounted using one of two strategies, selected
 * with {@link #setMode(String)}:
 * <ul>
 *     <li>{@link #MODE_CUSTOM_ELEMENT} (default): the bundle registers a
 *     <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_components">Custom Element</a>
 *     (e.g. via {@code customElements.define(...)}, or Vue's {@code defineCustomElement}). This
 *     component creates that element, assigns {@link #setProps(Map)} as properties/attributes and
 *     appends it to its container. The browser mounts/unmounts it automatically through the
 *     element's own {@code connectedCallback}/{@code disconnectedCallback}.</li>
 *     <li>{@link #MODE_MOUNT_FN}: the bundle exposes global mount/unmount functions (a convention
 *     similar to single-spa), invoked explicitly as {@code window[mountFn](container, props)} and
 *     {@code window[unmountFn](container)} when this component is rendered and detached.</li>
 * </ul>
 * Any extra ZUL attribute not matching a bean property (via {@link DynamicPropertied}) is
 * forwarded as a prop to the mounted microfrontend, e.g. {@code userId="${user.id}"}.
 *
 * Example:
 * <pre>{@code
 * <microfrontend src="/bundles/my-vue-app.js" tag="my-vue-app" userId="${user.id}"/>
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class MicroFrontend extends Div implements DynamicPropertied, AfterCompose {

    /** Mounts the bundle's custom element (default mode). */
    public static final String MODE_CUSTOM_ELEMENT = "custom-element";
    /** Mounts the bundle through global {@code window[mountFn]/window[unmountFn]} functions. */
    public static final String MODE_MOUNT_FN = "mount-fn";

    private String src;
    private String type = "module";
    private String mode = MODE_CUSTOM_ELEMENT;
    private String tag;
    private String mountFn;
    private String unmountFn;
    private final Map<String, Object> props = new LinkedHashMap<>();
    private boolean composed;

    public MicroFrontend() {
        setSclass("microfrontend");
    }

    /**
     * Mounts the microfrontend once the component tree is composed. Property setters invoked
     * while the ZUL page is still being parsed (e.g. {@code src}, {@code tag}, dynamic props such
     * as {@code userId}) call {@link #mount()} eagerly to support runtime changes, but they are
     * no-ops until this flag is set, so the initial mount happens exactly once, with every
     * attribute already applied.
     */
    @Override
    public void afterCompose() {
        composed = true;
        mount();
    }

    public String getSrc() {
        return src;
    }

    /**
     * Sets the URL of the JavaScript bundle to load, e.g. {@code /bundles/my-app.js}, and
     * (re)mounts the microfrontend.
     *
     * @param src bundle URL
     */
    public void setSrc(String src) {
        this.src = src;
        mount();
    }

    public String getType() {
        return type;
    }

    /**
     * Sets the {@code <script type="...">} used to load the bundle. Defaults to {@code module}.
     *
     * @param type script type, e.g. {@code module} or {@code text/javascript}
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    /**
     * Sets the mounting strategy: {@link #MODE_CUSTOM_ELEMENT} or {@link #MODE_MOUNT_FN}.
     *
     * @param mode mounting mode
     */
    public void setMode(String mode) {
        this.mode = mode;
        mount();
    }

    public String getTag() {
        return tag;
    }

    /**
     * Sets the custom element tag name to create, required when {@link #MODE_CUSTOM_ELEMENT} is
     * used, e.g. {@code my-vue-app}.
     *
     * @param tag custom element tag name
     */
    public void setTag(String tag) {
        this.tag = tag;
        mount();
    }

    public String getMountFn() {
        return mountFn;
    }

    /**
     * Sets the name of the global function used to mount the bundle, required when
     * {@link #MODE_MOUNT_FN} is used. Invoked as {@code window[mountFn](container, props)}.
     *
     * @param mountFn global mount function name
     */
    public void setMountFn(String mountFn) {
        this.mountFn = mountFn;
        mount();
    }

    public String getUnmountFn() {
        return unmountFn;
    }

    /**
     * Sets the name of the global function used to unmount the bundle, required when
     * {@link #MODE_MOUNT_FN} is used. Invoked as {@code window[unmountFn](container)} when this
     * component is detached from the page.
     *
     * @param unmountFn global unmount function name
     */
    public void setUnmountFn(String unmountFn) {
        this.unmountFn = unmountFn;
    }

    public Map<String, Object> getProps() {
        return props;
    }

    /**
     * Replaces all props passed to the mounted microfrontend and remounts it.
     *
     * @param props props map, must be JSON-serializable
     */
    public void setProps(Map<String, Object> props) {
        this.props.clear();
        if (props != null) {
            this.props.putAll(props);
        }
        mount();
    }

    /**
     * Adds or replaces a single prop passed to the mounted microfrontend and remounts it.
     *
     * @param name  prop name
     * @param value prop value, must be JSON-serializable
     */
    public void addProp(String name, Object value) {
        props.put(name, value);
        mount();
    }

    /**
     * Builds the client-side configuration and instructs the browser to load (if not already
     * cached) the bundle and mount it into this component's container element. No-op until the
     * component is attached to a page and {@link #src} plus the mode-specific requirements
     * ({@link #tag} or {@link #mountFn}) are set.
     */
    private void mount() {
        if (!composed || getPage() == null || src == null || !isMountable()) {
            return;
        }
        Clients.evalJavaScript("dynamiaMountMicrofrontend('" + getUuid() + "', " + buildConfig() + ");");
    }

    private boolean isMountable() {
        if (MODE_CUSTOM_ELEMENT.equals(mode)) {
            return tag != null;
        }
        return mountFn != null;
    }

    private String buildConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("src", src);
        config.put("type", type);
        config.put("mode", mode);
        config.put("tag", tag);
        config.put("mountFn", mountFn);
        config.put("unmountFn", unmountFn);
        config.put("props", props);
        return StringPojoParser.convertMapToJson(config);
    }

    /**
     * Unmounts the microfrontend client-side when this component leaves the page, preventing
     * memory leaks from mounted framework instances (Vue apps, React roots, subscriptions, etc.)
     * when using {@link #MODE_MOUNT_FN}. Components using {@link #MODE_CUSTOM_ELEMENT} are cleaned
     * up automatically by the browser through the custom element's {@code disconnectedCallback}.
     *
     * @param page the page this component is detached from
     */
    @Override
    public void onPageDetached(Page page) {
        if (MODE_MOUNT_FN.equals(mode) && unmountFn != null) {
            Clients.evalJavaScript("dynamiaUnmountMicrofrontend('" + getUuid() + "', " + buildConfig() + ");");
        }
        super.onPageDetached(page);
    }

    @Override
    public boolean hasDynamicProperty(String name) {
        return props.containsKey(name);
    }

    @Override
    public Object getDynamicProperty(String name) {
        return props.get(name);
    }

    /**
     * Forwards any extra ZUL attribute not matching a bean property as a prop for the mounted
     * microfrontend.
     *
     * @param name  prop name
     * @param value prop value, must be JSON-serializable
     */
    @Override
    public void setDynamicProperty(String name, Object value) throws WrongValueException {
        props.put(name, value);
        mount();
    }
}
