package tools.dynamia.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.integration.Containers;
import tools.dynamia.web.pwa.PWAManifest;

/**
 * REST controller to serve the Progressive Web App (PWA) manifest file.
 * <p>
 * Provides an endpoint to retrieve the manifest as JSON, enabling PWA features for the application.
 * <p>
 * Endpoint:
 * <ul>
 *   <li>GET /manifest.json - Returns the PWA manifest</li>
 * </ul>
 * <p>
 * When needed register using @Bean in a configuration class:
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@RestController
public class PWAManifestController {

    /**
     * The PWA manifest instance to be served.
     */
    private PWAManifest manifest;

    /**
     * Returns the PWA manifest as JSON.
     *
     * @return the {@link PWAManifest} object
     */
    @GetMapping(value = "/manifest.json", produces = "application/json")
    public ResponseEntity<PWAManifest> getManifest() {
        if (manifest == null) {
            manifest = Containers.get().findObject(PWAManifest.class);
        }

        return manifest != null ? ResponseEntity.ok(manifest) : ResponseEntity.notFound().build();
    }
}
