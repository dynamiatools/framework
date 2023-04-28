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
package tools.dynamia.viewers.impl;

import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.ViewDescriptorInterceptorUtils;
import tools.dynamia.viewers.util.ViewDescriptorReaderUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;

/**
 * A factory for creating AbstractViewDescriptor objects.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public abstract class AbstractViewDescriptorFactory implements ViewDescriptorFactory {

    private static final String DEFAULT_DEVICE = "screen";

    /**
     * The Constant FULLYLOADED.
     */
    private final static String FULLYLOADED = "FullyLoaded";

    /**
     * The logger.
     */
    private final LoggingService logger = new SLF4JLoggingService(getClass());

    /**
     * View Descriptors Cache
     */
    // devices = [type = [class = descriptor]]
    private final SimpleCache<String, SimpleCache<String, SimpleCache<Class, ViewDescriptor>>> descriptors = new SimpleCache<>();

    /**
     * The last changes.
     */
    private final SimpleCache<ViewDescriptor, ViewDescriptorMetainfo> lastChanges = new SimpleCache<>();

    /**
     * The all descriptors.
     */
    private final Set<ViewDescriptor> allDescriptors = new HashSet<>();

    /**
     * The descriptors location.
     */
    private String descriptorsLocation = "classpath*:META-INF/descriptors/**/*.*";

    /**
     * The autoreload mode.
     */
    private boolean autoreloadMode = true;

    @Override
    public ViewDescriptor getDescriptor(Class beanClass, String device, String viewType) {
        if (allDescriptors.isEmpty()) {
            loadViewDescriptors();
        }

        logger.debug("Getting view descriptor for " + beanClass + "  type:" + viewType);

        ViewDescriptor viewDescriptor = findDescriptor(beanClass, device, viewType);

        // ViewDescriptor not found. Keep searching in super classes
        if (viewDescriptor == null) {
            viewDescriptor = findAndMergeWithParent(beanClass, viewType, viewDescriptor);
        }

        if (viewDescriptor == null) {
            logger.warn("NO view descriptor found for " + beanClass + ":" + viewType + ":" + device
                    + " using default view descriptor");
            viewDescriptor = getDefaultViewDescriptor(beanClass, viewType);
        } else if (isAutoreloadMode()) {
            ViewDescriptorMetainfo metainfo = lastChanges.get(viewDescriptor);
            ViewDescriptor reloaded = reloadViewDescriptor(viewDescriptor, metainfo);
            if (reloaded != null) {
                addViewDescriptor(reloaded);
                viewDescriptor = reloaded;
            }
        }
        viewDescriptor = fullyLoad(viewDescriptor, beanClass, viewType);
        return viewDescriptor;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptorFactory#getDescriptor(java.lang
     * .Class, java.lang.String)
     */
    @Override
    public ViewDescriptor getDescriptor(Class beanClass, String viewType) {
        return getDescriptor(beanClass, DEFAULT_DEVICE, viewType);
    }

    /**
     * Fully load.
     *
     * @param viewDescriptor the view descriptor
     * @param beanClass      the bean class
     * @param viewType       the view type
     * @return the view descriptor
     */
    private ViewDescriptor fullyLoad(ViewDescriptor viewDescriptor, Class beanClass, String viewType) {
        if (!isFullyLoaded(viewDescriptor)) {
            String extendsValue = "";
            if (viewDescriptor.getExtends() != null) {
                extendsValue = viewDescriptor.getExtends();
            }

            if (extendsValue.equals("parent") && beanClass != null) {
                viewDescriptor = findAndMergeWithParent(beanClass, viewType, viewDescriptor);
            } else if (!extendsValue.isEmpty()) {
                ViewDescriptor parent = getDescriptor(extendsValue);
                merge(viewDescriptor, parent);
            }

            setFullyLoaded(viewDescriptor, true);
        }
        return viewDescriptor;
    }

    /**
     * Find and merge with parent.
     *
     * @param beanClass      the bean class
     * @param viewType       the view type
     * @param viewDescriptor the view descriptor
     * @return the view descriptor
     */
    private ViewDescriptor findAndMergeWithParent(Class beanClass, String viewType, ViewDescriptor viewDescriptor) {
        if (viewDescriptor == null) {
            viewDescriptor = getDefaultViewDescriptor(beanClass, viewType);
        }
        if (viewDescriptor.isAutofields() || viewDescriptor.getExtends() != null) {
            ViewDescriptor parentViewDescriptor = getParentViewDescriptor(beanClass, viewType);

            if (parentViewDescriptor != null) {
                DefaultViewDescriptor result = new DefaultViewDescriptor(beanClass, viewType,
                        viewDescriptor.isAutofields());

                merge(result, parentViewDescriptor);
                merge(result, viewDescriptor);
                return result;
            }
        }
        return viewDescriptor;
    }

    /**
     * Merge.
     *
     * @param viewDescriptor      the view descriptor
     * @param otherViewDescriptor the other view descriptor
     */
    private void merge(ViewDescriptor viewDescriptor, ViewDescriptor otherViewDescriptor) {
        if (viewDescriptor instanceof MergeableViewDescriptor && otherViewDescriptor != null) {
            MergeableViewDescriptor mvd = (MergeableViewDescriptor) viewDescriptor;
            mvd.merge(otherViewDescriptor);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptorFactory#getDescriptor(java.lang
     * .String)
     */

    @Override
    public ViewDescriptor getDescriptor(String id) {
        return getDescriptor(id, DEFAULT_DEVICE);
    }

    /**
     * Get a view descriptor by id and device
     *
     * @param id              View descriptor id
     * @param preferredDevice (screen, smartphone, tablet)
     * @return
     */
    @Override
    public ViewDescriptor getDescriptor(String id, String preferredDevice) {
        final String device = preferredDevice != null ? preferredDevice : DEFAULT_DEVICE;

        if (allDescriptors.isEmpty()) {
            loadViewDescriptors();
        }

        ViewDescriptor viewDescriptor = allDescriptors.stream()
                .filter(vd -> vd.getId().equals(id))
                .filter(vd -> vd.getDevice().equals(device))
                .findFirst()
                .orElse(null);

        if (viewDescriptor == null) {
            if (device.equals(DEFAULT_DEVICE)) {
                throw new ViewDescriptorNotFoundException("Cannot found view descriptor using id: " + id);
            } else {
                return null;
            }
        } else if (isAutoreloadMode()) {
            ViewDescriptorMetainfo metainfo = lastChanges.get(viewDescriptor);
            ViewDescriptor reloaded = reloadViewDescriptor(viewDescriptor, metainfo);
            if (reloaded != null) {
                if (viewDescriptor.getBeanClass() != null) {
                    var devicesCache = descriptors.get(viewDescriptor.getDevice());
                    if (devicesCache != null) {
                        var viewTypeCache = devicesCache.get(viewDescriptor.getViewTypeName());
                        if (viewTypeCache != null) {
                            viewTypeCache.remove(viewDescriptor.getBeanClass());
                        }
                    }
                }
                allDescriptors.remove(viewDescriptor);

                addViewDescriptor(reloaded);
                viewDescriptor = reloaded;
            }
        }
        viewDescriptor = fullyLoad(viewDescriptor, viewDescriptor.getBeanClass(), id);

        return viewDescriptor;

    }

    /**
     * Gets the default view descriptor.
     *
     * @param beanClass the bean class
     * @param viewType  the view type
     * @return the default view descriptor
     */
    public abstract ViewDescriptor getDefaultViewDescriptor(Class beanClass, String viewType);

    /**
     * Adds the view descriptor.
     *
     * @param viewDescriptor the view descriptor
     */
    public void addViewDescriptor(ViewDescriptor viewDescriptor) {
        if (viewDescriptor != null) {
            if (viewDescriptor.getBeanClass() != null) {
                var classDescriptorCache = descriptors.get(viewDescriptor.getDevice());

                if (classDescriptorCache == null) {
                    classDescriptorCache = new SimpleCache<>();
                    descriptors.add(viewDescriptor.getDevice(), classDescriptorCache);
                }

                var descriptorsByTypeCache = classDescriptorCache.get(viewDescriptor.getViewTypeName());
                if (descriptorsByTypeCache == null) {
                    descriptorsByTypeCache = new SimpleCache<>();
                    classDescriptorCache.add(viewDescriptor.getViewTypeName(), descriptorsByTypeCache);
                }
                descriptorsByTypeCache.add(viewDescriptor.getBeanClass(), viewDescriptor);
            }
            allDescriptors.add(viewDescriptor);

        }
    }

    /**
     * Load view descriptors.
     */
    @Override
    public void loadViewDescriptors() {
        loadViewDescriptorFromProviders(Containers.get().findObjects(ViewDescriptorsProvider.class));
        loadViewDescriptorsFromFiles();
    }

    protected void loadViewDescriptorFromProviders(Collection<ViewDescriptorsProvider> providers) {
        if (providers != null && !providers.isEmpty()) {
            providers.forEach(provider -> {
                logger.info("Loading view descriptors from provider: " + provider.getClass());
                provider.getDescriptors().forEach(newViewDescriptor -> {
                    addViewDescriptor(newViewDescriptor);
                    ViewDescriptorInterceptorUtils.fireInterceptorsFor(newViewDescriptor);
                });
            });
        }
    }

    protected void loadViewDescriptorsFromFiles() {
        try {
            logger.info("Loading view Descriptors from location " + getDescriptorsLocation());
            Resource[] descFiles = IOUtils.getResources(getDescriptorsLocation());

            if (descFiles == null || descFiles.length == 0) {
                logger.info("No view descriptors found in " + getDescriptorsLocation());
                return;
            }

            logger.info(descFiles.length + " view descriptors found");
            for (Resource resource : descFiles) {
                logger.info("Reading view descriptor " + resource.getFilename());

                createNewViewDescriptor(resource);
            }

        } catch (IOException ex) {
            logger.error("Error loading view descriptors", ex);
        }
    }

    /**
     * Creates a new AbstractViewDescriptor object.
     *
     * @param resource the resource
     * @return the view descriptor
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private ViewDescriptor createNewViewDescriptor(Resource resource) throws IOException {
        ViewDescriptor newViewDescriptor = read(resource);
        addViewDescriptor(newViewDescriptor);
        if (newViewDescriptor != null && isAutoreloadMode()) {
            lastChanges.add(newViewDescriptor, new ViewDescriptorMetainfo(resource, resource.getLastModified()));
        }
        ViewDescriptorInterceptorUtils.fireInterceptorsFor(newViewDescriptor);
        return newViewDescriptor;
    }

    /**
     * Reload view descriptor.
     *
     * @param original the original
     * @param metainfo the metainfo
     * @return the view descriptor
     */
    private ViewDescriptor reloadViewDescriptor(ViewDescriptor original, ViewDescriptorMetainfo metainfo) {
        try {
            if (metainfo != null && metainfo.changed()) {
                logger.info("Reloading ViewDescriptor for " + original.getBeanClass() + " --> "
                        + original.getViewTypeName());
                ViewDescriptor reloaded = read(metainfo.getResource());
                metainfo.update();
                lastChanges.remove(original);
                lastChanges.add(reloaded, metainfo);
                return reloaded;
            }
        } catch (IOException iOException) {
            logger.error("Error reloading viewdescriptor: " + iOException.getMessage());
        }
        return null;
    }

    /**
     * Read.
     *
     * @param resource the resource
     * @return the view descriptor
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private ViewDescriptor read(Resource resource) throws IOException {
        ViewDescriptor viewDescriptor = null;
        ViewDescriptorReader reader = ViewDescriptorReaderUtils.getReaderFor(resource.getFileExtension());
        if (reader != null) {
            List<ViewDescriptorReaderCustomizer> customizers = ViewDescriptorReaderUtils.getCustomizers(reader);
            try (InputStreamReader streamReader = new InputStreamReader(resource.getInputStream())) {
                viewDescriptor = reader.read(resource, streamReader, customizers);
            }
        } else {
            logger.warn("No ViewDescriptorReader found for descriptor  " + resource.getFilename());
        }
        return viewDescriptor;
    }

    /**
     * Gets the descriptors location.
     *
     * @return the descriptors location
     */
    public String getDescriptorsLocation() {
        return descriptorsLocation;
    }

    /**
     * Sets the descriptors location.
     *
     * @param descriptorsLocation the new descriptors location
     */
    public void setDescriptorsLocation(String descriptorsLocation) {
        this.descriptorsLocation = descriptorsLocation;
    }

    /**
     * Checks if is autoreaload mode.
     *
     * @return true, if is autoreaload mode
     */
    public boolean isAutoreloadMode() {
        return autoreloadMode;
    }

    /**
     * Sets the autoreload mode.
     *
     * @param autoreloadMode the new autoreload mode
     */
    public void setAutoreloadMode(boolean autoreloadMode) {
        this.autoreloadMode = autoreloadMode;
    }

    @Override
    public ViewDescriptor findDescriptor(Class beanClass, String viewType) {
        return findDescriptor(beanClass, DEFAULT_DEVICE, viewType);

    }

    @Override
    public ViewDescriptor findDescriptor(Class beanClass, String device, String viewType) {
        if (device == null) {
            device = DEFAULT_DEVICE;
        }

        SimpleCache<String, SimpleCache<Class, ViewDescriptor>> classDescriptors = descriptors.get(device);
        SimpleCache<Class, ViewDescriptor> viewsDescriptors = null;

        if (classDescriptors != null) {
            viewsDescriptors = classDescriptors.get(viewType);
        }

        ViewDescriptor viewDescriptor = null;

        if (viewsDescriptors != null) {
            viewDescriptor = viewsDescriptors.get(beanClass);
        }

        if (viewDescriptor == null && !DEFAULT_DEVICE.equals(device)) {
            return findDescriptor(beanClass, DEFAULT_DEVICE, viewType);
        }

        return viewDescriptor;
    }

    /**
     * Gets the parent view descriptor.
     *
     * @param beanClass the bean class
     * @param viewType  the view type
     * @return the parent view descriptor
     */
    private ViewDescriptor getParentViewDescriptor(Class beanClass, String viewType) {
        ViewDescriptor vd = null;

        if (beanClass == null || viewType == null || viewType.isEmpty()) {
            return null;
        }

        if (beanClass.getSuperclass() != Object.class) {
            vd = getDescriptor(beanClass.getSuperclass(), viewType);
        } else {
            return null;
        }

        if (vd == null) {
            vd = getParentViewDescriptor(beanClass.getSuperclass(), viewType);
        }

        return vd;
    }

    /**
     * Sets the fully loaded.
     *
     * @param viewDescriptor the view descriptor
     * @param b              the b
     */
    private void setFullyLoaded(ViewDescriptor viewDescriptor, boolean b) {
        if (viewDescriptor != null) {
            viewDescriptor.getParams().put(FULLYLOADED, b);
        }
    }

    /**
     * Checks if is fully loaded.
     *
     * @param viewDescriptor the view descriptor
     * @return true, if is fully loaded
     */
    private boolean isFullyLoaded(ViewDescriptor viewDescriptor) {
        if (viewDescriptor != null) {
            return viewDescriptor.getParams().get(FULLYLOADED) == Boolean.TRUE;
        } else {
            return false;
        }
    }

    /**
     * Store view descriptor info about origin and modified time. Useful to check if viewdescriptor changed
     */
    private class ViewDescriptorMetainfo {

        /**
         * The resource.
         */
        private final Resource resource;

        /**
         * The last modified.
         */
        private long lastModified;

        /**
         * Instantiates a new view descriptor metainfo.
         *
         * @param viewDescriptor the view descriptor
         * @param lastModified   the last modified
         */
        public ViewDescriptorMetainfo(Resource viewDescriptor, long lastModified) {
            this.resource = viewDescriptor;
            this.lastModified = lastModified;
        }

        /**
         * Sets the last modified.
         *
         * @param lastModified the new last modified
         */
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        public Resource getResource() {
            return resource;
        }

        /**
         * Changed.
         *
         * @return true, if successful
         */
        private boolean changed() {
            try {
                return getResource().getLastModified() != lastModified;
            } catch (IOException iOException) {
                logger.error("Error getting lastModified from Resource " + getResource().getFilename() + ": "
                        + iOException.getMessage());
                return false;
            }
        }

        /**
         * Update.
         */
        private void update() {
            try {
                setLastModified(getResource().getLastModified());
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
    }
}
