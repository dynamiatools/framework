package tools.dynamia.zk.ui;

import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import java.util.List;
import java.util.Objects;

public class Card extends DivContainer {

    public static final String SCLASS_PREFIX = "card-";
    private Div header;
    private Div body = new Div();
    private Div tools;
    private H3 title;
    private String iconSclass;
    private I icon;
    private Text titleValue;
    private String color;

    private boolean expandable;
    private Button expandableBtn;
    private boolean collapsable;
    private Button collapsableBtn;
    private boolean closeable;
    private Button closableBtn;

    public Card() {
        initUI();
    }

    protected void initUI() {
        setZclass("card");
        header = new Div();
        header.setZclass(SCLASS_PREFIX + "header");
        appendChild(header);

        title = new H3();
        title.setSclass(SCLASS_PREFIX + "title");
        titleValue = new Text();
        icon = new I();
        title.appendChild(icon);

        title.appendChild(titleValue);
        header.appendChild(title);

        tools = new Div();
        tools.setZclass(SCLASS_PREFIX + "tools");
        header.appendChild(tools);

        body = new Div();
        body.setZclass(SCLASS_PREFIX + "body");
        appendChild(body);

        collapsableBtn = createToolButton("fa fa-minus", "collapse");
        expandableBtn = createToolButton("fa fa-expand", "maximize");
        closableBtn = createToolButton("fa fa-times", "remove");
    }

    private Button createToolButton(String icon, String role) {
        var btn = new Button();
        btn.setZclass("btn btn-tool");
        btn.setIconSclass(icon);
        btn.setClientDataAttribute("card-widget", role);
        return btn;
    }


    public void setTitle(String title) {
        if (!Objects.equals(titleValue.getValue(), title)) {
            titleValue.setValue(" " + title);
        }
    }

    public String getTitle() {
        return titleValue.getValue();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        if (!Objects.equals(this.color, color)) {
            this.removeSclass(SCLASS_PREFIX + color);
            this.color = color;
            this.addSclass(SCLASS_PREFIX + color);
        }
    }

    @Override
    public boolean insertBefore(Component newChild, Component refChild) {
        if (newChild != header && newChild != body) {
            return body.insertBefore(newChild, refChild);
        } else {
            return super.insertBefore(newChild, refChild);
        }
    }

    @Override
    public <T extends Component> List<T> getChildren() {
        return body.getChildren();
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
        if (this.expandable) {
            expandableBtn.setParent(tools);
        } else {
            expandableBtn.setParent(null);
        }
    }

    public boolean isCollapsable() {
        return collapsable;
    }

    public void setCollapsable(boolean collapsable) {
        this.collapsable = collapsable;
        if (this.collapsable) {
            collapsableBtn.setParent(tools);
        } else {
            collapsableBtn.setParent(null);
        }
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
        if (this.closeable) {
            closableBtn.setParent(tools);
        } else {
            closableBtn.setParent(null);
        }
    }

    public Button getExpandableBtn() {
        return expandableBtn;
    }

    public Button getCollapsableBtn() {
        return collapsableBtn;
    }

    public Button getClosableBtn() {
        return closableBtn;
    }

    public String getIconSclass() {
        return iconSclass;
    }

    public void setIconSclass(String iconSclass) {
        if(!Objects.equals(this.iconSclass,iconSclass)) {
            this.iconSclass = iconSclass;
            icon.setSclass(iconSclass);
        }
    }
}

