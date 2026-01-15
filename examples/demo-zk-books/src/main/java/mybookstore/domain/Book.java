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

package mybookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import mybookstore.domain.enums.StockStatus;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "books")
@OrderBy("title")
public class Book extends BaseEntity {


    @NotNull
    private String title;
    @Column(length = 1000)
    private String sinopsys;
    @Column(name = "publication_year")
    private int year;
    @NotEmpty
    private String isbn;
    @ManyToOne
    private Category category;


    private LocalDate publishDate;
    private LocalDate buyDate;

    private BigDecimal price;

    @Enumerated
    private StockStatus stockStatus = StockStatus.IN_STOCK;
    private LocalDateTime statusDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookReview> reviews = new ArrayList<>();

    private boolean onSale;
    private BigDecimal salePrice;


    @OneToOne
    private EntityFile bookCover;

    private double discount;//percent

    @Transient
    private boolean selected;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinopsys() {
        return sinopsys;
    }

    public void setSinopsys(String sinopsys) {
        this.sinopsys = sinopsys;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public LocalDate getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(LocalDate buyDate) {
        this.buyDate = buyDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatus stockStatus) {
        if(this.stockStatus != stockStatus){
            this.statusDate = LocalDateTime.now();
        }
        this.stockStatus = stockStatus;
    }

    @Override
    public String toString() {
        return title;
    }

    public List<BookReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<BookReview> reviews) {
        this.reviews = reviews;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public EntityFile getBookCover() {
        return bookCover;
    }

    public void setBookCover(EntityFile bookCover) {
        this.bookCover = bookCover;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
