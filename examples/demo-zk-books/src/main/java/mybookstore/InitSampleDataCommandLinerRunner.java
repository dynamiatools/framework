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

import mybookstore.domain.Book;
import mybookstore.domain.Category;
import mybookstore.domain.enums.StockStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class InitSampleDataCommandLinerRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        var novels = new Category("Novels")
                .add(new Category("Fantasy")) //subcategories
                .add(new Category("Scifi"))
                .add(new Category("Drama"));

        novels.save();

        var programming = new Category("Programming")
                .add(new Category("Java"))  //subcategories
                .add(new Category("Groovy"))
                .add(new Category("Kotlin"))
                .add(new Category("Cloud"))
                .add(new Category("Frontend"))
                .add(new Category("Architecture"));

        programming.save();

        List.of("My First Pony", "100 years of drama", "My Best Friend", "The Little Prince", "Lord of the Rings", "Game of Thrones",
                        "Skyward", "Son of Mist", "Dune", "Divergent", "Henry Pota")

                .forEach(title -> {
                    int random = new Random().nextInt(novels.getSubcategories().size());
                    var category = novels.getSubcategories().get(random);
                    newBook(title, category, random);
                });

        List.of("My First Programming", "Clean Code", "Design Patterns", "Scale to the cloud", "Spring Boot", "Dynamia in Action", "Flutter", "Dart",
                        "Javascript Maxx", "Tha Naxt Web", "Kotlin for Android")
                .forEach(title -> {
                    int random = new Random().nextInt(novels.getSubcategories().size());
                    var category = programming.getSubcategories().get(random);
                    newBook(title, category, random);
                });

        System.out.println("Demo ready to run");
    }

    private static void newBook(String title, Category category, int random) {
        var book = new Book();
        book.setTitle(title);
        book.setCategory(category);
        book.setYear(new Random().nextInt(2000, DateTimeUtils.getCurrentYear()));
        book.setBuyDate(LocalDate.now());
        book.setIsbn(StringUtils.randomString().toUpperCase());
        book.setSinopsys("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        book.setPrice(BigDecimal.valueOf(25L * random));
        book.setStockStatus(StockStatus.random());
        book.setPublishDate(DateTimeUtils.addYears(LocalDate.now(), -new Random().nextInt(1, 10)));
        book.save();
    }
}
