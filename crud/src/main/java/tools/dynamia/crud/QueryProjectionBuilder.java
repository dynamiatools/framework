package tools.dynamia.crud;

import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueryProjectionBuilder {

    private final static String VAR = "e";

    public static QueryBuilder buildFromViewDescriptor(Class entityClass, ViewDescriptor descriptor, QueryParameters parameters) {


        List<Field> descriptorFields = Viewers.getFields(descriptor);
        List<String> fields = descriptorFields.stream()
                .filter(QueryProjectionBuilder::isSelectableField)
                .map(QueryProjectionBuilder::getFieldName)
                .collect(Collectors.toList());

        fields = new ArrayList<>(fields);

        fields.add(0, "id");


        var query = QueryBuilder.select(fields.toArray(new String[0]))
                .from(entityClass, VAR);


        descriptorFields.stream()
                .filter(field -> field.isEntity() || field.isOptional())
                .forEach(f -> {
                    String variable = f.hasVariable() ? f.getVariable() : f.getName().replace(".", "");
                    String name = f.getName();
                    if (!f.isEntity() && name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                    query.leftJoin(VAR + "." + name + " as " + variable);
                });

        query.where(parameters);

        query.resultType(BeanMap.class);


        return query;
    }

    private static String getFieldName(Field f) {
        String name = f.getName();
        if (f.hasPath()) {
            name = "(" + f.getPath() + ") as " + f.getName().replace(".", "_");
        }

        return name;

    }

    private static boolean isSelectableField(Field f) {

        if (f.hasPath()) {
            return true;
        }

        try {
            Class<? extends Annotation> transientAnnotation = (Class<? extends Annotation>) Class.forName("jakarta.persistence.Transient");
            if (f.getPropertyInfo() != null && (f.getPropertyInfo().isAnnotationPresent(transientAnnotation) || f.getPropertyInfo().isTransient())) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            //ignore
        }


        return !f.isCollection() && f.isProperty() && f.isVisible();
    }
}
