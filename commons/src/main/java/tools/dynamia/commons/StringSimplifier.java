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
package tools.dynamia.commons;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * The Class StringSimplifier.
 *
 * @author Mario A. Serrano Leones
 */
public class StringSimplifier {

    /**
     * The Constant DEFAULT_REPLACE_CHAR.
     */
    public static final char DEFAULT_REPLACE_CHAR = '-';

    /**
     * The Constant DEFAULT_REPLACE.
     */
    public static final String DEFAULT_REPLACE = String.valueOf(DEFAULT_REPLACE_CHAR);

    /**
     * The Constant NONDIACRITICS.
     */
    private static final Map<String, String> NONDIACRITICS = new MapBuilder<String, String>()
            .put(".", "")
            .put("\"", "")
            .put("'", "")
            //Keep relevant characters as seperation
            .put(" ", DEFAULT_REPLACE)
            .put("]", DEFAULT_REPLACE)
            .put("[", DEFAULT_REPLACE)
            .put(")", DEFAULT_REPLACE)
            .put("(", DEFAULT_REPLACE)
            .put("=", DEFAULT_REPLACE)
            .put("!", DEFAULT_REPLACE)
            .put("/", DEFAULT_REPLACE)
            .put("\\", DEFAULT_REPLACE)
            .put("&", DEFAULT_REPLACE)
            .put(",", DEFAULT_REPLACE)
            .put("?", DEFAULT_REPLACE)
            .put("°", DEFAULT_REPLACE) //Remove ?? is diacritic?
            .put("|", DEFAULT_REPLACE)
            .put("<", DEFAULT_REPLACE)
            .put(">", DEFAULT_REPLACE)
            .put(";", DEFAULT_REPLACE)
            .put(":", DEFAULT_REPLACE)
            .put("_", DEFAULT_REPLACE)
            .put("#", DEFAULT_REPLACE)
            .put("~", DEFAULT_REPLACE)
            .put("+", DEFAULT_REPLACE)
            .put("*", DEFAULT_REPLACE)
            //Replace non-diacritics as their equivalent characters
            .put("\u0141", "l") // BiaLystock
            .put("\u0142", "l") // Bialystock
            .put("ß", "ss")
            .put("æ", "ae")
            .put("ø", "o")
            .put("©", "c")
            .put("\u00D0", "d") // All �? ð from http://de.wikipedia.org/wiki/%C3%90
            .put("\u00F0", "d")
            .put("\u0110", "d")
            .put("\u0111", "d")
            .put("\u0189", "d")
            .put("\u0256", "d")
            .put("\u00DE", "th") // thorn Þ
            .put("\u00FE", "th") // thorn þ
            .build();

    /**
     * Simplified string.
     *
     * @param orig the orig
     * @return the string
     */
    public static String simplifiedString(String orig) {
        String str = orig;
        if (str == null) {
            return null;
        }
        str = stripDiacritics(str);
        str = stripNonDiacritics(str);
        if (str.length() == 0) {
            // Ugly special case to work around non-existing empty strings
            // in Oracle. Store original crapstring as simplified.
            // It would return an empty string if Oracle could store it.
            return orig;
        }
        return str.toLowerCase();
    }

    /**
     * Strip non diacritics.
     *
     * @param orig the orig
     * @return the string
     */
    private static String stripNonDiacritics(String orig) {
        StringBuilder ret = new StringBuilder();
        String lastchar = null;
        for (int i = 0; i < orig.length(); i++) {
            String source = orig.substring(i, i + 1);
            String replace = NONDIACRITICS.get(source);
            String toReplace = replace == null ? source : replace;
            if (DEFAULT_REPLACE.equals(lastchar) && DEFAULT_REPLACE.equals(toReplace)) {
                toReplace = "";
            } else {
                lastchar = toReplace;
            }
            ret.append(toReplace);
        }
        if (ret.length() > 0 && DEFAULT_REPLACE_CHAR == ret.charAt(ret.length() - 1)) {
            ret.deleteCharAt(ret.length() - 1);
        }
        return ret.toString();
    }

    /*
     Special regular expression character ranges relevant for simplification -> see http://docstore.mik.ua/orelly/perl/prog3/ch05_04.htm
     InCombiningDiacriticalMarks: special marks that are part of "normal" ä, ö, î etc..
     IsSk: Symbol, Modifier see http://www.fileformat.info/info/unicode/category/Sk/list.htm
     IsLm: Letter, Modifier see http://www.fileformat.info/info/unicode/category/Lm/list.htm
     */
    /**
     * The Constant DIACRITICS_AND_FRIENDS.
     */
    public static final Pattern DIACRITICS_AND_FRIENDS
            = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

    /**
     * Strip diacritics.
     *
     * @param str the str
     * @return the string
     */
    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }

    private StringSimplifier() {
    }
}
