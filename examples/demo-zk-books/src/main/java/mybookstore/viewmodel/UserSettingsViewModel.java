package mybookstore.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Locales;
import org.zkoss.util.TimeZones;
import tools.dynamia.app.SessionApplicationTemplate;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.web.util.SessionLocaleProvider;
import tools.dynamia.web.util.SessionTimeZoneProvider;
import tools.dynamia.zk.ZKAppConfiguration;

import java.time.ZoneId;
import java.util.TimeZone;

public class UserSettingsViewModel {


    private String skin;
    private String locale = Messages.getDefaultLocale().toLanguageTag();
    private String timeZone = Messages.getDefaultTimeZone().getId();

    @Init
    public void init() {
        skin = SessionApplicationTemplate.get().getSkin().getId();
    }


    @Command
    public void applySkin() {
        if (skin != null) {
            ZKAppConfiguration.updateSkin(skin);
            UIMessages.showMessage("Reloading..");

        }
    }

    @Command
    public void applyLocale() {
        if (locale != null) {
            SessionLocaleProvider.setSessionLocale(Locales.getLocale(locale));
            UIMessages.showMessage("Locale applied");
        }
    }

    @Command
    public void applyTimeZone() {
        if (timeZone != null) {
            SessionTimeZoneProvider.setSessionTimeZone(ZoneId.of(timeZone));
            UIMessages.showMessage("Time zone applied");
        }
    }


    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
