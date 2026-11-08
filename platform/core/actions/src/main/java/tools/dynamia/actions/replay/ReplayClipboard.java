/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.actions.replay;

import tools.dynamia.ui.Clipboard;

/**
 * Headless {@link Clipboard}: the text is delivered with the final response, only by the pass that ran to the end.
 */
public final class ReplayClipboard implements Clipboard {

    private final ReplaySession session;

    /**
     * @param session the session of the pass
     */
    public ReplayClipboard(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void copy(String text) {
        session.copyToClipboard(text);
    }
}
