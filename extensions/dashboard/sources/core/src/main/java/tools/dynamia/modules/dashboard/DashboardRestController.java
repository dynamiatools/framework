
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

package tools.dynamia.modules.dashboard;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.ClassUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.ViewDescriptorNotFoundException;
import tools.dynamia.web.navigation.ErrorResult;

import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoint that serves the data of the widgets of a dashboard, so a JS frontend can render a dashboard
 * without ZK. The layout (columns, spans) and the widget slots come from the dashboard view descriptor, fetched
 * with the metadata API; this endpoint only returns each widget's data.
 *
 * <pre>
 * GET /api/dashboard/{descriptorId}/widgets/{field}?param=value
 * </pre>
 *
 * Only widgets declared as a field of the dashboard descriptor can be loaded, never an arbitrary widget id.
 * Unknown dashboards or fields answer {@code 404}. Request parameters are passed to
 * {@link DashboardWidgetDefinition#update(Map)}.
 * <p>
 * Every request works on its own widget instance: a prototype bean (what {@link InstallDashboardWidget} declares) is
 * already new on each lookup, and a widget registered as a shared singleton bean is replaced by a new instance created
 * by the Spring bean factory, so {@code init}/{@code update} never mix the state of concurrent users.
 * <p>
 * <strong>Access:</strong> the caller must be authenticated ({@link HttpServletRequest#getUserPrincipal()}), otherwise
 * the endpoint answers {@code 401}. A dashboard descriptor has no link to the navigation pages that show it, so the
 * navigation restrictions of those pages cannot be applied here: any authenticated user can read the widgets of any
 * dashboard descriptor. Applications that need per-dashboard authorization must restrict {@code /api/dashboard/**} in
 * their web security configuration.
 *
 * @author Mario Serrano Leones
 */
@RestController
@RequestMapping(value = DashboardRestController.PATH, produces = "application/json")
public class DashboardRestController {

    /** Base path of the dashboard endpoints. */
    public static final String PATH = "/api/dashboard";

    private static final String DASHBOARD_VIEW = "dashboard";
    private static final String WIDGET_PARAM = "widget";
    private static final LoggingService logger = LoggingService.get(DashboardRestController.class);

    private final ViewDescriptorFactory viewDescriptorFactory;
    private final ApplicationContext applicationContext;

    /**
     * Creates the controller without a Spring context: widgets are used as {@link DashboardWidgets#findById} returns
     * them.
     *
     * @param viewDescriptorFactory the factory used to resolve the dashboard descriptor
     */
    public DashboardRestController(ViewDescriptorFactory viewDescriptorFactory) {
        this(viewDescriptorFactory, null);
    }

    /**
     * Creates the controller.
     *
     * @param viewDescriptorFactory the factory used to resolve the dashboard descriptor
     * @param applicationContext    the Spring context, used to give each request its own widget instance
     */
    @Autowired
    public DashboardRestController(ViewDescriptorFactory viewDescriptorFactory, ApplicationContext applicationContext) {
        this.viewDescriptorFactory = viewDescriptorFactory;
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes a dashboard widget and returns its data.
     *
     * @param descriptorId the dashboard view descriptor id
     * @param field        the name of the descriptor field the widget is bound to
     * @param params       request parameters, forwarded to {@link DashboardWidgetDefinition#update(Map)}
     * @param request      the current HTTP request, used for the error path
     * @return {@code 200} with a {@link DashboardWidgetResponse}, {@code 401} if the caller is not authenticated,
     * {@code 404} if the dashboard, field or widget
     * does not exist, or {@code 500} if the widget fails
     */
    @GetMapping("/{descriptorId}/widgets/{field}")
    public ResponseEntity<?> getWidget(@PathVariable String descriptorId, @PathVariable String field,
                                       @RequestParam Map<String, String> params, HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResult(
                    HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Authentication required", request.getRequestURI()));
        }

        ViewDescriptor descriptor;
        try {
            descriptor = viewDescriptorFactory.getDescriptor(descriptorId);
        } catch (ViewDescriptorNotFoundException e) {
            return notFound("Dashboard not found: " + descriptorId, request);
        }
        if (!DASHBOARD_VIEW.equals(descriptor.getViewTypeName())) {
            return notFound("Dashboard not found: " + descriptorId, request);
        }

        var descriptorField = descriptor.getField(field);
        if (descriptorField == null) {
            return notFound("Widget field not found: " + field, request);
        }

        var widgetId = descriptorField.getParams().get(WIDGET_PARAM);
        var widget = newWidgetInstance(DashboardWidgets.findById(widgetId != null ? widgetId.toString() : null));
        if (widget == null) {
            return notFound("Widget not found for field: " + field, request);
        }

        try {
            var context = new WidgetContext(descriptor, descriptorField);
            widget.init(context);
            if (!params.isEmpty()) {
                widget.update(new HashMap<>(params));
            }
            return ResponseEntity.ok(new DashboardWidgetResponse(field, widget.getId(), widget.getType(),
                    widget.getTitle(), widget.isTitleVisible(), widget.isEditable(), widget.isClosable(),
                    widget.isMaximizable(), widget.getData(context)));
        } catch (Exception e) {
            logger.error("Error loading dashboard widget " + widget.getId() + " of " + descriptorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResult(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "WIDGET_ERROR", "Error loading widget " + field,
                    request.getRequestURI()));
        }
    }

    /**
     * Makes sure the widget is not shared with other requests. A prototype bean is already a new instance; any other
     * bean is replaced by a new instance created (and autowired) by the bean factory.
     */
    private DashboardWidgetDefinition newWidgetInstance(DashboardWidgetDefinition widget) {
        if (widget == null || applicationContext == null) {
            return widget;
        }
        Class<?> type = ClassUtils.getUserClass(widget);
        String[] names = applicationContext.getBeanNamesForType(type);
        if (names.length == 1 && applicationContext.isPrototype(names[0])) {
            return widget;
        }
        return (DashboardWidgetDefinition) applicationContext.getAutowireCapableBeanFactory().createBean(type);
    }

    private static ResponseEntity<ErrorResult> notFound(String message, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult(
                HttpStatus.NOT_FOUND.value(), "NOT_FOUND", message, request.getRequestURI()));
    }
}
