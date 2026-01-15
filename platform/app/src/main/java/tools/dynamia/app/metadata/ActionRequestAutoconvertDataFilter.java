package tools.dynamia.app.metadata;

import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionFilter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Component;

import java.util.Map;

/**
 * {@code ActionRequestAutoconvertDataFilter} is an {@link ActionFilter} implementation that automatically converts
 * the data in an {@link ActionExecutionRequest} from a {@link Map} or JSON string to a POJO (Plain Old Java Object).
 * <p>
 * This filter is useful when the action request data is received in a generic format (Map or JSON) and needs to be
 * converted to a specific Java class before execution. It also supports loading entities by ID if the data is not present
 * but a dataId is provided.
 * <p>
 * The conversion only occurs if the data type is a valid class name.
 *
 * Typical usage is in service layers or controllers that handle dynamic action execution requests.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@Component
public class ActionRequestAutoconvertDataFilter implements ActionFilter {

    /**
     * Logger for this filter.
     */
    private final static LoggingService logger = new SLF4JLoggingService(ActionRequestAutoconvertDataFilter.class);

    /**
     * Service for CRUD operations, used to load entities by ID.
     */
    private final CrudService crudService;

    /**
     * Constructs a new {@code ActionRequestAutoconvertDataFilter} with the given CRUD service.
     *
     * @param crudService the CRUD service to use for entity loading
     */
    public ActionRequestAutoconvertDataFilter(CrudService crudService) {
        this.crudService = crudService;
    }

    /**
     * Before executing an action, attempts to convert the request data to the expected POJO type if necessary.
     * <p>
     * Supported conversions:
     * <ul>
     *     <li>Map to POJO</li>
     *     <li>JSON string to POJO</li>
     *     <li>Entity loading by ID</li>
     * </ul>
     *
     * @param action  the action being executed
     * @param request the execution request containing data and type information
     */
    @Override
    public void beforeActionExecution(Action action, ActionExecutionRequest request) {

        if (request.getDataType() != null && !BeanUtils.isValidClassName(request.getDataType())) {
            try {
                Class dataClass = BeanUtils.findClass(request.getDataType());
                Object convertedData = null;
                if (request.getData() instanceof Map<?, ?> jsonMap) {
                    logger.info("Auto converting data Map to POJO from action execution request");
                    convertedData = StringPojoParser.parseJsonToPojo(jsonMap, dataClass);
                } else if (request.getData() instanceof String json) {
                    logger.info("Auto converting data Json string to POJO from action execution request");
                    convertedData = StringPojoParser.parseJsonToPojo(json, dataClass);
                } else if (request.getData() == null && request.getDataId() != null && !request.getDataId().isBlank()) {
                    logger.info("Finding entity of type " + request.getDataType() + " with id " + request.getDataId());
                    convertedData = findEntity(dataClass, request.getDataId());
                }

                if (convertedData != null) {
                    request.setData(convertedData);
                }

            } catch (Exception e) {
                logger.warn("Cannot auto convert data from action execution request: " + e.getMessage());
            }
        }

    }

    private Object findEntity(Class dataClass, String dataId) {
        Object entity = null;

        try {
            if (DomainUtils.isLong(dataId)) {
                entity = crudService.find(dataClass, Long.parseLong(dataId));
            } else if (DomainUtils.isInteger(dataId)) {
                entity = crudService.find(dataClass, Integer.parseInt(dataId));
            } else {
                entity = crudService.find(dataClass, dataId);
            }
        } catch (Exception e) {
            logger.error("Error finding entity of type " + dataClass.getName() + " with id " + dataId + ": " + e.getMessage(), e);
        }

        return entity;
    }
}
