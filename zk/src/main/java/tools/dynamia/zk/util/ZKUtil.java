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

import jakarta.servlet.http.HttpSession;
import org.zkoss.zhtml.impl.AbstractTag;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.event.EventListener;
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
import tools.dynamia.zk.crud.ui.EntityTreeNode;
import tools.dynamia.zk.navigation.ZKNavigationManager;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.ui.InputPanel;
import tools.dynamia.zk.ui.MessageDialog;
import tools.dynamia.zk.ui.SimpleListItemRenderer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Comprehensive utility class providing helper methods for common ZK framework operations.
 * This class offers a wide range of utilities for working with ZK components, dialogs, messages,
 * event handling, and UI manipulations in ZK-based applications.
 *
 * <p>Main functionalities include:</p>
 * <ul>
 *     <li>Message and dialog display (alerts, questions, custom dialogs)</li>
 *     <li>Component data binding (comboboxes, listboxes)</li>
 *     <li>Dynamic component creation and manipulation</li>
 *     <li>Event queue management and subscriptions</li>
 *     <li>UI component state management (readonly, disabled)</li>
 *     <li>Navigation and execution context utilities</li>
 *     <li>Icon configuration for components</li>
 *     <li>Ajax loader creation</li>
 *     <li>Tooltip generation</li>
 * </ul>
 *
 * <p>This is an abstract utility class with only static methods and cannot be instantiated.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Show a message
 * ZKUtil.showMessage("Operation completed successfully", MessageType.NORMAL);
 *
 * // Fill a combobox with data
 * ZKUtil.fillCombobox(myCombo, dataList, true);
 *
 * // Show a dialog
 * ZKUtil.showDialog("/views/myView.zul", "My Dialog", myData);
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ZKUtil {

    /**
     * Logger instance for this utility class.
     */
    private static final LoggingService LOGGER = new SLF4JLoggingService(ZKUtil.class);

    /**
     * Execution argument key for parent window reference.
     */
    private static final String PARENT_WINDOW = "parentWindow";

    /**
     * Execution argument key for entity data.
     */
    private static final String ENTITY = "entity";

    /**
     * Execution argument key for navigation page reference.
     */
    public static final String NAVIGATION_PAGE = "navigationPage";


    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ZKUtil() {
    }

    /**
     * Constant representing the YES button option in messageboxes.
     */
    public static final int YES = Messagebox.YES;

    /**
     * Constant representing the NO button option in messageboxes.
     */
    public static final int NO = Messagebox.NO;

    /**
     * Displays a message to the user with default title and normal type.
     *
     * @param message the message text to display
     */
    public static void showMessage(String message) {
        showMessage(message, "Mensaje", MessageType.NORMAL);
    }

    /**
     * Displays a message to the user with a default title and specified type.
     *
     * @param message the message text to display
     * @param type    the message type (NORMAL, INFO, WARNING, ERROR, etc.)
     */
    public static void showMessage(String message, MessageType type) {
        showMessage(message, "Message", type);

    }

    /**
     * Displays a message to the user with custom title and type.
     * Uses the configured {@link MessageDisplayer} from the container, or falls back
     * to {@link MessageDialog} if none is configured.
     *
     * @param message the message text to display
     * @param title   the dialog title
     * @param type    the message type (NORMAL, INFO, WARNING, ERROR, etc.)
     */
    public static void showMessage(String message, String title, MessageType type) {

        MessageDisplayer displayer = Containers.get().findObject(MessageDisplayer.class);
        if (displayer == null) {
            displayer = new MessageDialog();
        }

        displayer.showMessage(message, title, type);

    }

    /**
     * Displays a question dialog with YES/NO buttons.
     * The listener is invoked when the user clicks a button, allowing the caller
     * to handle the user's response.
     *
     * @param question the question text to display
     * @param title    the dialog title
     * @param listener the event listener to handle button clicks
     */
    public static void showQuestion(String question, String title, EventListener<Messagebox.ClickEvent> listener) {
        Messagebox.Button[] buttons = {Messagebox.Button.YES, Messagebox.Button.NO};
        Messagebox.show(question, title, buttons, Messagebox.QUESTION, listener);
    }

    /**
     * Fills a combobox with data from a collection.
     *
     * @param combo the combobox to fill
     * @param data  the collection of data items
     * @param live  if true, the model reflects changes in the underlying collection
     */
    public static void fillCombobox(Combobox combo, Collection data, boolean live) {
        fillCombobox(combo, data, null, live);
    }

    /**
     * Fills a combobox with data from a collection and sets a selected item.
     *
     * @param combo    the combobox to fill
     * @param data     the collection of data items
     * @param selected the item to be selected initially (can be null)
     * @param live     if true, the model reflects changes in the underlying collection
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
     * Fills a combobox with data from a list using a live model.
     *
     * @param combo the combobox to fill
     * @param data  the list of data items
     */
    public static void fillCombobox(Combobox combo, List data) {
        fillCombobox(combo, data, true);
    }

    /**
     * Fills a combobox with data from a list.
     *
     * @param combo the combobox to fill
     * @param data  the list of data items
     * @param live  if true, the model reflects changes in the underlying list
     */
    public static void fillCombobox(Combobox combo, List data, boolean live) {
        fillCombobox(combo, data, null, live);
    }

    /**
     * Fills a combobox with data from an array.
     *
     * @param combo the combobox to fill
     * @param data  the array of data items
     * @param live  if true, the model reflects changes in the underlying collection
     */
    public static void fillCombobox(Combobox combo, Object[] data, boolean live) {
        if (combo != null) {
            List dataList = Arrays.asList(data);
            fillCombobox(combo, dataList, live);
        }
    }

    /**
     * Fills a listbox with data from a collection.
     * The listbox model is cleared before populating with new data.
     *
     * @param listbox the listbox to fill
     * @param data    the collection of data items
     * @param live    if true, the model reflects changes in the underlying collection
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
     * Fills a listbox with data from a list.
     * The listbox model is cleared before populating with new data.
     *
     * @param listbox the listbox to fill
     * @param data    the list of data items
     * @param live    if true, the model reflects changes in the underlying list
     */
    public static void fillListbox(Listbox listbox, List data, boolean live) {
        if (listbox != null && data != null) {
            listbox.setModel((ListModel) null);
            listbox.setModel(new ListModelList<>(data, live));
        }
    }

    /**
     * Fills a listbox with data from an array.
     * The listbox model is cleared before populating with new data.
     *
     * @param listbox the listbox to fill
     * @param data    the array of data items
     * @param live    if true, the model reflects changes in the underlying collection
     */
    public static void fillListbox(Listbox listbox, Object[] data, boolean live) {
        if (listbox != null && data != null) {
            List dataList = Arrays.asList(data);
            fillListbox(listbox, dataList, live);
        }
    }

    /**
     * Clears all input components on the page by setting their values to null.
     * Recursively processes all components in the page hierarchy.
     *
     * @param page the page containing components to clear
     */
    public static void clearPage(Page page) {
        if (page != null) {
            Component comp = page.getFirstRoot();
            clearComponent(comp);
        }
    }

    /**
     * Clears a component's value by setting it to null.
     * If the component has children, recursively clears them as well.
     * Only works with {@link InputElement} instances.
     *
     * @param comp the component to clear
     */
    public static void clearComponent(Component comp) {

        for (Object object : comp.getChildren()) {
            try {
                clearComponent((Component) object);
            } catch (Exception ignored) {
            }
        }
        if (comp instanceof InputElement ie) {
            ie.setRawValue(null);
        }
    }

    /**
     * Displays a modal dialog window with content loaded from a URI.
     * Automatically adjusts size for smartphone devices.
     *
     * @param uri    the URI of the content to load (can be a ZUL file path)
     * @param title  the dialog window title
     * @param data   optional data to pass to the dialog content
     * @param width  the dialog width (e.g., "500px", "80%")
     * @param height the dialog height (e.g., "400px", "60%")
     * @return the created modal window
     */
    public static Window showDialog(String uri, String title, Object data, String width, String height) {
        return showDialog(uri, title, data, width, height, null);
    }

    /**
     * Displays a modal dialog window with content loaded from a URI.
     * Automatically adjusts size for smartphone devices.
     *
     * @param uri    the URI of the content to load (can be a ZUL file path)
     * @param title  the dialog window title
     * @param width  the dialog width (e.g., "500px", "80%")
     * @param height the dialog height (e.g., "400px", "60%")
     * @return the created modal window
     */
    public static Window showDialog(String uri, String title, String width, String height) {
        return showDialog(uri, title, null, width, height, null);
    }

    /**
     * Displays a modal dialog window with content loaded from a URI.
     * Supports close event handling and automatic adjustment for smartphone devices.
     *
     * @param uri             the URI of the content to load (can be a ZUL file path)
     * @param title           the dialog window title
     * @param data            optional data to pass to the dialog content
     * @param width           the dialog width (e.g., "500px", "80%")
     * @param height          the dialog height (e.g., "400px", "60%")
     * @param onCloseListener optional event listener triggered when dialog is closed
     * @return the created modal window
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
     * Creates a window component with content loaded from a URI.
     * The window is created with a caption, close button, and normal border.
     * Data can be passed to the content via execution arguments.
     *
     * @param uri   the URI of the content to load (can be null for an empty window)
     * @param title the window title displayed in the caption
     * @param data  optional data to pass to the window content (available as "data", "result", or "entity")
     * @return the created window (not yet displayed)
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
     * Creates an empty window with just a title.
     *
     * @param title the window title
     * @return the created empty window
     */
    public static Window createWindow(String title) {
        return createWindow(null, title, null);
    }

    /**
     * Displays a modal dialog with content loaded from a URI and optional data.
     *
     * @param uri   the URI of the content to load
     * @param title the dialog title
     * @param data  optional data to pass to the dialog
     * @return the created modal window
     */
    public static Window showDialog(String uri, String title, Object data) {
        return showDialog(uri, title, data, null, null);
    }

    /**
     * Displays a modal dialog with content loaded from a URI.
     *
     * @param uri   the URI of the content to load
     * @param title the dialog title
     * @return the created modal window
     */
    public static Window showDialog(String uri, String title) {
        return showDialog(uri, title, null, null, null);
    }

    /**
     * Displays a modal dialog containing a component.
     *
     * @param title     the dialog title
     * @param component the component to display in the dialog
     * @return the created modal window
     */
    public static Window showDialog(String title, Component component) {
        return showDialog(title, component, null, null);
    }

    /**
     * Displays a modal dialog containing a component with specified dimensions.
     *
     * @param title     the dialog title
     * @param component the component to display in the dialog
     * @param width     the dialog width (e.g., "500px", "80%")
     * @param height    the dialog height (e.g., "400px", "60%")
     * @return the created modal window
     */
    public static Window showDialog(String title, Component component, String width, String height) {
        return showDialog(title, component, width, height, null);
    }

    /**
     * Displays a modal dialog containing a component with dimensions and close listener.
     * The dialog is maximizable and automatically adjusts for smartphone devices.
     *
     * @param title           the dialog title
     * @param component       the component to display in the dialog
     * @param width           the dialog width (e.g., "500px", "80%")
     * @param height          the dialog height (e.g., "400px", "60%")
     * @param onCloseListener optional event listener triggered when dialog is closed
     * @return the created modal window, or null if an error occurs
     */
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
     * Displays a simple input dialog with a label and input field of the specified type.
     * The dialog triggers the provided listener when the user submits input.
     *
     * @param <T>           the type of input expected
     * @param label         the label text for the input field
     * @param inputClass    the class type of the input (String.class, Integer.class, etc.)
     * @param value         the initial value for the input field (can be null)
     * @param eventListener the listener to handle input submission
     * @return the InputPanel component displaying the dialog
     */
    public static <T> InputPanel showInputDialog(String label, Class<T> inputClass, Object value,
                                                 EventListener eventListener) {
        InputPanel inputPanel = new InputPanel(label, value, inputClass);
        inputPanel.addEventListener(InputPanel.ON_INPUT, eventListener);
        inputPanel.showDialog();
        return inputPanel;
    }

    /**
     * Displays a simple input dialog with a label and input field of the specified type.
     * The dialog triggers the provided listener when the user submits input.
     *
     * @param <T>           the type of input expected
     * @param label         the label text for the input field
     * @param inputClass    the class type of the input (String.class, Integer.class, etc.)
     * @param eventListener the listener to handle input submission
     * @return the InputPanel component displaying the dialog
     */
    public static <T> InputPanel showInputDialog(String label, Class<T> inputClass, EventListener eventListener) {
        return showInputDialog(label, inputClass, null, eventListener);
    }

    /**
     * Displays a password input dialog.
     * The input field is masked and the consumer receives the password value when submitted.
     *
     * @param label         the label text for the password field
     * @param inputPassword the consumer to receive the password value
     * @return the InputPanel component displaying the dialog
     */
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
     * Checks if an input element is empty (null or empty string).
     *
     * @param input the input element to check
     * @return true if the input text is null or empty, false otherwise
     */
    public static boolean isEmpty(InputElement input) {
        return input.getText() == null || input.getText().isEmpty();
    }

    /**
     * Synchronizes a data paginator with a ZK paginal component.
     * Updates the paginator's page and page size to match the paginal,
     * and updates the paginal's total size from the paginator.
     *
     * @param dataPaginator the data paginator to synchronize
     * @param paginal       the ZK paginal component
     */
    public static void synchronizePaginator(DataPaginator dataPaginator, Paginal paginal) {
        if (dataPaginator != null && paginal != null) {
            dataPaginator.setPage(paginal.getActivePage() + 1);
            dataPaginator.setPageSize(paginal.getPageSize());
            paginal.setTotalSize((int) dataPaginator.getTotalSize());
        }
    }

    /**
     * Retrieves the first page from the current execution's desktop.
     *
     * @return the first page in the desktop
     */
    public static Page getFirstPage() {
        Desktop desktop = Executions.getCurrent().getDesktop();
        return desktop.getFirstPage();
    }

    /**
     * Displays a popup component next to a reference component.
     *
     * @param refComponent     the reference component to position the popup near
     * @param contentComponent the component to display inside the popup
     * @param width            the popup width (e.g., "300px"), can be null
     * @param height           the popup height (e.g., "200px"), can be null
     * @return the created popup component
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
     * Displays a popup component next to a reference component with automatic sizing.
     *
     * @param refComponent     the reference component to position the popup near
     * @param contentComponent the component to display inside the popup
     * @return the created popup component
     */
    public static Popup showPopup(Component refComponent, Component contentComponent) {
        return showPopup(refComponent, contentComponent, null, null);
    }

    /**
     * Retrieves the current HTTP session ID.
     *
     * @return the session ID string
     */
    public static String getSessionID() {
        HttpSession session = (HttpSession) Executions.getCurrent().getSession().getNativeSession();
        return session.getId();
    }

    /**
     * Creates an Ajax loader component with custom message and image.
     * The loader is centered and styled with the "ajax-loader" CSS class.
     *
     * @param message   the loading message to display
     * @param imagePath the path to the loading image (null uses default)
     * @return a VBox component containing the loader
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
            imagePath = "/static/dynamia-tools/images/ajax-loader.gif";
        }

        Image img = new Image(imagePath);
        Label lbl = new Label(message);
        lbl.setStyle("font-weight:bold");

        box.appendChild(img);
        box.appendChild(lbl);
        return div;
    }

    /**
     * Creates an Ajax loader component with default settings (empty message and default image).
     *
     * @return a VBox component containing the loader
     */
    public static Component createAjaxLoader() {
        return createAjaxLoader("", null);
    }

    /**
     * Creates an Ajax loader component with a custom message and default image.
     *
     * @param message the loading message to display
     * @return a VBox component containing the loader
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
     * Configures an icon for a ZK component, supporting both IMAGE and FONT icon types.
     * Automatically handles different component types (LabelImageElement, AbstractTag, Image).
     *
     * @param icon      the icon to configure
     * @param component the component to apply the icon to
     * @param size      the desired icon size
     */
    public static void configureComponentIcon(Icon icon, Component component, IconSize size) {

        if (icon == null) {
            return;
        }

        String realPath = icon.getRealPath(component, size);

        if (component instanceof LabelImageElement element) {

            switch (icon.getType()) {
                case IMAGE -> element.setImage(realPath);
                case FONT -> element.setIconSclass(realPath);
            }
        } else if (component instanceof AbstractTag element) {
            switch (icon.getType()) {
                case IMAGE -> {
                    Image img = new Image(realPath);
                    img.setParent(component);
                }
                case FONT -> element.setSclass(realPath);
            }
        } else if (component instanceof Image image && icon.getType() == IconType.IMAGE) {
            image.setSrc(realPath);
        }
    }

    /**
     * Configures an icon for a component using an icon name from the current theme.
     * Resolves the icon from {@link IconsTheme} and applies it to the component.
     *
     * @param image     the icon name or identifier
     * @param component the component to apply the icon to
     * @param size      the desired icon size
     */
    public static void configureComponentIcon(String image, Component component, IconSize size) {
        configureComponentIcon(IconsTheme.get().getIcon(image), component, size);
    }

    /**
     * Creates a ZK component from a URI with optional arguments.
     * Supports both resource URIs (classpath:, file:, http:) and regular paths.
     * For resource URIs, creates components directly; for regular paths, uses Include.
     *
     * @param uri    the URI of the component definition (ZUL file or resource)
     * @param parent the parent component to attach to
     * @param args   map of arguments to pass to the component
     * @return the created component
     * @throws UiException if the resource is not found or cannot be loaded
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

    /**
     * Retrieves a resource from a URI, with device-specific variant support.
     * Attempts to load a device-specific version first (e.g., myfile.smartphone.zul)
     * before falling back to the standard version.
     *
     * @param uri the resource URI
     * @return the resource, or null if not found
     */
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

    /**
     * Checks if a URI represents a resource URI (starts with classpath:, file:, or http:).
     *
     * @param uri the URI to check
     * @return true if the URI is a resource URI, false otherwise
     */
    private static boolean isResourceURI(String uri) {
        return uri.startsWith("classpath:") || uri.startsWith("file:") || uri.startsWith("http:");
    }

    /**
     * Initializes event queue subscribers for a target object by loading annotations.
     * This method scans for event queue subscription annotations on the target object.
     * <p>
     * Call this method to set up event queue listeners defined via {@link tools.dynamia.zk.Subscribe} annotation.
     *
     * @param target the object to initialize event queue subscriptions for
     */
    public static void subscribeEventQueues(Object target) {
        new EventQueueSubscriber(target).loadAnnotations();
    }

    /**
     * Publishes an event to a named event queue with specific scope.
     *
     * @param name       the name of the event queue
     * @param scope      the scope of the event queue (DESKTOP, SESSION, APPLICATION)
     * @param autocreate whether to create the queue if it doesn't exist
     * @param evt        the event to publish
     */
    public static void publishToEventQueue(String name, String scope, boolean autocreate, Event evt) {
        EventQueues.lookup(name, scope, autocreate).publish(evt);
    }

    /**
     * Publishes an event to a desktop-scoped event queue.
     * The queue is automatically created if it doesn't exist.
     *
     * @param name the name of the event queue
     * @param evt  the event to publish
     */
    public static void publishToEventQueue(String name, Event evt) {
        publishToEventQueue(name, EventQueues.DESKTOP, true, evt);
    }

    /**
     * Publishes an event to a session-scoped event queue.
     * The queue is automatically created if it doesn't exist.
     *
     * @param name the name of the event queue
     * @param evt  the event to publish
     */
    public static void publishToSessionEventQueue(String name, Event evt) {
        publishToEventQueue(name, EventQueues.SESSION, true, evt);
    }

    /**
     * Checks if the current execution is within a ZK event listener thread.
     *
     * @return true if within an event listener context, false otherwise
     */
    public static boolean isInEventListener() {
        return Executions.getCurrent() != null;
    }

    /**
     * Checks if the current execution is within a desktop scope.
     *
     * @return true if within desktop scope, false otherwise
     */
    public static boolean isInDesktopScope() {
        return isInEventListener() && Executions.getCurrent().getDesktop() != null;
    }

    /**
     * Retrieves the current execution's desktop.
     *
     * @return the current desktop, or null if not in desktop scope
     */
    public static Desktop getCurrentDesktop() {
        if (isInDesktopScope()) {
            return Executions.getCurrent().getDesktop();
        }
        return null;
    }

    /**
     * Displays a modal dialog with a listbox for selecting a single item.
     * When an item is selected, the dialog closes and the listener is invoked.
     *
     * @param title    the dialog title
     * @param model    the list of items to display
     * @param onSelect the listener to handle item selection
     * @return the created modal window
     */
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

    /**
     * Displays a modal dialog with a listbox for selecting multiple items.
     * Shows checkmarks for multi-selection and a button to confirm the selection.
     *
     * @param title    the dialog title
     * @param label    the text for the confirmation button
     * @param model    the list of items to display
     * @param onSelect the listener to handle selection (receives list of selected items)
     * @return the created modal window
     */
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

    /**
     * Displays a dialog with a multiline text input field and a confirmation button.
     * The dialog closes automatically when the button is clicked unless the event propagation is stopped.
     *
     * @param title       the dialog title
     * @param buttonLabel the text for the confirmation button
     * @param evt         the listener to handle the text input (receives the text value)
     */
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
     * Selects an item in a combobox programmatically.
     * Works with comboboxes that have a ListModelList as their model.
     *
     * @param combobox the combobox to update
     * @param value    the value to select (null to clear selection)
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

    /**
     * Selects an item in a listbox programmatically.
     * Works with listboxes that have a ListModelList as their model.
     *
     * @param listbox the listbox to update
     * @param value   the value to select (null to clear selection)
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
     * Retrieves an argument from the current ZK execution context.
     *
     * @param name the name of the argument
     * @return the argument value, or null if not found
     */
    public static Object getExecutionArg(String name) {
        return Executions.getCurrent().getArg().get(name);
    }

    /**
     * Retrieves the entity object bound to the current ZK execution.
     * This is commonly used to pass entity data to dialogs and views.
     *
     * @return the entity object, or null if not present
     */
    public static Object getExecutionEntity() {
        return getExecutionArg(ENTITY);
    }

    /**
     * Retrieves the parent window bound to the current ZK execution.
     * This is useful for accessing the dialog window that opened the current view.
     *
     * @return the parent window, or null if not present
     */
    public static Window getExecutionParentWindow() {
        return (Window) getExecutionArg(PARENT_WINDOW);
    }

    /**
     * Retrieves the current navigation page from the execution context.
     * Falls back to the current page from the NavigationManager if not in execution args.
     *
     * @return the navigation page, or null if not available
     */
    public static tools.dynamia.navigation.Page getExecutionNavigationPage() {
        var page = (tools.dynamia.navigation.Page) getExecutionArg(NAVIGATION_PAGE);
        if (page == null) {
            page = ZKNavigationManager.getInstance().getCurrentPage();
        }
        return page;
    }

    /**
     * Recursively changes the readonly/disabled state of a component and its children.
     * Handles various component types including inputs, buttons, checkboxes, dateboxes, comboboxes, and bandboxes.
     *
     * @param comp     the component to modify
     * @param readOnly true to make readonly/disabled, false to make editable/enabled
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
     * Displays a modal dialog window with custom icon.
     * Extends the standard showDialog functionality by allowing custom icon configuration.
     *
     * @param uri             the URI of the content to load
     * @param title           the dialog title
     * @param icon            the icon CSS class for the caption
     * @param data            optional data to pass to the dialog
     * @param width           the dialog width
     * @param height          the dialog height
     * @param onCloseListener optional close event listener
     * @return the created modal window
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
     * Updates the browser's URI and page title using client-side JavaScript.
     * This allows changing the URL displayed in the browser without reloading the page.
     *
     * @param pagetitle the new page title to display in the browser
     * @param uri       the new URI to display in the browser address bar
     */
    public static void updateClientURI(String pagetitle, String uri) {
        Clients.evalJavaScript(String.format("changeURI('%s','%s');", pagetitle, uri));
    }

    /**
     * Configures a textbox to use the HTML5 search input type.
     * This provides native browser search styling and behavior.
     *
     * @param textbox the textbox to configure (null-safe)
     */
    public static void typeSearch(Textbox textbox) {
        if (textbox != null) {
            textbox.setClientAttribute("type", "search");
        }
    }

    /**
     * Flattens a tree model into a single collection containing all nodes.
     * Recursively traverses the tree structure and extracts all entities or nodes.
     * Null values are automatically filtered out.
     *
     * @param treeModel the tree model to flatten
     * @return a collection containing all entities/nodes from the tree
     */
    public static Collection flatTreeModel(TreeModel treeModel) {
        var result = new ArrayList<>();

        var parent = treeModel.getRoot();
        flat(treeModel, result, parent);
        result.removeIf(Objects::isNull);
        return result;
    }

    /**
     * Recursive helper method to flatten a tree model.
     * Extracts entities from EntityTreeNode instances or adds nodes directly.
     *
     * @param treeModel the tree model being flattened
     * @param result    the result list to accumulate items
     * @param parent    the current parent node being processed
     */
    private static void flat(TreeModel treeModel, ArrayList<Object> result, Object parent) {
        for (int i = 0; i < treeModel.getChildCount(parent); i++) {
            var child = treeModel.getChild(parent, i);
            if (child != null) {
                if (child instanceof EntityTreeNode node) {
                    result.add(node.getEntity());
                } else {
                    result.add(child);
                }
                flat(treeModel, result, child); //nested children
            }
        }
    }

}
