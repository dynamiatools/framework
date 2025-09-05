package tools.dynamia.viewers;

/**
 * Enum representing the possible restriction types for a field in a {@link View} descriptor.
 * <p>
 * Each value defines a specific level of access or visibility for a field, allowing implementations
 * of {@link FieldRestriction} to control how fields are presented and interacted with in viewers.
 * </p>
 * <ul>
 *   <li><b>NONE</b>: No restriction is applied. The field is fully accessible and editable.</li>
 *   <li><b>READONLY</b>: The field is visible but cannot be edited. It is displayed as read-only to the user.</li>
 *   <li><b>HIDDEN</b>: The field is not visible in the view. It is completely hidden from the user interface.</li>
 *   <li><b>UNKNOWN</b>: The restriction type is not determined. This value can be used as a default or fallback when no specific restriction applies.</li>
 * </ul>
 */
public enum FieldRestrictionType {
    NONE, READONLY, HIDDEN, UNKNOWN
}
