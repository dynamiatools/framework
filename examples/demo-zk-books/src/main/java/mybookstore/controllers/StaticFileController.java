package mybookstore.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import tools.dynamia.integration.sterotypes.Controller;

@Controller
public class StaticFileController {

    @GetMapping("/front")
    public String file() {
        System.out.println("Loading static /file");
        return "static/file.html";
    }

    @GetMapping("/app")
    public String app() {
        System.out.println("Loading static /app");
        return "static/app/index.html";
    }

    @GetMapping("/components")
    public String components() {

        return "components";
    }

    @GetMapping("/books")
    public String books() {

        return "books";
    }

    /**
     * Should resolver view to views/html-view.html
     *
     * @return
     */
    @GetMapping("/other-front")
    public String htmlView() {
        return "custom-view";
    }
}
