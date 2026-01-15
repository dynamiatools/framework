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

package mybookstore;

import mybookstore.domain.Category;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.dynamia.app.Ehcache3CacheManager;
import tools.dynamia.commons.UserInfo;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.navigation.DefaultPageProvider;
import tools.dynamia.ui.icons.IconsProvider;
import tools.dynamia.web.pwa.PWAIcon;
import tools.dynamia.web.pwa.PWAManifest;
import tools.dynamia.web.pwa.PWAShortcut;
import tools.dynamia.zk.ui.ZIconsProvider;

import java.util.List;

@SpringBootApplication
@EntityScan({"mybookstore", "tools.dynamia"})
@EnableCaching
@EnableScheduling
public class MyBookStoreApplication { //<1>


    public static void main(String[] args) {
        SpringApplication.run(MyBookStoreApplication.class, args); //<2>
    }


    @Bean
    public CacheManager cacheManager() {
        return new Ehcache3CacheManager();
    }

    @Bean
    public EntityReferenceRepository<Long> categoriesReferenceRepository() {
        return new DefaultEntityReferenceRepository<>(Category.class, "name");
    }

    @Bean
    public DefaultPageProvider defaultPageProvider() {
        return () -> "library/books";
    }

    @Bean
    public IconsProvider iconsProvider() {
        return new ZIconsProvider();
    }


    @Bean
    public PWAManifest manifest() {
        return PWAManifest.builder()
                .name("My Book Store")
                .shortName("Books")
                .startUrl("/")
                .backgroundColor("#ffffff")
                .themeColor("#3f51b5")
                .display("standalone")
                .categories(List.of("books", "education", "library"))
                .addIcon(PWAIcon.builder()
                        .src("android-chrome-192x192.png")
                        .sizes("192x192")
                        .type("image/png")
                        .build())
                .addIcon(PWAIcon.builder()
                        .src("android-chrome-512x512.png")
                        .sizes("512x512")
                        .type("image/png")
                        .build())
                .addShortcut(PWAShortcut.builder()
                        .name("Home")
                        .shortName("Home")
                        .description("Go to home page")
                        .url("/")
                        .addIcon(PWAIcon.builder()
                                .src("android-chrome-192x192.png")
                                .sizes("192x192")
                                .type("image/png").build())
                        .build())
                .build();
    }


    /**
     * Initializes sample data on application startup for user info. @Bean should be name "userInfo" to be detected by Dynamical Template
     *
     * @return a user info instance with sample data
     */
    @Bean("userInfo")
    public UserInfo userInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("admin");
        userInfo.setFullName("Administrator");
        userInfo.setImage("/static/user-photo.jpg");
        return userInfo;
    }
}
