package tools.dynamia.modules.email.ui;

import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.modules.email.OTPMessage;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a dialog window for entering an OTP (One-Time Password).
 */
public class OTPInputDialog extends Window {


    private String otp;
    private Label label;
    private List<InputElement> inputs;
    private OTPMessage.OTPType type = OTPMessage.OTPType.NUMERIC;
    private int size = 6;
    private Callback verifiedCallback;
    private Callback invalidCallback;
    private Button button;
    private final String prefix = StringUtils.randomString().substring(0, 4);

    public OTPInputDialog(String otp) {
        this.otp = otp;
        init();
    }

    public OTPInputDialog(String otp, OTPMessage.OTPType type) {
        this.otp = otp;
        this.type = type;
        init();
    }

    private void init() {
        getChildren().clear();

        setClosable(true);
        setTitle("Verify");
        setWidth("500px");
        setStyle("padding: 10px");
        setBorder(true);

        var layout = new Vlayout();
        layout.setHflex("1");
        appendChild(layout);


        label = new Label();
        label.setStyle("text-align: center; display: block; padding-bottom: 12px;");
        layout.appendChild(label);
        inputs = new ArrayList<>();

        var inputLayout = new Hlayout();
        inputLayout.setStyle("text-align: center");
        layout.appendChild(inputLayout);


        for (int i = 1; i <= size; i++) {
            var input = buildInput(i);
            inputs.add(input);
            inputLayout.appendChild(input);
        }

        this.button = new Button("Verify");
        button.setZclass("btn btn-block btn-success");
        button.setStyle("margin-top: 20px");
        appendChild(button);

        button.addEventListener(Events.ON_CLICK, event -> {
            String inputCode = inputs.stream().map(InputElement::getText).collect(Collectors.joining());
            if (inputCode.equals(otp)) {
                if (verifiedCallback != null) {
                    verifiedCallback.doSomething();
                    detach();
                }
            } else {
                if (invalidCallback != null) {
                    invalidCallback.doSomething();
                }
            }
        });

    }

    private InputElement buildInput(int index) {
        InputElement inp = null;
        if (type == OTPMessage.OTPType.NUMERIC) {
            inp = new Intbox();
        } else {
            inp = new Textbox();
        }
        inp.setMaxlength(1);
        inp.setWidth("40px");
        inp.setHeight("50px");
        inp.setStyle("text-align: center; font-size: 20px");
        inp.setSclass(prefix + index);
        inp.setClientAttribute("onkeyup", "jq('." + prefix + (index + 1) + "').focus()");
        if (index == 1) {
            inp.focus();
        }
        return inp;
    }

    public void show() {
        setPage(ZKUtil.getFirstPage());
        doModal();
    }

    public String getOtp() {
        return otp;
    }


    public void onVerified(Callback callback) {
        this.verifiedCallback = callback;
    }

    public void onInvalid(Callback callback) {
        this.invalidCallback = callback;
    }

    public void setLabel(String text) {
        label.setValue(text);
    }


    public void setButtonLabel(String text) {
        button.setLabel(text);
    }


    public void clear() {
        inputs.forEach(inputElement -> inputElement.setText(null));
        inputs.stream().findFirst().ifPresent(HtmlBasedComponent::focus);
    }
}
