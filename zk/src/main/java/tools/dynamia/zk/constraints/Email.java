package tools.dynamia.zk.constraints;


import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Constraint;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.ValidatorUtil;

public class Email implements Constraint {
    @Override
    public void validate(Component comp, Object value) throws WrongValueException {
        if (comp != null && comp.getId() != null && !comp.getId().isEmpty() && comp.getParent() != null) {
            if (value instanceof String email && !email.isBlank()) {
                try {
                    ValidatorUtil.validateEmail(email, "invalid");
                } catch (ValidationError e) {
                    throw new WrongValueException(comp, Messages.get(Email.class, "email"));
                }
            }
        }
    }
}
