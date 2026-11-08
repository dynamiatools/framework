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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.util.Clients;
import tools.dynamia.ui.Clipboard;

/**
 * ZK {@link Clipboard}: writes the clipboard from the browser and, if the browser refuses, shows the text in a prompt so
 * the user can copy it by hand.
 */
public class ZKClipboard implements Clipboard {

    @Override
    public void copy(String text) {
        Clients.evalJavaScript(script(text));
    }

    /**
     * Builds the client script. The text is embedded as a JSON string literal, so quotes, line breaks and
     * {@code </script>} cannot break out of it.
     *
     * @param text the text to copy
     * @return the JavaScript to run in the browser
     */
    static String script(String text) {
        String literal = jsString(text);
        return "(function(t){var f=function(){window.prompt('Copy', t);};"
                + "if(navigator.clipboard&&navigator.clipboard.writeText){navigator.clipboard.writeText(t).catch(f);}else{f();}"
                + "})(" + literal + ");";
    }

    static String jsString(String text) {
        var out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '<' -> out.append("\\u003c");
                case '\u2028' -> out.append("\\u2028");
                case '\u2029' -> out.append("\\u2029");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
