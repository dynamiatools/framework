package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.modules.reports.api.v2.ReportDesignerInfo;
import tools.dynamia.modules.reports.api.v2.ReportPreviewRequest;
import tools.dynamia.modules.reports.core.ReportsSettings;
import tools.dynamia.modules.reports.core.design.ReportsDesignerService;
import tools.dynamia.modules.reports.core.security.ReportDesigners;

import java.util.Map;

/**
 * REST API for the report designer UI. Only users allowed by a
 * {@link tools.dynamia.modules.reports.core.security.ReportDesignerPolicy} (by default the roles of
 * {@code dynamia.reports.designer-roles}) can use it, everyone else gets {@code 403}.
 * <pre>
 * GET  /api/reports/v2/design/info
 * POST /api/reports/v2/design/preview
 * GET  /api/reports/v2/design/{id}/definition
 * POST /api/reports/v2/design/import
 * POST /api/reports/v2/design/datasources/{id}/test
 * </pre>
 */
@RestController
@RequestMapping(ReportsDesignerController.PATH)
public class ReportsDesignerController extends AbstractReportsApiController {

    public static final String PATH = ReportsApiController.PATH + "/design";

    private final ReportsDesignerService service;
    private final ReportsSettings settings;

    /**
     * @param service  the designer operations
     * @param settings the module settings
     */
    public ReportsDesignerController(ReportsDesignerService service, ReportsSettings settings) {
        this.service = service;
        this.settings = settings;
    }

    /**
     * Tells if the user can design reports; the UI uses it to show or hide the designer tools.
     *
     * @param request the current request
     * @return the designer info, or 401
     */
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> info(HttpServletRequest request) {
        return authenticated(request)
                ? ResponseEntity.ok(new ReportDesignerInfo(ReportDesigners.canDesign(), settings.getPreviewLimit()))
                : unauthorized(request);
    }

    /**
     * Runs the query being designed and returns its first rows.
     *
     * @param body    the query, language, datasource and parameters
     * @param request the current request
     * @return the preview, 400 for a query that is not allowed, 403 or 401
     */
    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> preview(@RequestBody ReportPreviewRequest body, HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(service.preview(body)) : unauthorized(request);
    }

    /**
     * Downloads the definition of a report as JSON.
     *
     * @param id      the report id
     * @param request the current request
     * @return the definition JSON, 404, 403 or 401
     */
    @GetMapping(value = "/{id:\\d+}/definition", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> definition(@PathVariable Long id, HttpServletRequest request) {
        if (!authenticated(request)) {
            return unauthorized(request);
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(service.exportDefinition(id));
    }

    /**
     * Imports a report definition.
     *
     * @param json    the definition, as exported by {@link #definition}
     * @param request the current request
     * @return the id of the new report
     */
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importDefinition(@RequestBody String json, HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(Map.of("id", service.importDefinition(json))) : unauthorized(request);
    }

    /**
     * Tests the connection of a datasource.
     *
     * @param id      the datasource id
     * @param request the current request
     * @return whether the connection is valid
     */
    @PostMapping(value = "/datasources/{id:\\d+}/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> testDataSource(@PathVariable Long id, HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(service.testDataSource(id)) : unauthorized(request);
    }
}
