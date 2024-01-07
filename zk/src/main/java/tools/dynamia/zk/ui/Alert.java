package tools.dynamia.zk.ui;

import org.zkoss.zhtml.H4;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Text;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import tools.dynamia.ui.MessageType;

public class Alert extends Div {

    private H4 title;
    private Text titleValue;
    private I icon;
    private String iconSclass;
    private boolean closeable;
    private Button closeableBtn;

    private MessageType type = MessageType.NORMAL;
    private Text value;


    public Alert() {
        initUI();
    }

    protected void initUI() {
        setZclass("alert alert-dismissible");


        closeableBtn = new Button("x");
        closeableBtn.setZclass("close");
        closeableBtn.setClientDataAttribute("dismiss", "alert");
        closeableBtn.setClientAttribute("aria-hidden", "true");
        appendChild(closeableBtn);


        title = new H4();
        title.setStyle("font-size: 110%");
        icon = new I();
        icon.setSclass("icon");
        title.appendChild(icon);

        titleValue = new Text();
        title.appendChild(titleValue);
        appendChild(title);

        value = new Text();
        appendChild(value);
        setType(type);

    }


    public void setTitle(String title) {
        titleValue.setValue(title);
    }

    public String getTitle() {
        return titleValue.getValue();
    }

    public void setValue(String value) {
        this.value.setValue(value);
    }

    public String getValue() {
        return value.getValue();
    }

    public String getIconSclass() {
        return iconSclass;
    }

    public void setIconSclass(String iconSclass) {
        this.iconSclass = iconSclass;
        icon.setSclass("icon " + iconSclass);
    }


    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;

        String sclass = switch (type) {
            case NORMAL -> "alert-success";
            case ERROR, CRITICAL -> "alert-danger";
            case WARNING -> "alert-warning";
            case INFO -> "alert-info";
            case SPECIAL -> "bg-puple";
        };

        String icon = switch (type) {
            case NORMAL -> "fas fa-check";
            case ERROR, CRITICAL -> "fas fa-ban";
            case WARNING -> "fas fa-exclamation-triangle";
            case INFO -> "fas fa-info";
            case SPECIAL -> "fas fa-smile";
        };

        setZclass("alert alert-dismissible " + sclass);
        setIconSclass(icon);
    }

    public void setType(String type) {
        if (type != null) {
            try {
                if (type.equalsIgnoreCase("danger")) {
                    type = "ERROR";
                } else if (type.equalsIgnoreCase("success")) {
                    type = "NORMAL";
                }

                setType(MessageType.valueOf(type.toUpperCase().trim()));
            } catch (Exception e) {
                setType(MessageType.NORMAL);
            }
        }
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;

        closeableBtn.setVisible(this.closeable);

    }

    public Button getCloseableBtn() {
        return closeableBtn;
    }
}
