package tools.dynamia.modules.reports.core.domain.enums;

import java.math.BigDecimal;
import java.util.Date;

public enum DataType {
    TEXT(String.class), DATE(Date.class), NUMBER(Long.class), CURRENCY(BigDecimal.class), ENUM(Enum.class), ENTITY(Object.class), BOOLEAN(Boolean.class), DATE_TIME(Date.class), TIME(Date.class);

    DataType(Class typeClass) {
        this.typeClass = typeClass;
    }

    public Class getTypeClass() {
        return typeClass;
    }

    private Class typeClass;
}
