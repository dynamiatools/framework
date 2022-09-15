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

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.URLable;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ImageCache;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class CoolLabel extends Div implements LoadableOnly {

    public static final String ON_TITLE_CHANGED = "onTitleChanged";
    /**
     *
     */
    private static final long serialVersionUID = 8628833708100482613L;

    static {
        BindingComponentIndex.getInstance().put("title", CoolLabel.class);
        ComponentAliasIndex.getInstance().add(CoolLabel.class);
    }

    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Label descriptionLabel;
    private Image image;
    private File imageFile;
    private String imageURL;
    private String noImagePath = "/zkau/web/tools/images/no-photo.jpg";
    private Image noImage;
    private int progress;
    private final Progressmeter progressmeter;
    private boolean showImage = true;
    private boolean breakLinesSubtitle;
    private boolean breakLinesDescription;

    private String title;

    public CoolLabel() {
        setSclass("cool-lb");

        titleLabel = new Label();
        titleLabel.setSclass("cool-lb-title");
        titleLabel.setStyle("display: block");

        subtitleLabel = new Label();
        subtitleLabel.setSclass("cool-lb-subtitle");
        subtitleLabel.setStyle("display: block");

        descriptionLabel = new Label();
        descriptionLabel.setSclass("cool-lb-description");
        descriptionLabel.setStyle("display: block");

        progressmeter = new Progressmeter();
        progressmeter.setSclass("cool-lb-progress");
        progressmeter.setWidth("100%");
        progressmeter.setHeight("10px");
        setNoImagePath(noImagePath);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }
        progressmeter.setValue(progress);
        layout();
    }

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
        layout();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        if (this.image != image) {
            this.image = image;
            if (this.image != null) {
                this.image.setSclass("cool-lb-image");
            }
            layout();
        }
    }

    public void setImageFile(File file) {
        this.imageFile = file;
        try {
            if (file != null) {
                image = new Image();
                image.setContent(new AImage(file));
                setImage(image);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public File getImageFile() {
        return imageFile;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
        if (imageURL != null) {
            setImage(loadImageFromURL(imageURL));
        }
    }

    public void setImageURL(URLable urlable) {
        if (urlable != null) {
            setImageURL(urlable.toURL());
        }
    }

    private Image loadImageFromURL(String imageURL) {
        Image newimage = new Image();
        if (imageURL.startsWith("classpath:")) {
            try {
                AImage imageContent = ImageCache.get(imageURL);
                if (imageContent == null) {
                    Resource resource = IOUtils.getResource(imageURL);
                    if (resource.exists()) {
                        imageContent = new AImage(resource.getFilename(), resource.getInputStream());
                        ImageCache.add(imageURL, imageContent);
                    }
                }

                if (imageContent != null) {
                    newimage.setContent(imageContent);
                }
            } catch (Exception e) {
                // ignore
            }
        } else {
            newimage.setSrc(imageURL);
        }
        return newimage;
    }

    public void setTitle(String title) {
        if (!Objects.equals(this.title,title)) {
            this.title = title;
            titleLabel.setValue(title);
            fireOnTitleChanged();
        }
    }

    public String getTitle() {
        return this.title;
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setValue(subtitle);
    }

    public String getSubtitle() {
        return subtitleLabel.getValue();
    }

    public void setDescription(String description) {
        descriptionLabel.setValue(description);
    }

    public String getDescription() {
        return descriptionLabel.getValue();
    }

    public String getNoImagePath() {
        return noImagePath;
    }

    public void setNoImagePath(String noImagePath) {
        this.noImagePath = noImagePath;
        if (noImagePath != null && !noImagePath.isEmpty()) {
            this.noImage = loadImageFromURL(noImagePath);
            if (this.noImage != null) {
                this.noImage.setSclass("cool-lb-image");
            }
        }
    }

    @Override
    public void setParent(Component parent) {
        layout();
        super.setParent(parent);
    }

    private void layout() {
        getChildren().clear();

        Hlayout hlayout = new Hlayout();
        hlayout.setClass("cool-lb-hl");

        if (isShowImage()) {
            if (image == null && noImage != null) {
                noImage.setParent(hlayout);
            }
            if (image != null) {
                image.setParent(hlayout);
            }
        }

        Div div = new Div();
        div.setSclass("cool-lb-labels");
        titleLabel.setParent(div);
        subtitleLabel.setParent(div);
        descriptionLabel.setParent(div);

        div.setParent(hlayout);
        appendChild(hlayout);

        if (progress > 0) {
            progressmeter.setParent(this);
        }
    }

    private void fireOnTitleChanged() {
        Events.postEvent(new Event(ON_TITLE_CHANGED, this, getTitle()));

    }

    public boolean isBreakLinesSubtitle() {
        return breakLinesSubtitle;
    }

    public void setBreakLinesSubtitle(boolean breakLinesSubtitle) {
        this.breakLinesSubtitle = breakLinesSubtitle;
        if(breakLinesSubtitle){
            subtitleLabel.setStyle("display: block; white-space: pre-line");
        }else{
            subtitleLabel.setStyle("display: block");
        }
    }

    public boolean isBreakLinesDescription() {
        return breakLinesDescription;
    }

    public void setBreakLinesDescription(boolean breakLinesDescription) {
        this.breakLinesDescription = breakLinesDescription;
        if(breakLinesDescription){
            descriptionLabel.setStyle("display: block; white-space: pre-line");
        }else{
            descriptionLabel.setStyle("display: block");
        }
    }
}
