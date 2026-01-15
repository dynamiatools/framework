package tools.dynamia.modules.entityfile.ui;

import tools.dynamia.integration.scheduling.MorningTask;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.entityfile.ui.components.EntityFileImage;

@Provider
public class AutoclearEntityFileImageCacheJob implements MorningTask {

    @Override
    public void execute() {
        EntityFileImage.clearCache();
    }
}
