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
package tools.dynamia.ui;

import java.util.Objects;

/**
 * Facade to give a text to the user's clipboard from an action, without knowing the front end.
 *
 * <pre>{@code
 * UIClipboard.copy(entityFile.toURL());
 * }</pre>
 *
 * In a remote run the text travels in the final response ({@code params.clipboard}), so it is delivered once, by the pass
 * that ran to the end.
 */
public final class UIClipboard {

    private UIClipboard() {
    }

    /**
     * Copies a text to the user's clipboard, or shows it when the browser refuses.
     *
     * @param text the text to copy
     * @throws NullPointerException if {@code text} is {@code null}
     * @throws UIUnavailableException when the current environment has no UI
     */
    public static void copy(String text) {
        Objects.requireNonNull(text, "text");
        UIFacades.port(Clipboard.class).copy(text);
    }
}
