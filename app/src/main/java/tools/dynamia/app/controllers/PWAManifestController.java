package tools.dynamia.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.web.pwa.PWAManifest;

/**
 * Controller to serve the PWA manifest file
 */
@RestController
public class PWAManifestController {

    private final PWAManifest manifest;

    public PWAManifestController(PWAManifest manifest) {
        this.manifest = manifest;
    }

    @GetMapping(value = "/manifest.json", produces = "application/manifest+json")
    public PWAManifest getManifest() {
        return manifest;
    }
}
