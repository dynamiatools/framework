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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.ext.DynamicPropertied;
import org.zkoss.zk.ui.sys.ComponentCtrl;
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
 *     <li>{@link #MODE_AUTO}: for a bundle that self-mounts into a hardcoded selector at import
 *     time, as a default {@code vite build} of an app (not a custom-element library) does, e.g.
 *     {@code createApp(App).mount('#app')}. Selected automatically when {@link #setApp(String)} is
 *     used and neither {@link #setTag} nor {@link #setMountFn} is set: the {@code <body>} markup
 *     of the discovered {@code index.html} (its mount target element(s), stripped of any
 *     {@code <script>} tags) is copied into this component's container <em>before</em> the bundle
 *     loads, so the bundle's own bootstrap code finds its target and mounts itself, no
 *     {@link #setTag} or {@link #setMountFn} required. There is no unmount hook for this mode (the
 *     bundle exposes none), and it mounts only once — it does not support remounting into a new
 *     container on prop changes. Only one live instance of a given {@link #app} is allowed per
 *     page: its script runs once and self-mounts via a hardcoded id from its own index.html, so a
 *     second simultaneous instance would either duplicate that id or silently render nothing; it
 *     is refused with a console error instead. {@link #setProps(Map)}/dynamic props and
 *     {@code dynamiaEmit} are <strong>not</strong> delivered to the bundle in this mode: a
 *     self-mounting bundle has no defined channel to receive them, so it must fetch its own
 *     data/config independently. Use {@link #MODE_CUSTOM_ELEMENT} or {@link #MODE_MOUNT_FN} — both
 *     of which support multiple simultaneous instances cleanly — if the server needs to pass props
 *     in, receive events back, or embed the same app more than once on one page.</li>
 * </ul>
 * Any extra ZUL attribute not matching a bean property (via {@link DynamicPropertied}) is
 * forwarded as a prop to the mounted microfrontend, e.g. {@code userId="${user.id}"}.
 * <p>
 * {@link #setShadow(boolean)} mounts the bundle inside a
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_components/Using_shadow_DOM">shadow
 * root</a> attached to this component's container, isolating its CSS/DOM from the host ZK page in
 * both directions (host styles don't leak in, the microfrontend's styles don't leak out). Any
 * {@link #setCss} / auto-discovered stylesheet is loaded as an inline {@code <style>} inside the
 * shadow root instead of the document {@code <head>}. Supported with {@link #MODE_CUSTOM_ELEMENT}
 * (the created element and a {@code <style>} are appended to the shadow root) and
 * {@link #MODE_MOUNT_FN} ({@code mountFn} receives a plain {@code <div>} created inside the shadow
 * root instead of the container itself). <strong>Not</strong> supported with {@link #MODE_AUTO}:
 * the bundle finds its mount target via a page-level {@code document.getElementById(...)} call it
 * makes itself, which cannot see inside a shadow root by design — {@code shadow=true} with app
 * auto-discovery and no {@code tag}/{@code mountFn} is refused client-side with a console error.
 * <p>
 * Instead of {@link #setSrc} / {@link #setCss}, {@link #setApp(String)} points to the root folder
 * of a bundler's production build (e.g. Vite's {@code dist/} copied as-is into a static folder).
 * The browser fetches {@code {app}/index.html} and extracts the {@code <script type="module">}
 * and {@code <link rel="stylesheet">} tags it references, so hashed output filenames
 * (e.g. {@code assets/index-mU_jDp6H.js}) never need to be hardcoded. This relies on the
 * conventional single-entry {@code index.html} that Vite/Rollup/Webpack SPA builds produce; it
 * has only been verified against Vite output — check the generated {@code index.html} manually if
 * you use another bundler.
 * <p>
 * All bean properties ({@link #setSrc}, {@link #setMode}, {@link #setProps}, dynamic props, etc.)
 * are plain getter/setter pairs, so they work with ZK MVVM data binding out of the box, e.g.
 * {@code src="@bind(vm.bundleUrl)"} or {@code userId="@bind(vm.userId)"}. For the reverse
 * direction, the mounted microfrontend can call the {@code dynamiaEmit(data)} function injected
 * into its props to notify the server, which fires {@value #ON_EVENT} and can be bound with
 * {@code onMicrofrontendEvent="@command('handleIt', data=event.data)"}.
 *
 * Example:
 * <pre>{@code
 * <microfrontend src="/bundles/my-vue-app.js" css="/bundles/my-vue-app.css" tag="my-vue-app"
 *                userId="${user.id}" onMicrofrontendEvent="@command('handleIt', data=event.data)"/>
 *
 * <microfrontend app="/static/next/subscription" tag="my-vue-app" userId="${user.id}"/>
 *
 * <microfrontend app="/static/next/subscription" userId="${user.id}"/>
 *
 * <microfrontend src="/bundles/my-vue-app.js" css="/bundles/my-vue-app.css" mode="mount-fn"
 *                mountFn="mount" unmountFn="unmount" shadow="true"/>
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class MicroFrontend extends Div implements DynamicPropertied, AfterCompose {

    /** Mounts the bundle's custom element (default mode). */
    public static final String MODE_CUSTOM_ELEMENT = "custom-element";
    /** Mounts the bundle through global {@code window[mountFn]/window[unmountFn]} functions. */
    public static final String MODE_MOUNT_FN = "mount-fn";
    /** Mounts a self-bootstrapping {@link #setApp(String)} bundle by replicating its index.html body. */
    public static final String MODE_AUTO = "auto";
    /** Event fired when the mounted microfrontend calls the injected {@code dynamiaEmit(data)} function. */
    public static final String ON_EVENT = "onMicrofrontendEvent";
    /** Internal, server-only event used to coalesce bursts of {@link #mount()} calls; never sent by the client. */
    private static final String EVT_MOUNT = "onMicrofrontendMount";

    static {
        addClientEvent(MicroFrontend.class, ON_EVENT, ComponentCtrl.CE_IMPORTANT);
    }

    private String src;
    private String css;
    private String app;
    private String type = "module";
    private String mode = MODE_CUSTOM_ELEMENT;
    private String tag;
    private String mountFn;
    private String unmountFn;
    private final Map<String, Object> props = new LinkedHashMap<>();
    private boolean shadow;
    private boolean composed;
    private boolean mountScheduled;

    public MicroFrontend() {
        setSclass("microfrontend");
        addEventListener(EVT_MOUNT, event -> {
            mountScheduled = false;
            doMount();
        });
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

    public String getCss() {
        return css;
    }

    /**
     * Sets the stylesheet(s) to load alongside the bundle, e.g. a Vite/webpack build's separate
     * {@code index.css} chunk. Accepts a single URL or a comma-separated list, e.g.
     * {@code css="/bundles/app.css,/bundles/app-vendor.css"}. Loads are cached/shared client-side
     * the same way as {@link #setSrc}.
     *
     * @param css stylesheet URL(s), comma-separated
     */
    public void setCss(String css) {
        this.css = css;
        mount();
    }

    public String getApp() {
        return app;
    }

    /**
     * Sets the root folder of a bundler production build (e.g. Vite's {@code dist/}) to
     * auto-discover the bundle and stylesheet(s) from, instead of setting {@link #setSrc} /
     * {@link #setCss} manually. See the class Javadoc for how discovery works and its
     * limitations. When set, it takes precedence over {@link #src} / {@link #css}.
     *
     * @param app root URL of the build output, e.g. {@code /static/next/subscription}
     */
    public void setApp(String app) {
        this.app = app;
        mount();
    }

    public boolean isShadow() {
        return shadow;
    }

    /**
     * Sets whether to mount the bundle inside a shadow root attached to this component's
     * container, isolating its CSS/DOM from the host page. See the class Javadoc for supported
     * modes and limitations.
     *
     * @param shadow whether to use a shadow root
     */
    public void setShadow(boolean shadow) {
        this.shadow = shadow;
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
     * Schedules a (re)mount, coalescing every {@link #mount()} call made within the same request
     * into a single actual mount: MVVM binding resolves several {@code @bind}-ed properties one
     * setter call at a time (e.g. {@code theme}, then {@code shadow}, then {@code user}), each of
     * which would otherwise trigger its own flickering remount with a partially-updated
     * configuration. {@link #EVT_MOUNT} is posted to the current execution (processed after the
     * request's other handlers finish, still within the same response, no client round trip) where
     * {@link #doMount()} runs once with the final state. No-op until the component is attached to
     * a page and either {@link #app}, or {@link #src} plus the mode-specific requirements
     * ({@link #tag} or {@link #mountFn}), are set.
     */
    private void mount() {
        if (!composed || getPage() == null || (src == null && app == null) || !isMountable()) {
            return;
        }
        if (mountScheduled) {
            return;
        }
        mountScheduled = true;
        Events.postEvent(EVT_MOUNT, this, null);
    }

    private void doMount() {
        Clients.evalJavaScript("dynamiaMountMicrofrontend('" + getUuid() + "', " + buildConfig() + ");");
    }

    /**
     * Resolves the actual mounting strategy to use, defaulting {@link #setApp(String)} without a
     * {@link #tag}/{@link #mountFn} to {@link #MODE_AUTO} regardless of {@link #mode}'s default
     * value, so ZUL authors don't have to set {@code mode="auto"} explicitly.
     */
    private String effectiveMode() {
        if (MODE_MOUNT_FN.equals(mode) && mountFn != null) {
            return MODE_MOUNT_FN;
        }
        if (tag != null) {
            return MODE_CUSTOM_ELEMENT;
        }
        if (app != null) {
            return MODE_AUTO;
        }
        return mode;
    }

    private boolean isMountable() {
        String effective = effectiveMode();
        if (MODE_MOUNT_FN.equals(effective)) {
            return mountFn != null;
        }
        if (MODE_AUTO.equals(effective)) {
            return app != null;
        }
        return tag != null;
    }

    private String buildConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("src", src);
        config.put("css", css);
        config.put("app", app);
        config.put("type", type);
        config.put("mode", effectiveMode());
        config.put("tag", tag);
        config.put("mountFn", mountFn);
        config.put("unmountFn", unmountFn);
        config.put("shadow", shadow);
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
