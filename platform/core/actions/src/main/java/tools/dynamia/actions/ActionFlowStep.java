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
    private Object data;
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

    public void setViewDescriptor(String viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
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
