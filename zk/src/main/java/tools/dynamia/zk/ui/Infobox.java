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

import org.zkoss.zhtml.I;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.Objects;

public class Infobox extends Div {

    static {
        ComponentAliasIndex.getInstance().put("infobox", Infobox.class);
        BindingComponentIndex.getInstance().put("number", Infobox.class);
    }

    private String icon;
    private String text;
    private String number;
    private int progress;
    private boolean showProgress = false;
    private String progressDescription;
    private String background;
    private String iconBackground;

    private final Div uiProgress;
    private final Div uiProgressBar;
    private final Label uiProgressDescription;
    private final Div uiContent;
    private final Span uiIcon;
    private final Label uiText;
    private final Label uiNumber;


    public Infobox() {
        setZclass("info-box");

        uiIcon = new Span();
        uiIcon.setZclass("info-box-icon");
        appendChild(uiIcon);

        uiContent = new Div();
        uiContent.setZclass("info-box-content");
        appendChild(uiContent);

        uiText = new Label();
        uiText.setZclass("info-box-text");
        uiContent.appendChild(uiText);

        uiNumber = new Label();
        uiNumber.setZclass("info-box-number");
        uiContent.appendChild(uiNumber);

        uiProgress = new Div();
        uiProgress.setZclass("progress");

        uiProgressBar = new Div();
        uiProgressBar.setZclass("progress-bar");
        uiProgress.appendChild(uiProgressBar);

        uiProgressDescription = new Label();

    }

    private void updateProgress() {
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }
        uiProgressBar.setStyle("width: " + progress + "%");
        uiProgressDescription.setValue(progressDescription);

    }


    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
        if (showProgress) {
            uiContent.appendChild(uiProgress);
            uiContent.appendChild(uiProgressDescription);
        } else {
            uiContent.removeChild(uiProgress);
            uiContent.removeChild(uiProgressDescription);
        }

    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        if (!Objects.equals(this.icon, icon)) {
            this.icon = icon;
            uiIcon.getChildren().clear();
            I i = new I();
            i.setSclass(icon);
            uiIcon.appendChild(i);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            uiText.setValue(text);
        }
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if (!Objects.equals(this.number, number)) {
            this.number = number;
            uiNumber.setValue(number);
            Events.postEvent(new Event(Events.ON_CHANGE, this));
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        updateProgress();
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public String getProgressDescription() {
        return progressDescription;
    }

    public void setProgressDescription(String progressDescription) {
        this.progressDescription = progressDescription;
        updateProgress();
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        if (!Objects.equals(this.background, background)) {
            this.background = background;
            setZclass("info-box " + background);
        }
    }


    public String getIconBackground() {
        return iconBackground;
    }

    public void setIconBackground(String iconBackground) {
        if (!Objects.equals(this.iconBackground, iconBackground)) {
            this.iconBackground = iconBackground;
            uiIcon.setSclass(iconBackground);
        }
    }
}
