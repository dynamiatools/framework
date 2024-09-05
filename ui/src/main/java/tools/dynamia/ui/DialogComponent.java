package tools.dynamia.ui;

public interface DialogComponent extends UIComponent{

    void setTitle(String title);

    String getTitle();

    void setWidth(String width);

    String getWidth();

    void setHeight(String height);

    String getHeight();

    void setContent(Object content);

    Object getContent();

    void setData(Object data);

    Object getData();

    void onClose(EventCallback callback);

    void setDraggable(boolean draggable);

    boolean isDraggable();

    void setClosable(boolean closable);

    boolean isClosable();

    void show();

    void close();

}
