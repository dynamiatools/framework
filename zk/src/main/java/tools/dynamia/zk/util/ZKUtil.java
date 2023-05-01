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
package tools.dynamia.zk.util;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zhtml.impl.AbstractTag;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;
import org.zkoss.zul.ext.Paginal;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.LabelImageElement;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.EventQueueSubscriber;
import tools.dynamia.zk.navigation.ZKNavigationManager;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.ui.InputPanel;
import tools.dynamia.zk.ui.MessageDialog;
import tools.dynamia.zk.ui.SimpleListItemRenderer;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility class for common ZK process
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ZKUtil {

    private static final LoggingService LOGGER = new SLF4JLoggingService(ZKUtil.class);

    private static final String PARENT_WINDOW = "parentWindow";
    private static final String ENTITY = "entity";
    public static final String NAVIGATION_PAGE = "navigationPage";


    private ZKUtil() {
    }

    /**
     * The Constant YES.
     */
    public static final int YES = Messagebox.YES;

    /**
     * The Constant NO.
     */
    public static final int NO = Messagebox.NO;

    /**
     * show a message.
     *
     * @param message the message
     */
    public static void showMessage(String message) {
        showMessage(message, "Mensaje", MessageType.NORMAL);
    }

    /**
     * show a message by type.
     *
     * @param message the message
     * @param type    the type
     */
    public static void showMessage(String message, MessageType type) {
        showMessage(message, "Message", type);

    }

    /**
     * Show message.
     *
     * @param message the message
     * @param title   the title
     * @param type    the type
     */
    public static void showMessage(String message, String title, MessageType type) {

        MessageDisplayer displayer = Containers.get().findObject(MessageDisplayer.class);
        if (displayer == null) {
            displayer = new MessageDialog();
        }

        displayer.showMessage(message, title, type);

    }

    /**
     * Show question.
     *
     * @param question the question
     * @param title    the title
     * @param listener the listener
     */
    public static void showQuestion(String question, String title, EventListener<Messagebox.ClickEvent> listener) {
        Messagebox.Button[] buttons = {Messagebox.Button.YES, Messagebox.Button.NO};
        Messagebox.show(question, title, buttons, Messagebox.QUESTION, listener);
    }

    /**
     * Fill combobox.
     *
     * @param combo the combo
     * @param data  the data
     * @param live  the live
     */
    public static void fillCombobox(Combobox combo, Collection data, boolean live) {
        fillCombobox(combo, data, null, live);
    }

    /**
     * Fill combobox.
     *
     * @param combo    the combo
     * @param data     the data
     * @param selected the selected
     * @param live     the live
     */
    public static void fillCombobox(Combobox combo, Collection data, Object selected, boolean live) {
        if (combo != null) {
            ListModelList model = new ListModelList<>(data);
            if (selected != null) {
                model.addToSelection(selected);
            }
            combo.setModel(model);
        }
    }

    /**
     * Fill combobox.
     *
     * @param combo the combo
     * @param data  the data
     */
    public static void fillCombobox(Combobox combo, List data) {
        fillCombobox(combo, data, true);
    }

    /**
     * Fill combobox.
     *
     * @param combo the combo
     * @param data  the data
     * @param live  the live
     */
    public static void fillCombobox(Combobox combo, List data, boolean live) {
        fillCombobox(combo, data, null, live);
    }

    /**
     * Fill combobox.
     *
     * @param combo the combo
     * @param data  the data
     * @param live  the live
     */
    public static void fillCombobox(Combobox combo, Object[] data, boolean live) {
        if (combo != null) {
            List dataList = Arrays.asList(data);
            fillCombobox(combo, dataList, live);
        }
    }

    /**
     * Fill listbox.
     *
     * @param listbox the listbox
     * @param data    the data
     * @param live    the live
     */
    public static void fillListbox(Listbox listbox, Collection data, boolean live) {
        if (listbox != null && data != null) {
            listbox.setModel((ListModel) null);
            if (data instanceof List) {
                listbox.setModel(new ListModelList<>((List) data, live));
            } else {
                listbox.setModel(new ListModelList<>(new ArrayList(data), live));
            }
        }
    }

    /**
     * Fill listbox.
     *
     * @param listbox the listbox
     * @param data    the data
     * @param live    the live
     */
    public static void fillListbox(Listbox listbox, List data, boolean live) {
        if (listbox != null && data != null) {
            listbox.setModel((ListModel) null);
            listbox.setModel(new ListModelList<>(data, live));
        }
    }

    /**
     * Fill listbox.
     *
     * @param listbox the listbox
     * @param data    the data
     * @param live    the live
     */
    public static void fillListbox(Listbox listbox, Object[] data, boolean live) {
        if (listbox != null && data != null) {
            List dataList = Arrays.asList(data);
            fillListbox(listbox, dataList, live);
        }
    }

    /**
     * clean all input components in the page.
     *
     * @param page the page
     */
    public static void clearPage(Page page) {
        if (page != null) {
            Component comp = page.getFirstRoot();
            clearComponent(comp);
        }
    }

    /**
     * clear the component value.
     *
     * @param comp the comp
     */
    public static void clearComponent(Component comp) {

        for (Object object : comp.getChildren()) {
            try {
                clearComponent((Component) object);
            } catch (Exception e) {
            }
        }
        if (comp instanceof InputElement ie) {
            ie.setRawValue(null);
        }
    }

    /**
     * Show dialog.
     *
     * @param uri    the uri
     * @param title  the title
     * @param data   the data
     * @param height the height
     * @param width  the width
     */
    public static Window showDialog(String uri, String title, Object data, String width, String height) {
        return showDialog(uri, title, data, width, height, null);
    }

    /**
     * Show dialog.
     *
     * @param uri    the uri
     * @param title  the title
     * @param width  the width
     * @param height the height
     * @return the window
     */
    public static Window showDialog(String uri, String title, String width, String height) {
        return showDialog(uri, title, null, width, height, null);
    }

    /**
     * Show dialog.
     *
     * @param uri             the uri
     * @param title           the title
     * @param data            the data
     * @param height          the height
     * @param width           the width
     * @param onCloseListener the on close listener
     * @return the window
     */
    public static Window showDialog(String uri, String title, Object data, String width, String height,
                                    EventListener onCloseListener) {

        final Window dialog = createWindow(uri, title, data);

        if (onCloseListener != null) {
            dialog.addEventListener(Events.ON_CLOSE, onCloseListener);
        }
        if (height != null) {
            dialog.setHeight(height);
        }

        if (width != null) {
            dialog.setWidth(width);
        }

        if (HttpUtils.isSmartphone()) {
            dialog.setWidth("99%");
            dialog.setHeight("99%");
            dialog.setDraggable("false");
        }


        dialog.doModal();

        return dialog;

    }

    /**
     * Creates the window.
     *
     * @param uri   the uri
     * @param title the title
     * @param data  the data
     * @return the window
     */
    public static Window createWindow(String uri, String title, Object data) {
        final Window dialog = new Window();
        Caption caption = new Caption(title);
        caption.setIconSclass("fa fa-dot-circle");
        dialog.appendChild(caption);
        dialog.setClosable(true);
        dialog.setBorder("normal");


        dialog.setPage(getFirstPage());
        if (uri != null) {

            Map args = MapBuilder.put("dialog", dialog, "result", data, "data", data);

            if (data instanceof Map map) {
                for (Object key : map.keySet()) {
                    args.put(key.toString(), map.get(key));
                }
            }

            if (DomainUtils.isEntity(data)) {
                args.put(ENTITY, data);
            }

            args.put(PARENT_WINDOW, dialog);

            Component component = createComponent(uri, dialog, args);
            if (component instanceof Include) {
                ((Include) component).setVflex("1");
            }

        }

        dialog.addEventListener(Events.ON_CANCEL, e -> {
            if (dialog.isClosable()) {
                dialog.detach();
            }
        });

        return dialog;
    }

    /**
     * Creates the window.
     *
     * @param title the title
     * @return the window
     */
    public static Window createWindow(String title) {
        return createWindow(null, title, null);
    }

    /**
     * Show dialog.
     *
     * @param uri   the uri
     * @param title the title
     * @param data  the data
     */
    public static Window showDialog(String uri, String title, Object data) {
        return showDialog(uri, title, data, null, null);
    }

    /**
     * Show dialog.
     *
     * @param uri   the uri
     * @param title the title
     */
    public static Window showDialog(String uri, String title) {
        return showDialog(uri, title, null, null, null);
    }

    /**
     * Show dialog.
     *
     * @param title     the title
     * @param component the component
     */
    public static Window showDialog(String title, Component component) {
        return showDialog(title, component, null, null);
    }

    /**
     * Show dialog.
     *
     * @param title     the title
     * @param component the component
     * @param width     the width
     * @param height    the height
     */
    public static Window showDialog(String title, Component component, String width, String height) {
        return showDialog(title, component, width, height, null);
    }

    public static Window showDialog(String title, Component component, String width, String height,
                                    EventListener<Event> onCloseListener) {
        try {

            Window dialog = createWindow(title);

            dialog.setMaximizable(true);

            if (onCloseListener != null) {
                dialog.addEventListener(Events.ON_CLOSE, onCloseListener);
            }


            dialog.setWidth("80%");
            if (width != null) {
                dialog.setWidth(width);
            }

            if (height != null) {
                dialog.setHeight(height);
            }

            if (HttpUtils.isSmartphone()) {
                dialog.setWidth("99%");
                dialog.setHeight("99%");
                dialog.setDraggable("false");
            }

            component.setParent(dialog);

            if (component instanceof HtmlBasedComponent hcomp && height != null) {
                if (hcomp.getVflex() == null) {
                    hcomp.setVflex("1");
                }
            }
            dialog.doModal();
            return dialog;
        } catch (Exception ex) {
            LOGGER.error("Error showing Window dialog", ex);
            return null;
        }
    }

    /**
     * Show a simple InputPanel
     *
     * @param label
     * @param inputClass
     * @param value
     * @param eventListener
     */
    public static <T> InputPanel showInputDialog(String label, Class<T> inputClass, Object value,
                                                 EventListener eventListener) {
        InputPanel inputPanel = new InputPanel(label, value, inputClass);
        inputPanel.addEventListener(InputPanel.ON_INPUT, eventListener);
        inputPanel.showDialog();
        return inputPanel;
    }

    /**
     * Show a simple InputPanel.
     *
     * @param <T>           the generic type
     * @param label         the label
     * @param inputClass    the input class
     * @param eventListener the event listener
     */
    public static <T> InputPanel showInputDialog(String label, Class<T> inputClass, EventListener eventListener) {
        return showInputDialog(label, inputClass, null, eventListener);
    }

    public static InputPanel showInputPassword(String label, Consumer<String> inputPassword) {
        var inputPanel = showInputDialog(label, String.class, event -> {
            var password = (String) event.getData();
            if (password != null && !password.isBlank()) {
                inputPassword.accept(password);
            }
        });

        ((Textbox) inputPanel.getTextbox()).setType("password");

        return inputPanel;
    }

    /**
     * Checks if is empty.
     *
     * @param input the input
     * @return true, if is empty
     */
    public static boolean isEmpty(InputElement input) {
        return input.getText() == null || input.getText().isEmpty();
    }

    /**
     * Synchronize paginator.
     *
     * @param dataPaginator the data paginator
     * @param paginal       the paginal
     */
    public static void synchronizePaginator(DataPaginator dataPaginator, Paginal paginal) {
        if (dataPaginator != null && paginal != null) {
            dataPaginator.setPage(paginal.getActivePage() + 1);
            dataPaginator.setPageSize(paginal.getPageSize());
            paginal.setTotalSize((int) dataPaginator.getTotalSize());
        }
    }

    /**
     * Gets the first page.
     *
     * @return the first page
     */
    public static Page getFirstPage() {
        Desktop desktop = Executions.getCurrent().getDesktop();
        return desktop.getFirstPage();
    }

    /**
     * Show popup.
     *
     * @param refComponent     the ref component
     * @param contentComponent the content component
     * @param width            the width
     * @param height           the height
     * @return the popup
     */
    public static Popup showPopup(Component refComponent, Component contentComponent, String width, String height) {
        Popup popup = new Popup();
        popup.setPage(refComponent.getPage());
        if (width != null) {
            popup.setWidth(width);
        }
        if (height != null) {
            popup.setHeight(height);
        }
        contentComponent.setParent(popup);
        popup.open(refComponent);

        return popup;

    }

    /**
     * Show popup.
     *
     * @param refComponent     the ref component
     * @param contentComponent the content component
     * @return the popup
     */
    public static Popup showPopup(Component refComponent, Component contentComponent) {
        return showPopup(refComponent, contentComponent, null, null);
    }

    /**
     * Gets the session id.
     *
     * @return the session id
     */
    public static String getSessionID() {
        HttpSession session = (HttpSession) Executions.getCurrent().getSession().getNativeSession();
        return session.getId();
    }

    /**
     * Creates the ajax loader.
     *
     * @param message   the message
     * @param imagePath the image path
     * @return the component
     */
    public static Component createAjaxLoader(String message, String imagePath) {
        Vbox div = new Vbox();
        div.setWidth("100%");
        div.setHeight("100%");
        div.setPack("center");
        div.setAlign("center");
        div.setSclass("ajax-loader");

        Vbox box = new Vbox();
        box.setPack("center");
        box.setAlign("center");
        div.appendChild(box);

        if (imagePath == null) {
            imagePath = "/zkau/web/tools/images/ajax-loader.gif";
        }

        Image img = new Image(imagePath);
        Label lbl = new Label(message);
        lbl.setStyle("font-weight:bold");

        box.appendChild(img);
        box.appendChild(lbl);
        return div;
    }

    /**
     * Creates the ajax loader.
     *
     * @return the component
     */
    public static Component createAjaxLoader() {
        return createAjaxLoader("", null);
    }

    /**
     * Creates the ajax loader.
     *
     * @param message the message
     * @return the component
     */
    public static Component createAjaxLoader(String message) {
        return createAjaxLoader(message, null);
    }

    /**
     * Create a tooltip popup with title and description.
     *
     * @param title       the title
     * @param description the description
     * @return the popup
     */
    public static Popup createTooltip(String title, String description) {
        Popup popup = new Popup();
        Vlayout layout = new Vlayout();
        popup.appendChild(layout);
        Label lbltitle = new Label(title);
        layout.appendChild(lbltitle);
        if (description != null && !description.isEmpty()) {
            layout.appendChild(new Label(description));
            lbltitle.setStyle("font-weight:bold");
        }

        return popup;
    }

    /**
     * Create a tootip popup.
     *
     * @param text the text
     * @return the popup
     */
    public static Popup createTooltip(String text) {
        return createTooltip(text, null);
    }

    /**
     * Configure the component icon, its takes care of IMAGE and FONT type
     * icons.
     *
     * @param icon      the icon
     * @param component the component
     * @param size      the size
     */
    public static void configureComponentIcon(Icon icon, Component component, IconSize size) {

        if (icon == null) {
            return;
        }

        String realPath = icon.getRealPath(component, size);

        if (component instanceof LabelImageElement) {

            LabelImageElement element = (LabelImageElement) component;
            switch (icon.getType()) {
                case IMAGE:
                    element.setImage(realPath);
                    break;
                case FONT:
                    element.setIconSclass(realPath);
                    break;
            }
        } else if (component instanceof AbstractTag) {
            AbstractTag element = (AbstractTag) component;
            switch (icon.getType()) {
                case IMAGE:
                    Image img = new Image(realPath);
                    img.setParent(component);
                    break;
                case FONT:
                    element.setSclass(realPath);
                    break;
            }
        } else if (component instanceof Image image && icon.getType() == IconType.IMAGE) {
            image.setSrc(realPath);
        }
    }

    /**
     * Configure the component icon for the Action, its takes care of IMAGE and
     * FONT type icons.
     *
     * @param image     the action
     * @param component the component
     * @param size      the size
     */
    public static void configureComponentIcon(String image, Component component, IconSize size) {
        configureComponentIcon(IconsTheme.get().getIcon(image), component, size);
    }

    /**
     * Creates the component.
     *
     * @param uri    the uri
     * @param parent the parent
     * @param args   the args
     */
    public static Component createComponent(String uri, Component parent, Map<?, ?> args) {
        if (isResourceURI(uri)) {
            Resource resource = getDeviceResource(uri);
            if (resource == null) {
                throw new UiException("Resource for page not found " + uri);
            }


            try {
                if (resource.exists()) {
                    return Executions.createComponentsDirectly(new InputStreamReader(resource.getInputStream()),
                            resource.getFileExtension(), parent, args);
                } else {
                    throw new UiException("Resource for page not exists " + uri);
                }
            } catch (IOException e) {
                throw new UiException("Error creating component directly from resource" + resource, e);
            }
        } else {
            Include include = new Include(uri);
            include.setParent(parent);

            if (args != null) {
                for (Object key : args.keySet()) {
                    include.setAttribute(key.toString(), args.get(key));
                }
            }
            return include;
        }
    }

    private static Resource getDeviceResource(String uri) {
        Resource resource = null;
        String device = HttpUtils.detectDevice();
        String deviceUri = uri;
        if (!HttpUtils.DEVICE_SCREEN.equals(device)) {
            String ext = "." + StringUtils.getFilenameExtension(uri);
            if (deviceUri.endsWith(ext)) {
                deviceUri = deviceUri.replace(ext, "." + device.toLowerCase() + ext);
            }

            resource = IOUtils.getResource(deviceUri);
            if (resource == null || !resource.exists()) {
                deviceUri = uri;
                resource = null;
            }
        }

        if (resource == null) {
            resource = IOUtils.getResource(deviceUri);
        }

        return resource;
    }

    private static boolean isResourceURI(String uri) {
        return uri.startsWith("classpath:") || uri.startsWith("file:") || uri.startsWith("http:");
    }

    public static void initEventQueueSubscribers(Object target) {
        new EventQueueSubscriber(target).loadAnnotations();
    }

    public static void eventQueuePublish(String name, String scope, boolean autocreate, Event evt) {
        EventQueues.lookup(name, scope, autocreate).publish(evt);
    }

    public static void eventQueuePublish(String name, Event evt) {
        eventQueuePublish(name, EventQueues.DESKTOP, true, evt);
    }

    public static boolean isInEventListener() {
        return Executions.getCurrent() != null;
    }

    public static Window showListboxSelector(String title, List model, EventListener<SelectEvent> onSelect) {
        Listbox listbox = new Listbox();
        listbox.setVflex("1");
        listbox.setHflex("1");
        listbox.setItemRenderer(new SimpleListItemRenderer());
        fillListbox(listbox, model, true);
        Window win = showDialog(title, listbox, "500px", "500px");
        listbox.addEventListener(Events.ON_SELECT, (SelectEvent evt) -> {
            win.detach();

            onSelect.onEvent(evt);
        });
        return win;
    }

    public static Window showListboxMultiSelector(String title, String label, List model,
                                                  EventListener<Event> onSelect) {
        Listbox listbox = new Listbox();
        listbox.setVflex("1");
        // listbox.setHflex("1");
        listbox.setWidth("100%");
        listbox.setCheckmark(true);
        listbox.setItemRenderer(new SimpleListItemRenderer());

        fillListbox(listbox, model, true);
        listbox.setMultiple(true);

        Window win = showDialog(title, listbox, "500px", "500px");
        Button btn = new Button(label);
        win.appendChild(btn);
        btn.addEventListener(Events.ON_CLICK, evt -> {
            win.detach();
            if (listbox.getSelectedCount() > 0) {
                List selectedObjects = listbox.getSelectedItems().stream().map(Listitem::getValue)
                        .collect(Collectors.toList());
                onSelect.onEvent(new Event(Events.ON_SELECT, listbox, selectedObjects));
            }
        });
        return win;
    }

    public static void showTextInputDialog(String title, String buttonLabel, EventListener<Event> evt) {
        Vlayout vlayout = new Vlayout();


        Textbox textbox = new Textbox();
        textbox.setMultiline(true);
        textbox.setWidth("100%");
        textbox.setHeight("100px");
        vlayout.appendChild(textbox);

        Button btn = new Button(buttonLabel);
        btn.setAutodisable("self");
        btn.setStyle("float: right");
        vlayout.appendChild(btn);

        Window win = ZKUtil.showDialog(title, vlayout);
        win.setWidth("500px");
        btn.addEventListener(Events.ON_CLICK, e -> {
            Event newEvt = new Event(Events.ON_CLICK, vlayout, textbox.getValue());
            evt.onEvent(newEvt);

            if (newEvt.isPropagatable()) {
                win.detach();
            }

        });

    }

    /**
     * Make easy select a combobox item
     *
     * @param combobox
     * @param value
     */
    public static void setSelected(Combobox combobox, Object value) {
        if (combobox != null) {

            if (value == null) {
                combobox.setSelectedItem(null);
            } else if (combobox.getModel() instanceof ListModelList) {
                ((ListModelList<Object>) combobox.getModel()).addToSelection(value);
            }
        }
    }

    /***
     * Make easy selecte a listbox item
     * @param listbox
     * @param value
     */
    public static void setSelected(Listbox listbox, Object value) {
        if (listbox != null) {
            if (value == null) {
                listbox.setSelectedItem(null);
            } else if (listbox.getModel() instanceof ListModelList) {
                ((ListModelList<Object>) listbox.getModel()).addToSelection(value);
            }
        }
    }

    /**
     * Return and argument from current execution
     *
     * @param name
     * @return
     */
    public static Object getExecutionArg(String name) {
        return Executions.getCurrent().getArg().get(name);
    }

    /**
     * Return entity binding to current zk execution (or event)
     *
     * @return
     */
    public static Object getExecutionEntity() {
        return getExecutionArg(ENTITY);
    }

    /**
     * Return parent {@link Window} binding to current zk execution (or event)
     *
     * @return
     */
    public static Window getExecutionParentWindow() {
        return (Window) getExecutionArg(PARENT_WINDOW);
    }

    /**
     * Return current navigation {@link tools.dynamia.navigation.Page} binded to current execution or currentPage from {@link tools.dynamia.navigation.NavigationManager}
     *
     * @return
     */
    public static tools.dynamia.navigation.Page getExecutionNavigationPage() {
        var page = (tools.dynamia.navigation.Page) getExecutionArg(NAVIGATION_PAGE);
        if (page == null) {
            page = ZKNavigationManager.getInstance().getCurrentPage();
        }
        return page;
    }

    /**
     * Automatic change component to read only or disabled. Include children component
     *
     * @param comp
     * @param readOnly
     */
    public static void changeReadOnly(Component comp, boolean readOnly) {

        if (comp instanceof Checkbox checkbox) {
            checkbox.setDisabled(readOnly);
        }

        if (comp instanceof InputElement input) {
            input.setReadonly(readOnly);
        }

        if (comp instanceof CanBeReadonly) {
            ((CanBeReadonly) comp).setReadonly(readOnly);
        }

        if (comp instanceof Button button) {
            button.setDisabled(readOnly);
        }

        if (comp instanceof Datebox datebox) {
            datebox.setButtonVisible(!readOnly);
        } else if (comp instanceof Combobox combo) {
            combo.setButtonVisible(!readOnly);
        } else if (comp instanceof Bandbox bandbox) {
            bandbox.setReadonly(readOnly);
        }

        if (comp != null && comp.getChildren() != null && !comp.getChildren().isEmpty()) {
            for (Component child : comp.getChildren()) {
                changeReadOnly(child, readOnly);
            }
        }
    }

    /**
     * Show Dialog with custom properties
     *
     * @param uri
     * @param title
     * @param icon
     * @param data
     * @param width
     * @param height
     * @param onCloseListener
     * @return
     */
    public static Window showDialog(String uri, String title, String icon, Object data, String width, String height,
                                    EventListener onCloseListener) {
        Window window = showDialog(uri, title, data, width, height, onCloseListener);
        if (window.getCaption() != null) {
            window.getCaption().setIconSclass(icon);
        }
        return window;
    }

    /**
     * Invoke a Javascript util method to update browser uri and page title
     *
     * @param pagetitle
     * @param uri
     */
    public static void updateClientURI(String pagetitle, String uri) {
        Clients.evalJavaScript(String.format("changeURI('%s','%s');", pagetitle, uri));
    }

    public static void typeSearch(Textbox textbox) {
        if (textbox != null) {
            textbox.setClientAttribute("type", "search");
        }
    }
}
