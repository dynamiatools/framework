package tools.dynamia.viewers.util;

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldRestriction;
import tools.dynamia.viewers.FieldRestrictionType;
import tools.dynamia.viewers.View;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class for handling field restrictions in viewers.
 * Provides methods to find, evaluate, and check restrictions on fields within views.
 */
public class FieldRestrictions {

    /**
     * Finds all available {@link FieldRestriction} instances, sorted by their order.
     *
     * @return a sorted list of {@link FieldRestriction} objects
     */
    public static List<FieldRestriction> findRestrictions() {
        return Containers.get().findObjects(FieldRestriction.class)
                .stream().sorted(Comparator.comparing(FieldRestriction::getOrder))
                .toList();
    }


    /**
     * Evaluates the given list of field restrictions for a specific view and field.
     * Returns the most relevant {@link FieldRestrictionType} found.
     *
     * @param restrictions the list of field restrictions to evaluate
     * @param view         the view containing the field
     * @param field        the field to evaluate restrictions for
     * @return the evaluated {@link FieldRestrictionType}
     */
    public static FieldRestrictionType evaluate(List<FieldRestriction> restrictions, View view, Field field) {
        FieldRestrictionType result = FieldRestrictionType.UNKNOWN;

        for (FieldRestriction restriction : restrictions) {
            FieldRestrictionType r = restriction.evaluale(view, field);
            if (r != FieldRestrictionType.UNKNOWN) {
                result = r;
                if (r == FieldRestrictionType.HIDDEN) {
                    break;
                }
            }
        }

        if (result == null) {
            result = FieldRestrictionType.UNKNOWN;
        }
        return result;

    }

    /**
     * Checks if a field is restricted (not NONE or UNKNOWN) according to the given restrictions.
     *
     * @param restrictions the list of field restrictions
     * @param view         the view containing the field
     * @param field        the field to check
     * @return true if the field is restricted, false otherwise
     */
    public static boolean isFieldRestricted(List<FieldRestriction> restrictions, View view, Field field) {
        FieldRestrictionType restrictionType = evaluate(restrictions, view, field);
        return restrictionType != FieldRestrictionType.NONE && restrictionType != FieldRestrictionType.UNKNOWN;
    }

    /**
     * Checks if a field is hidden according to the given restrictions.
     *
     * @param restrictions the list of field restrictions
     * @param view         the view containing the field
     * @param field        the field to check
     * @return true if the field is hidden, false otherwise
     */
    public static boolean isFieldHidden(List<FieldRestriction> restrictions, View view, Field field) {
        FieldRestrictionType restrictionType = evaluate(restrictions, view, field);
        return restrictionType == FieldRestrictionType.HIDDEN;
    }

    /**
     * Checks if a field is read-only according to the given restrictions.
     *
     * @param restrictions the list of field restrictions
     * @param view         the view containing the field
     * @param field        the field to check
     * @return true if the field is read-only, false otherwise
     */
    public static boolean isFieldReadOnly(List<FieldRestriction> restrictions, View view, Field field) {
        FieldRestrictionType restrictionType = evaluate(restrictions, view, field);
        return restrictionType == FieldRestrictionType.READONLY;
    }


    public static boolean isFieldVisble(List<FieldRestriction> restrictions, View view, Field field) {
        return field.isVisible() && !isFieldHidden(restrictions, view, field);
    }
}
