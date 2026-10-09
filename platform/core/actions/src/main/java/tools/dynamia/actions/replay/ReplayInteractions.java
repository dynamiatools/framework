package tools.dynamia.actions.replay;

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.commons.Callback;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.MessageType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link MessageDisplayer} of a headless run. It does not show anything: it answers from what the user already
 * answered and records the first question that has no answer yet.
 * <p>
 * Interactions are numbered in the order the action reaches them. When the action asks its n-th question and the
 * user has already answered it, the matching callback runs right there, so the code that follows the question in the
 * action keeps its place in the call stack. When it has no answer the question becomes {@link #pending()}, no
 * callback runs, and nothing else of this pass reaches the user: the runtime returns the question to the client
 * and, with the answer, runs the action again from the start. A deterministic action asks the same questions in the
 * same order, so the answers line up.
 */
public final class ReplayInteractions implements MessageDisplayer {

    /**
     * What the action told the user and was not a question.
     *
     * @param message the text
     * @param title   optional title
     * @param type    kind of message
     */
    public record Notification(String message, String title, MessageType type) {
    }

    private final List<Object> answers;
    private final List<Notification> notifications = new ArrayList<>();
    private int cursor;
    private ActionFlowStep pending;

    /**
     * @param answers the answers of the user so far, in the order of the interactions
     */
    public ReplayInteractions(List<Object> answers) {
        this.answers = answers;
    }

    /** @return the question waiting for an answer, or {@code null} when the action ran to the end */
    public ActionFlowStep pending() {
        return pending;
    }

    public boolean isPending() {
        return pending != null;
    }

    /** @return the messages of this pass, in order; they are only meaningful when the pass completed */
    public List<Notification> notifications() {
        return notifications;
    }

    private void interact(ActionFlowStep question, Consumer<Object> onAnswer) {
        if (pending != null) {
            return; // the action already stopped at an earlier question
        }
        if (cursor < answers.size()) {
            onAnswer.accept(answers.get(cursor++));
        } else {
            pending = question;
        }
    }

    private void notify(String message, String title, MessageType type) {
        if (pending == null) {
            notifications.add(new Notification(message, title, type));
        }
    }

    @Override
    public void showMessage(String message) {
        notify(message, null, MessageType.NORMAL);
    }

    @Override
    public void showMessage(String message, MessageType type) {
        notify(message, null, type);
    }

    @Override
    public void showMessage(String message, String title, MessageType type) {
        notify(message, title, type);
    }

    @Override
    public void showMessageDialog(String message, String title, MessageType messageType) {
        notify(message, title, messageType);
    }

    @Override
    public void showQuestion(String message, String title, Callback onYesResponse) {
        showQuestion(message, title, onYesResponse, null);
    }

    @Override
    public void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse) {
        interact(ActionFlowStep.confirm(message, title), answer -> {
            Callback chosen = isYes(answer) ? onYesResponse : onNoResponse;
            if (chosen != null) {
                chosen.doSomething();
            }
        });
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
        showInput(title, valueClass, null, onValue);
    }

    @Override
    public <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
        var step = ActionFlowStep.input(title, title);
        step.setData(defaultValue);
        interact(step, answer -> {
            if (answer != null && onValue != null) {
                onValue.accept(convert(answer, valueClass));
            }
        });
    }

    private static boolean isYes(Object answer) {
        return answer instanceof Boolean b ? b : "true".equalsIgnoreCase(String.valueOf(answer));
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(Object answer, Class<T> type) {
        if (type.isInstance(answer)) {
            return (T) answer;
        }
        var text = String.valueOf(answer).trim();
        Object value;
        if (type == String.class) value = text;
        else if (type == Integer.class || type == int.class) value = Integer.valueOf(text);
        else if (type == Long.class || type == long.class) value = Long.valueOf(text);
        else if (type == Double.class || type == double.class) value = Double.valueOf(text);
        else if (type == BigDecimal.class) value = new BigDecimal(text);
        else if (type == Boolean.class || type == boolean.class) value = Boolean.valueOf(text);
        else if (type == LocalDate.class) value = LocalDate.parse(text);
        else throw new IllegalArgumentException("Cannot convert the answer to " + type.getName());
        return (T) value;
    }
}
