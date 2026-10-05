
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.ViewDescriptorNotFoundException;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.web.navigation.ErrorResult;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class DashboardRestControllerTest {

    private static class SalesWidget extends AbstractDashboardWidgetDefinition {
        Map<String, Object> updated;
        boolean fail;

        @Override
        public String getId() {
            return "sales-kpi";
        }

        @Override
        public String getType() {
            return DashboardWidgetTypes.KPI;
        }

        @Override
        public void init(WidgetContext context) {
            if (fail) {
                throw new IllegalStateException("boom");
            }
            context.add("total", 42);
        }

        @Override
        public void update(Map<String, Object> params) {
            updated = params;
        }

        @Override
        public Object getData(WidgetContext context) {
            return new KpiWidgetData(context.get("total"), "Sales");
        }
    }

    private SalesWidget widget;
    private DashboardRestController controller;
    private ViewDescriptorFactory controllerFactory;

    @BeforeEach
    void setUp() {
        Containers.get().removeAllContainers();
        widget = new SalesWidget();
        var container = new SimpleObjectContainer();
        container.addObject(widget);
        Containers.get().installObjectContainer(container);

        var dashboard = new DefaultViewDescriptor(Object.class, "dashboard");
        dashboard.setId("mainDashboard");
        dashboard.addField(fieldWithWidget("totalSales", "sales-kpi"));
        dashboard.addField(fieldWithWidget("ghost", "not-registered"));

        var notADashboard = new DefaultViewDescriptor(Object.class, "table");
        notADashboard.setId("booksTable");

        var factory = (ViewDescriptorFactory) Proxy.newProxyInstance(
                ViewDescriptorFactory.class.getClassLoader(), new Class[]{ViewDescriptorFactory.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getDescriptor") && args.length == 1) {
                        for (ViewDescriptor d : new ViewDescriptor[]{dashboard, notADashboard}) {
                            if (d.getId().equals(args[0])) {
                                return d;
                            }
                        }
                        throw new ViewDescriptorNotFoundException("Cannot found view descriptor using id: " + args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        controllerFactory = factory;
        controller = new DashboardRestController(factory);
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    private static Field fieldWithWidget(String name, String widgetId) {
        var field = new Field(name);
        field.getParams().put("widget", widgetId);
        return field;
    }

    private ResponseEntity<?> call(String descriptor, String field, Map<String, String> params) {
        return controller.getWidget(descriptor, field, params, authenticatedRequest());
    }

    private static MockHttpServletRequest authenticatedRequest() {
        var request = new MockHttpServletRequest("GET", "/api/dashboard");
        request.setUserPrincipal(() -> "user");
        return request;
    }

    @Test
    void anonymousCallerIs401() {
        var response = controller.getWidget("mainDashboard", "totalSales", Map.of(), new MockHttpServletRequest("GET", "/api/dashboard"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(widget.updated);
    }

    @Test
    void aSharedSingletonWidgetIsNotUsedByTheRequest() {
        try (var context = new GenericApplicationContext()) {
            context.registerBean("salesWidget", SalesWidget.class, () -> widget);
            context.refresh();
            var perRequest = new DashboardRestController(controllerFactory, context);

            var response = perRequest.getWidget("mainDashboard", "totalSales", Map.of("range", "lastMonth"), authenticatedRequest());

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(DashboardWidgetResponse.class, response.getBody());
            assertNull(widget.updated, "the shared singleton must not receive the request state");
        }
    }

    @Test
    void returnsWidgetDefinitionAndData() {
        var response = call("mainDashboard", "totalSales", Map.of());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = assertInstanceOf(DashboardWidgetResponse.class, response.getBody());
        assertEquals("totalSales", body.field());
        assertEquals("sales-kpi", body.widget());
        assertEquals(DashboardWidgetTypes.KPI, body.type());
        assertEquals(new KpiWidgetData(42, "Sales"), body.data());
        assertNull(widget.updated);
    }

    @Test
    void forwardsRequestParamsToUpdate() {
        call("mainDashboard", "totalSales", Map.of("range", "lastMonth"));

        assertEquals(Map.of("range", "lastMonth"), widget.updated);
    }

    @Test
    void unknownDashboardIs404() {
        assertEquals(HttpStatus.NOT_FOUND, call("missing", "totalSales", Map.of()).getStatusCode());
    }

    @Test
    void nonDashboardDescriptorIs404() {
        assertEquals(HttpStatus.NOT_FOUND, call("booksTable", "totalSales", Map.of()).getStatusCode());
    }

    @Test
    void unknownFieldIs404() {
        assertEquals(HttpStatus.NOT_FOUND, call("mainDashboard", "nope", Map.of()).getStatusCode());
    }

    @Test
    void fieldBoundToUnregisteredWidgetIs404() {
        assertEquals(HttpStatus.NOT_FOUND, call("mainDashboard", "ghost", Map.of()).getStatusCode());
    }

    @Test
    void widgetFailureIs500WithoutLeakingTheCause() {
        widget.fail = true;

        var response = call("mainDashboard", "totalSales", Map.of());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        var body = assertInstanceOf(ErrorResult.class, response.getBody());
        assertEquals("WIDGET_ERROR", body.getError());
        assertEquals("Error loading widget totalSales", body.getMessage());
    }
}
