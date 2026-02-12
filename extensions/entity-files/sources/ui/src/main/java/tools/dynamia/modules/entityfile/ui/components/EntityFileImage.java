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

package tools.dynamia.modules.entityfile.ui.components;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Image;

import tools.dynamia.commons.SimpleCache;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ImageCache;

import java.io.Serial;
import java.util.Objects;

public class EntityFileImage extends Image {

    private static final SimpleCache<Long, String> URL_CACHE = new SimpleCache<>();
    private static final SimpleCache<Long, String> URL_THUMB_CACHE = new SimpleCache<>();


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -2182747459195865750L;

    static {
        BindingComponentIndex.getInstance().put("value", EntityFileImage.class);
        ComponentAliasIndex.getInstance().put("entityfileImage", EntityFileImage.class);
    }

    private EntityFile entityFile;
    private boolean thumbnail = false;
    private int thumbnailHeight = 64;
    private int thumbnailWidth = 64;
    private String noPhotoPath = "/static/dynamia-tools/images/no-photo.jpg";
    private String entityFileUuid;

    public EntityFile getValue() {
        return entityFile;
    }

    public void setValue(EntityFile entityFile) {
        this.entityFile = entityFile;
        loadImage();
    }

    public void setValue(String uuid) {
        setEntityFileUuid(uuid);
    }

    private void loadImage() {
        if (entityFile != null) {
            Long key = entityFile.getId();
            try {
                StoredEntityFile sef = entityFile.getStoredEntityFile();

                if (isThumbnail()) {
                    String thumbUrl = URL_THUMB_CACHE.getOrLoad(key, k -> entityFile.getStoredEntityFile().getThumbnailUrl(thumbnailWidth, thumbnailHeight));
                    setSrc(thumbUrl);
                } else {
                    setSrc(URL_CACHE.getOrLoad(key, k -> entityFile.toURL()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadNoPhoto();
            }
        } else {
            loadNoPhoto();
        }

        if (isThumbnail()) {
            setStyle("max-height: " + thumbnailHeight + "px; max-width: " + thumbnailWidth + "px");
        } else {
            setStyle(null);
        }
    }

    private void loadNoPhoto() {
        try {
            if (noPhotoPath != null) {
                if (noPhotoPath.startsWith("classpath")) {
                    AImage imageContent = ImageCache.get(noPhotoPath);
                    if (imageContent == null) {
                        Resource resource = IOUtils.getResource(noPhotoPath);
                        if (resource.exists()) {
                            imageContent = new AImage(resource.getFilename(), resource.getInputStream());
                            ImageCache.add(noPhotoPath, imageContent);
                        }
                    }

                    if (imageContent != null) {
                        setContent(imageContent);
                    }

                } else {
                    setSrc(noPhotoPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;

    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;

    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;

    }

    public String getNoPhotoPath() {
        return noPhotoPath;
    }

    public void setNoPhotoPath(String noPhotoPath) {
        this.noPhotoPath = noPhotoPath;

    }

    @Override
    public void setParent(Component parent) {
        super.setParent(parent);
        loadImage();
    }

    /**
     * Clear URL cache
     */
    public static void clearCache() {
        URL_CACHE.clear();
        URL_THUMB_CACHE.clear();
    }

    /**
     * Clear entity file cache
     *
     * @param file
     */
    public static void clearCache(EntityFile file) {
        if (file != null && file.getId() != null) {
            URL_CACHE.remove(file.getId());
            URL_THUMB_CACHE.remove(file.getId());
        }
    }

    public String getEntityFileUuid() {
        return entityFileUuid;
    }

    public void setEntityFileUuid(String entityFileUuid) {
        var old = this.entityFileUuid;
        if (!Objects.equals(old, entityFileUuid)) {
            this.entityFileUuid = entityFileUuid;
            loadEntityFileByUuid();
        }
    }

    private void loadEntityFileByUuid() {
        if (entityFileUuid != null && !entityFileUuid.isBlank()) {
            var service = Containers.get().findObject(EntityFileService.class);
            if (service != null) {
                setValue(service.getEntityFile(entityFileUuid));
            }
        }
    }
}
