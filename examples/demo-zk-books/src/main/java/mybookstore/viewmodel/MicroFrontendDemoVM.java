package mybookstore.viewmodel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Backs the "MVVM mode" panel of the MicroFrontend demo (microfrontend-demo.zul). Every bound
 * value below is a plain bean property, so it works with {@code @bind(vm.xxx)} with zero extra
 * code in tools.dynamia.zk.ui.MicroFrontend. {@link #getUser()} in particular proves that binding
 * a Java object (not just Strings/numbers) reaches the browser as real JSON: MicroFrontend's
 * buildConfig() serializes the whole props map through StringPojoParser.convertMapToJson, which is
 * backed by Jackson and recursively serializes nested POJOs, not toString().
 */
public class MicroFrontendDemoVM {

    private static final String[] ROLES = {"admin", "editor", "viewer"};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Random random = new Random();

    private String theme = "dark";
    private boolean shadow;
    private DemoUser user = new DemoUser(1L, "Ada Lovelace", "admin");
    private String lastEvent = "(none yet)";

    public String getTheme() {
        return theme;
    }

    public boolean isShadow() {
        return shadow;
    }

    public DemoUser getUser() {
        return user;
    }

    public String getLastEvent() {
        return lastEvent;
    }

    @Command
    @NotifyChange("theme")
    public void toggleTheme() {
        theme = "dark".equals(theme) ? "light" : "dark";
    }

    @Command
    @NotifyChange("shadow")
    public void toggleShadow() {
        shadow = !shadow;
    }

    @Command
    @NotifyChange("user")
    public void randomizeUser() {
        user = new DemoUser(random.nextLong(1000), "User " + random.nextInt(1000), ROLES[random.nextInt(ROLES.length)]);
    }

    @Command
    @NotifyChange("lastEvent")
    public void handleMicrofrontendEvent(@BindingParam("data") Object data) {
        lastEvent = LocalTime.now().format(TIME_FORMAT) + " -> " + data;
    }

    /** Bound as a whole to the "user" prop, proving nested POJO-to-JSON serialization. */
    public static class DemoUser {

        private final Long id;
        private final String name;
        private final String role;

        public DemoUser(Long id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }
    }
}
