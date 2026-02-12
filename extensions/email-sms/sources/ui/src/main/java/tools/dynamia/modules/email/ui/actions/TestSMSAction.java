package tools.dynamia.modules.email.ui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zul.Messagebox;
import tools.dynamia.actions.FastAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.email.SMSMessage;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.services.SMSService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

@InstallAction
public class TestSMSAction extends AbstractCrudAction {

    @Autowired
    private SMSService service;

    public TestSMSAction() {
        setApplicableClass(EmailAccount.class);
        setName("SMS");
        setMenuSupported(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        EmailAccount account = (EmailAccount) evt.getData();
        if (account != null) {
            Viewer viewer = createView(account);
            ZKUtil.showDialog("Test SMS Message: " + account.getName(), viewer, "60%", null);
        } else {
            UIMessages.showMessage("Select account to test", MessageType.WARNING);
        }

    }

    private Viewer createView(EmailAccount account) {
        ViewDescriptor descriptor = viewDescriptor("form", SMSMessage.class, false)
                .id("testSMSForm")
                .fields(
                        field("phoneNumber"),
                        field("text"),
                        field("senderID"),
                        field("transactional")
                )
                .layout("columns", 1)
                .build();

        final Viewer viewer = new Viewer(descriptor);

        SMSMessage msg = new SMSMessage();
        msg.setCredentials(account.getSmsUsername(), account.getSmsPassword(), account.getSmsRegion());
        viewer.setValue(msg);
        viewer.addAction(new FastAction("Test", evt -> {

            var result = service.send(msg);
            Messagebox.show("ID: " + result + "<br/>Sended: " + msg.isSended() + "<br/>Result: " + msg.getResult());
        }));
        viewer.setVflex(null);
        viewer.setContentVflex(null);

        return viewer;

    }
}
