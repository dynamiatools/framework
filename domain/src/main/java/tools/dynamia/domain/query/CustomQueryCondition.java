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
package tools.dynamia.domain.query;

import java.util.function.Consumer;

/**
 *
 * @author Mario Serrano Leones
 */
public class CustomQueryCondition extends AbstractQueryCondition<Object> {

    private String text;
    private Consumer<AbstractQuery> applyConsumer;

    public CustomQueryCondition(String text) {
        this.text = text;
    }

    public CustomQueryCondition(String text, Consumer<AbstractQuery> applyConsumer) {
        this.text = text;
        this.applyConsumer = applyConsumer;
    }

    @Override
    public String render(String property) {
        return text;
    }

    @Override
    public void apply(String property, AbstractQuery query) {
        if (applyConsumer != null) {
            applyConsumer.accept(query);
        }
    }

    @Override
    protected String getOperator() {
        return "";
    }

}
