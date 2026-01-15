package tools.dynamia.modules.security.ui.listeners;

import tools.dynamia.commons.UserInfo;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.listeners.LoginListener;

@Listener
public class SetupApplicationUserInfoListener implements LoginListener {


    @Override
    public void onLoginSuccess(User user) {

        UserInfo userInfo = Containers.get().findObject(UserInfo.class);
        if (userInfo != null) {
            userInfo.setId(user.getId());
            userInfo.setUid(user.getUuid());
            userInfo.setDate(user.getCreationDate());
            userInfo.setEmail(user.getEmail());
            userInfo.setFullName(user.getFullname());
            userInfo.setUsername(user.getUsername());
            userInfo.setProfilePath("system/security/myProfile");


            if (user.getPhoto() != null) {
                userInfo.setImage(user.getPhoto().toURL());
            }
        }
    }
}
