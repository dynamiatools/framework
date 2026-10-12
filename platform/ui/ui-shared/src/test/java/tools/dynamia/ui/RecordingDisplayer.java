package tools.dynamia.ui;

import tools.dynamia.commons.Callback;

import java.util.List;
import java.util.function.Consumer;

/** Test displayer that only records the plain messages. */
class RecordingDisplayer implements MessageDisplayer {

    private final List<String> shown;

    RecordingDisplayer(List<String> shown) {
        this.shown = shown;
    }

    @Override
    public void showMessage(String message) {
        shown.add(message);
    }

    @Override
    public void showMessage(String message, MessageType type) {
        shown.add(message);
    }

    @Override
    public void showMessage(String message, String title, MessageType type) {
        shown.add(message);
    }

    @Override
    public void showMessageDialog(String message, String title, MessageType messageType) {
        shown.add(message);
    }

    @Override
    public void showQuestion(String message, String title, Callback onYesResponse) {
    }

    @Override
    public void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse) {
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
    }
}
