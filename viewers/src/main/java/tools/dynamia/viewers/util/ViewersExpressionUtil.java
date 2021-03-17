/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.viewers.util;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import tools.dynamia.integration.ContainersBeanResolver;


/**
 * The Class ViewersExpressionUtil.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewersExpressionUtil {

    /**
     * The parser.
     */
    private static final ExpressionParser parser;

    /**
     * The context.
     */
    private static final StandardEvaluationContext context;

    static {
        parser = new SpelExpressionParser();
        context = new StandardEvaluationContext();
        context.setBeanResolver(new ContainersBeanResolver());
    }

    /**
     * Checks if is expression.
     *
     * @param value the value
     * @return true, if is expression
     */
    public static boolean isExpression(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    /**
     * Checks if is locale expression.
     *
     * @param value the value
     * @return true, if is locale expression
     */
    public static boolean isLocaleExpression(String value) {
        return value != null && value.startsWith("#{") && value.endsWith("}");
    }

    /**
     * Gets the expression value.
     *
     * @param text the text
     * @return the expression value
     */
    public static Object getExpressionValue(String text) {
        Expression exp = parseExpression(text);
        if (exp != null) {
            return exp.getValue();
        }
        return text;
    }

    /**
     * Parses the expression.
     *
     * @param text the text
     * @return the expression
     */
    public static Expression parseExpression(String text) {
        if (isExpression(text)) {
            //Remove ${ and } from start and end
            String expString = text.substring(2, text.length() - 1);
            return parser.parseExpression(expString);
        } else {
            return null;
        }
    }

    /**
     * $s.
     *
     * @param text the text
     * @return the string
     */
    public static String $s(String text) {
        Expression exp = parseExpression(text);
        return exp != null ? exp.getValue(context, String.class) : text;
    }

    /**
     * $.
     *
     * @param text the text
     * @return the object
     */
    public static Object $(String text) {
        Expression exp = parseExpression(text);
        return exp != null ? exp.getValue(context) : null;
    }

    private ViewersExpressionUtil() {
    }

}
