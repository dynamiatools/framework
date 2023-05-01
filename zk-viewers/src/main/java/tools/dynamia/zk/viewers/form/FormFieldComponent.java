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

import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;

import java.io.Serializable;

public class FormFieldComponent implements Serializable {

    private final String fieldName;
    private final Label label;
    private final Component inputComponent;
    private Component inputComponentAlt;

    public FormFieldComponent(String fieldName, Component label, Component inputComponent) {
        super();
        this.fieldName = fieldName;
        this.label = new Label(label);
        this.inputComponent = inputComponent;
    }

    public FormFieldComponent(String fieldName, Component label, Component inputComponent, Component inputComponentAlt) {
        this(fieldName, label, inputComponent);
        this.inputComponentAlt = inputComponentAlt;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Label getLabel() {
        return label;
    }

    public Component getInputComponent() {
        return inputComponent;
    }

    public void remove() {
        if (label != null && label.delegated != null) {
            label.delegated.detach();
        }
        if (inputComponent != null) {
            inputComponent.detach();
        }

        if (inputComponentAlt != null) {
            inputComponentAlt.detach();
        }
    }

    public void hide() {
        if (label != null && label.delegated != null) {
            label.setVisible(false);
        }

        if (inputComponent != null) {
            inputComponent.setVisible(false);
        }
        if (inputComponentAlt != null) {
            inputComponentAlt.setVisible(false);
        }
    }

    public void show() {
        if (label != null && label.delegated != null) {
            label.setVisible(true);
        }

        if (inputComponent != null) {
            inputComponent.setVisible(true);
        }
        if (inputComponentAlt != null) {
            inputComponentAlt.setVisible(true);
        }
    }

    public Component getInputComponentAlt() {
        return inputComponentAlt;
    }


    public record Label(Component delegated) {

        public String getValue() {
                try {
                    if (delegated instanceof org.zkoss.zul.Label) {
                        return ((org.zkoss.zul.Label) delegated).getValue();
                    } else if (delegated instanceof org.zkoss.zhtml.Label) {
                        Text text = (Text) delegated.getFirstChild();
                        return text.getValue();
                    }
                } catch (Exception ignored) {

                }
                return "";
            }

            public void setValue(String value) {
                try {
                    if (delegated instanceof org.zkoss.zul.Label) {
                        ((org.zkoss.zul.Label) delegated).setValue(value);
                    } else if (delegated instanceof org.zkoss.zhtml.Label) {
                        Text text = (Text) delegated.getFirstChild();
                        text.setValue(value);
                    }
                } catch (Exception ignored) {

                }
            }

            public void setTooltiptext(String tooltiptext) {
                if (delegated instanceof HtmlBasedComponent) {
                    ((HtmlBasedComponent) delegated).setTooltiptext(tooltiptext);
                } else if (delegated instanceof org.zkoss.zhtml.Label) {
                    ((org.zkoss.zhtml.Label) delegated).setTitle(tooltiptext);
                }
            }

            public String getTooltiptext() {
                if (delegated instanceof HtmlBasedComponent) {
                    return ((HtmlBasedComponent) delegated).getTooltiptext();
                } else if (delegated instanceof org.zkoss.zhtml.Label) {
                    return ((org.zkoss.zhtml.Label) delegated).getTitle();
                }
                return null;
            }

            public void setSclass(String sclass) {
                if (delegated instanceof HtmlBasedComponent) {
                    ((HtmlBasedComponent) delegated).setSclass(sclass);
                } else if (delegated instanceof org.zkoss.zhtml.Label) {
                    ((org.zkoss.zhtml.Label) delegated).setSclass(sclass);
                }
            }

            public String getSclass() {
                if (delegated instanceof HtmlBasedComponent) {
                    return ((HtmlBasedComponent) delegated).getSclass();
                } else if (delegated instanceof org.zkoss.zhtml.Label) {
                    return ((org.zkoss.zhtml.Label) delegated).getSclass();
                }
                return null;
            }

            public void setVisible(boolean visible) {
                delegated.setVisible(visible);
            }

            public boolean isVisible() {
                return delegated.isVisible();
            }

        }

}
