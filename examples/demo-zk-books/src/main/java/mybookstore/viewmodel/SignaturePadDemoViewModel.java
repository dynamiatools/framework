package mybookstore.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import tools.dynamia.zk.util.ZKUtil;

public class SignaturePadDemoViewModel {

    private String signature;

    @Command
    public void view() {
        System.out.println(signature);
        var image = new Image(signature);
        var div = new Div();
        div.appendChild(image);
        ZKUtil.showDialog("The signature", div, "400px", "300px");

    }

    @Command
    @NotifyChange("*")
    public void clear() {
        signature = null;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Command
    public void signatureChange(Event event) {
        System.out.println("Signature change: " + event);
    }
}
