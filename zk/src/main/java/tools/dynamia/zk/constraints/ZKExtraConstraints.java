package tools.dynamia.zk.constraints;

import org.zkoss.zul.Constraint;

public class ZKExtraConstraints {

    public final static Required REQUIRED = new Required();
    public final static Percent PERCENT = new Percent();
    public final static Email EMAIL = new Email();

    public static Constraint getInstance(String name) {
        if (name != null) {
            name = name.toLowerCase();
            return switch (name) {
                case "required" -> REQUIRED;
                case "percent", "min 0 max 100" -> PERCENT;
                case "email" -> EMAIL;
                default -> null;
            };
        }
        return null;
    }

}
