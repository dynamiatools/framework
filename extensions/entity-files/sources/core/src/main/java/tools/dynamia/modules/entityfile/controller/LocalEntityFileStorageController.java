package tools.dynamia.modules.entityfile.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tools.dynamia.integration.sterotypes.Controller;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler;

@Controller
public class LocalEntityFileStorageController {

    private final LocalEntityFileStorageHandler handler;

    public LocalEntityFileStorageController(LocalEntityFileStorageHandler handler) {
        this.handler = handler;
    }

    @GetMapping(value = "/storage/{file}")
    public ResponseEntity<Resource> get(@PathVariable String file, @RequestParam("uuid") String uuid, HttpServletRequest request) {
        var resource = handler.getResource(file, uuid, request);
        if (resource != null && resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
