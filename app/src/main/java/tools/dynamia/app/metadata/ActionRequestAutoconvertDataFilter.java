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
 * This filter run on {@link ActionExecutionRequest} and try to auto convert data that is Map or Json String to POJO.
 * It`s only works if data type is a valid class name.
 */
@Component
public class ActionRequestAutoconvertDataFilter implements ActionFilter {


    private final static LoggingService logger = new SLF4JLoggingService(ActionRequestAutoconvertDataFilter.class);
    private final CrudService crudService;

    public ActionRequestAutoconvertDataFilter(CrudService crudService) {
        this.crudService = crudService;
    }

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
