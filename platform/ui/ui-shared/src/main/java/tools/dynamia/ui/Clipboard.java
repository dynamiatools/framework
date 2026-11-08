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

/**
 * Port that puts a text in the user's clipboard. Browsers only allow writing the clipboard from a user gesture, so an
 * adapter may have to show the text instead; the contract is that the text reaches the user, not that it is in the
 * clipboard.
 */
@UIPort(name = "clipboard")
public interface Clipboard {

    /**
     * Copies {@code text} to the clipboard of the user, or shows it so the user can copy it.
     *
     * @param text the text; never {@code null}
     */
    void copy(String text);
}
