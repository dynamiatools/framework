package mybookstore.handoff;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.NavigationManager;

import java.io.IOException;

/**
 * Real-world navigation hand-off cases for manual/automated verification of
 * {@code NavigationManagerSession} and its request-scope filter:
 * <ul>
 *   <li>{@code /demo/handoff/redirect} — records a page + callback, then redirects (like a ZK command
 *   that calls {@code setPageLater} and {@code sendRedirect}). The target desktop must open Invoices (not the default page).</li>
 *   <li>{@code /demo/handoff/desktop} — builds a ZK desktop <i>without</i> choosing a page itself, so it
 *   can only show what was handed off by a previous request (or the default page if nothing was).</li>
 *   <li>{@code /demo/handoff/login} — see {@link HandOffDemoLoginFilter}.</li>
 *   <li>{@code /demo/handoff/status} — how many queued callbacks have run.</li>
 * </ul>
 */
@Controller
public class HandOffDemoController {

    @GetMapping("/demo/handoff/redirect")
    public void redirect(HttpServletResponse response) throws IOException {
        NavigationManager.setPageLater("library/invoices");
        NavigationManager.runLater(HandOffDemoState.CALLBACKS_RUN::incrementAndGet);
        response.sendRedirect("/demo/handoff/desktop");
    }

    @GetMapping("/demo/handoff/desktop")
    public ModelAndView desktop() {
        return new ModelAndView("embed");
    }

    @GetMapping(value = "/demo/handoff/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String status() {
        return "{\"callbacksRun\":" + HandOffDemoState.CALLBACKS_RUN.get() + "}";
    }
}
