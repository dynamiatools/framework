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
package tools.dynamia.zk.ui;

import org.zkoss.zul.Center;
import org.zkoss.zul.Include;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ContentArea extends Center  implements LoadableOnly{

    private boolean autotitle = true;
    private String contentClass;
    private final Include content;

    public ContentArea() {
        content = new Include();
        content.setId("content");
        content.setWidth("100%");
        content.setHeight("100%");
        content.setParent(this);
    }

    public boolean isAutotitle() {
        return autotitle;
    }

    public void setAutotitle(boolean autotitle) {
        this.autotitle = autotitle;
    }

    public String getContentClass() {
        return contentClass;
    }

    public void setContentClass(String contentClass) {
        this.contentClass = contentClass;
        this.content.setSclass(contentClass);
    }
}
