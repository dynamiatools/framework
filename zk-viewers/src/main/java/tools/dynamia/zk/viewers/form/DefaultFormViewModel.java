
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

package tools.dynamia.zk.viewers.form;

import org.zkoss.zk.ui.event.Events;
import tools.dynamia.zk.util.ZKBindingUtil;

import static tools.dynamia.zk.viewers.form.FormView.ON_VALUE_CHANGED;

/**
 * @author Mario A. Serrano Leones
 */
public class DefaultFormViewModel<T> implements FormViewModel<T> {

    private T value;


    public DefaultFormViewModel() {

    }

    @Override
    public T getValue() {

        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        update();
        Events.postEvent(ON_VALUE_CHANGED, null, value);

    }

    private void update() {
        ZKBindingUtil.postNotifyChange(this);
    }


}
