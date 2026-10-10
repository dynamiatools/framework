/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.actions;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.ui.MessageType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A single step in a {@link FlowRemoteAction} flow: either an interaction the client must perform
 * (show a confirm dialog, collect input, render a form...) or the terminal {@link ActionFlowStepType#DONE}
 * result.
 * <p>
 * Build instances with the static factories ({@link #confirm}, {@link #input}, {@link #dialog},
 * {@link #notify}, {@link #redirect}, {@link #call}, {@link #done}, {@link #custom}) — {@link #flowId}
 * and {@link #resumeToken} are stamped by {@link ActionFlows} right before the step is sent to the client,
 * not by action authors.
 * <p>
 * See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} for the full protocol design.
 *
 * @apiNote <b>Experimental</b>, see {@link FlowRemoteAction}. {@link #redirect} and {@link #call} are the
 * least settled: the Vue client treats {@code REDIRECT} as terminal and rejects {@code awaitReturn = true}.
 * @author Mario A. Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionFlowStep implements Serializable {

    private String flowId;
    private ActionFlowStepType type;
    private String title;
    private String message;
    private MessageType messageType;
    private String viewDescriptor;
    private String viewClass;
    private Object data;
    private java.util.Map<String, String> fieldErrors;
    private String resumeToken;

    /** A yes/no question. The client answers with a {@code boolean}. */
    public static ActionFlowStep confirm(String message, String title) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.CONFIRM;
        step.message = message;
        step.title = title;
        return step;
    }

    /** Shorthand for {@link #confirm(String, String)} with no title. */
    public static ActionFlowStep confirm(String message) {
        return confirm(message, null);
    }

    /** A single-value prompt. The client answers with a {@code String}/{@code Number}. */
    public static ActionFlowStep input(String message, String title) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.INPUT;
        step.message = message;
        step.title = title;
        return step;
    }

    /**
     * Renders {@code viewDescriptor} as a form (prefilled with {@code data}, when given) and collects the
     * submitted form as the answer.
     */
    public static ActionFlowStep dialog(String viewDescriptor, Object data, String title) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.DIALOG;
        step.viewDescriptor = viewDescriptor;
        step.data = data;
        step.title = title;
        return step;
    }

    /**
     * Same as {@link #dialog(String, Object, String)} for a form of {@code viewClass}, which may differ from the entity
     * the action runs on.
     */
    public static ActionFlowStep dialog(String viewDescriptor, String viewClass, Object data, String title) {
        var step = dialog(viewDescriptor, data, title);
        step.viewClass = viewClass;
        return step;
    }

    /**
     * The client shows {@code viewDescriptor} of {@code viewClass} read only, filled with {@code data}, and answers when
     * the user closes it.
     */
    public static ActionFlowStep view(String viewDescriptor, String viewClass, Object data, String title) {
        var step = dialog(viewDescriptor, viewClass, data, title);
        step.type = ActionFlowStepType.VIEW;
        return step;
    }

    /** Fire-and-forget notification — the client shows it and immediately continues the flow. */
    public static ActionFlowStep notify(String message, MessageType messageType) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.NOTIFY;
        step.message = message;
        step.messageType = messageType;
        return step;
    }

    /**
     * The client navigates to {@code url} and the flow ends there (relative or http(s) URLs only).
     * <b>Experimental:</b> {@code awaitReturn = true} is rejected by the Vue client for now.
     */
    public static ActionFlowStep redirect(String url, boolean awaitReturn) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.REDIRECT;
        step.data = Map.of("url", url, "awaitReturn", awaitReturn);
        return step;
    }

    /**
     * Same as {@link #redirect(String, boolean)}, asking the client to open the URL in a new window or tab.
     */
    public static ActionFlowStep redirect(String url, boolean awaitReturn, boolean newWindow) {
        var step = redirect(url, awaitReturn);
        step.data = Map.of("url", url, "awaitReturn", awaitReturn, "newWindow", newWindow);
        return step;
    }

    /** Shorthand for {@link #redirect(String, boolean)} that does not wait for the client to come back. */
    public static ActionFlowStep redirect(String url) {
        return redirect(url, false);
    }

    /**
     * The client invokes {@code actionId} first (add {@code "className"} to {@code data} for an entity-scoped
     * action; other entries become the called action's request data) and resumes this flow with the called
     * action's response as the answer. <b>Experimental.</b>
     */
    public static ActionFlowStep call(String actionId, Map<String, Object> data) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.CALL;
        var payload = new HashMap<String, Object>();
        payload.put("action", actionId);
        if (data != null) {
            payload.putAll(data);
        }
        step.data = payload;
        return step;
    }

    /**
     * The client asks the user to choose among options and answers with the <b>keys</b> chosen, never with positions.
     *
     * @param title    title of the picker, may be null
     * @param keys     stable key of each option
     * @param labels   what to show for each option, in the same order as {@code keys}
     * @param multiple whether several can be chosen
     */
    public static ActionFlowStep choice(String title, java.util.List<String> keys, java.util.List<String> labels, boolean multiple) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.CHOICE;
        step.title = title;
        var payload = new HashMap<String, Object>();
        var options = new java.util.ArrayList<Map<String, String>>();
        for (int i = 0; i < keys.size(); i++) {
            options.add(Map.of("key", keys.get(i), "label", labels.get(i)));
        }
        payload.put("options", options);
        payload.put("multiple", multiple);
        step.data = payload;
        return step;
    }

    /**
     * The client asks the user for files, sends each one to {@code /api/app/transfers} and answers with a list of
     * {@code {ref}}: references, not content.
     *
     * @param title    title of the picker, may be null
     * @param accept   accepted types as in an HTML {@code accept} attribute, may be null
     * @param multiple whether several files can be chosen
     */
    public static ActionFlowStep upload(String title, String accept, boolean multiple) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.UPLOAD;
        step.title = title;
        var payload = new HashMap<String, Object>();
        payload.put("accept", accept);
        payload.put("multiple", multiple);
        step.data = payload;
        return step;
    }

    /**
     * An {@code UPLOAD} step with the limits the server enforces. The client answers with a list of references to files it
     * already sent to {@code /api/app/transfers}: {@code [{ref}]}.
     *
     * @param title        title of the file chooser
     * @param accept       accepted extensions and MIME types, may be null
     * @param maxFiles     maximum number of files
     * @param maxFileSize  maximum size of each file in bytes
     * @param maxTotalSize maximum size of all files together in bytes, 0 for no extra limit
     * @return the step
     */
    public static ActionFlowStep upload(String title, String accept, int maxFiles, long maxFileSize, long maxTotalSize) {
        var step = upload(title, accept, maxFiles > 1);
        @SuppressWarnings("unchecked")
        var payload = (Map<String, Object>) step.data;
        payload.put("maxFiles", maxFiles);
        payload.put("maxFileSize", maxFileSize);
        payload.put("maxTotalSize", maxTotalSize);
        return step;
    }

    /** Terminal step carrying the action's result. */
    public static ActionFlowStep done(Object data) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.DONE;
        step.data = data;
        return step;
    }

    /** Terminal step carrying the action's result plus a message the client shows (e.g. a toast) on the way out. */
    public static ActionFlowStep done(Object data, String message, MessageType messageType) {
        var step = done(data);
        step.message = message;
        step.messageType = messageType;
        return step;
    }

    /** Escape hatch: the client looks up a registered step renderer named {@code component}. */
    public static ActionFlowStep custom(String component, Map<String, Object> data) {
        var step = new ActionFlowStep();
        step.type = ActionFlowStepType.CUSTOM;
        var payload = new HashMap<String, Object>();
        payload.put("component", component);
        if (data != null) {
            payload.putAll(data);
        }
        step.data = payload;
        return step;
    }

    public String getFlowId() {
        return flowId;
    }

    /** Stamped by {@link ActionFlows} — not meant to be set by action authors. */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public ActionFlowStepType getType() {
        return type;
    }

    public void setType(ActionFlowStepType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getViewDescriptor() {
        return viewDescriptor;
    }

    /**
     * For {@code DIALOG}: class name of the bean {@link #getViewDescriptor()} belongs to, when it is not the entity the
     * action runs on. {@code null} means the entity of the request.
     */
    public String getViewClass() {
        return viewClass;
    }

    public void setViewClass(String viewClass) {
        this.viewClass = viewClass;
    }

    public void setViewDescriptor(String viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    /**
     * @return when a form is shown again after a validation error, the message of each field that failed, by field name;
     * otherwise {@code null}
     */
    public java.util.Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(java.util.Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getResumeToken() {
        return resumeToken;
    }

    /** Stamped by {@link ActionFlows} — not meant to be set by action authors. */
    public void setResumeToken(String resumeToken) {
        this.resumeToken = resumeToken;
    }
}
