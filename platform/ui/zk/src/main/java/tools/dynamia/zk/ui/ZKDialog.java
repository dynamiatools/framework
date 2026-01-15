package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Window;
import tools.dynamia.ui.EventCallback;
import tools.dynamia.ui.DialogComponent;
import tools.dynamia.ui.UIComponent;

/**
 * ZK implementation of {@link DialogComponent}
 */
public class ZKDialog extends Window implements DialogComponent {
    private Component content;
    private Object data;
    private Caption caption;

    @Override
    public void setTitle(String title) {
        if (caption == null) {
            caption = new Caption();
            caption.setIconSclass("fa fa-dot-circle");
            appendChild(caption);
        }
        caption.setLabel(title);
    }

    @Override
    public String getTitle() {
        if (caption != null) {
            return caption.getLabel();
        } else {
            return null;
        }
    }

    @Override
    public void setContent(Object content) {
        if (content instanceof Component component) {
            this.content = component;
            appendChild(component);
        } else {
            throw new IllegalArgumentException("Content must be a ZK Component");
        }
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void onClose(EventCallback callback) {
        if (callback != null) {
            addEventListener(Events.ON_CLOSE, event -> callback.onEvent());
        }
    }

    @Override
    public void setDraggable(boolean draggable) {
        setDraggable(String.valueOf(draggable));
    }

    @Override
    public boolean isDraggable() {
        return "true".equals(getDraggable());
    }

    @Override
    public void show() {
        doModal();
    }

    @Override
    public void close() {
        detach();
    }

    @Override
    public void add(UIComponent child) {
        if (child instanceof Component component) {
            appendChild(component);
        }
    }
}
