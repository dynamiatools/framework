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


package tools.dynamia.web.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

/**
 * Util class to build and render to String html table
 *
 * @author Ing. Mario Serrano
 */
public class HtmlTableBuilder implements Serializable {

    public static final int LEFT = -1;
    public static final int CENTER = 0;
    public static final int RIGHT = 1;
    private final StringBuilder sb = new StringBuilder();
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    private final NumberFormat decimalFormat = DecimalFormat.getInstance();
    private boolean rowCreated;
    private String title;
    private int titleLevel = 1;
    private int currencyAlign = 1;
    private int numberAlign = 1;
    private int dateAlign;
    private String description;
    private String styleClass = "";
    private String rowClass = "";
    private String cellClass = "";
    private String headerClass = "";
    private String headerBackground = "#ccc";

    public HtmlTableBuilder() {
        this("100%", "12px");
    }

    public HtmlTableBuilder(String width, String fontSize) {
        sb.append(String.format("<table width='%s' style='font-size:%s'>", width, fontSize));
    }

    public void setCurrencyAlign(int i) {
        this.currencyAlign = i;
    }

    public void setDateAlign(int i) {
        this.dateAlign = i;
    }

    public void setTitle(String title, int level) {
        if (titleLevel <= 0) {
            titleLevel = 1;
        }
        this.title = title;
        this.titleLevel = level;
    }

    public void addRow() {
        if (rowCreated) {
            sb.append("</tr>");
        }
        rowCreated = true;
        sb.append(String.format("<tr class='%s'>", rowClass));

    }

    public void addColumnHeader(String... name) {
        for (String text : name) {
            sb.append(String.format("<th style='background:%s' class='%s'>", headerBackground, headerClass));
            sb.append(text);
            sb.append("</th>");
        }

    }

    public void addData(Object... data) {
        for (Object obj : data) {
            switch (obj) {
                case BigDecimal bigDecimal -> addCurrency(bigDecimal);
                case Integer i -> addInteger(i);
                case Number number -> addNumber(number);
                case Date date -> addDate(date);
                case null, default -> addText(obj);
            }
        }
    }

    public void addText(Object text) {
        sb.append(String.format("<td class='%s'>", cellClass));
        sb.append(text);
        sb.append("</td>");
    }

    public void addCurrency(BigDecimal number) {
        String align = computeAlign("right", currencyAlign);
        sb.append(String.format("<td style='text-align: %s' class='%s' >", align, cellClass));
        sb.append("$").append(numberFormat.format(number));
        sb.append("</td>");
    }

    public void addInteger(Integer integer) {
        sb.append(String.format("<td style='text-align:center' class='%s'>", cellClass));
        sb.append(integer);
        sb.append("</td>");
    }

    public void addNumber(Number number) {
        String align = computeAlign("right", numberAlign);
        sb.append(String.format("<td style='text-align: %s' class='%s' >", align, cellClass));
        if (number instanceof Double) {
            sb.append(decimalFormat.format(number));
        } else {
            sb.append(numberFormat.format(number));
        }
        sb.append("</td>");
    }


    public void addDate(Date fecha) {
        String align = computeAlign("center", dateAlign);
        sb.append(String.format("<td style='text-align:%s'>", align, cellClass));
        sb.append(dateFormat.format(fecha));
        sb.append("</td>");
    }

    private String computeAlign(String defaultAlign, int align) {
        String result = defaultAlign;
        if (align == RIGHT) {
            result = "right";
        } else if (align == LEFT) {
            result = "left";
        } else if (align == CENTER) {
            result = "center";
        }
        return result;
    }

    public String render() {
        if (rowCreated) {
            sb.append("</tr>");
        }
        sb.append("</table>");

        String result = sb.toString();
        if (title != null) {
            title = "<h" + titleLevel + ">" + title + "</h" + titleLevel + ">";
            if (description != null) {
                title += description;
            }
            result = title + result;
        }
        return result;
    }


    public void setDescription(String description) {
        if (description != null) {
            this.description = "<p>" + description + "</p>";
        }
    }

    @Override
    public String toString() {
        return render();
    }

    /**
     * Shortcut chained method to invoke addRow() and then addData(..) methods
     *
     */
    public HtmlTableBuilder addRowAndData(Object... data) {
        addRow();
        addData(data);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getRowClass() {
        return rowClass;
    }

    public void setRowClass(String rowClass) {
        this.rowClass = rowClass;
    }

    public String getCellClass() {
        return cellClass;
    }

    public void setCellClass(String cellClass) {
        this.cellClass = cellClass;
    }

    public String getHeaderClass() {
        return headerClass;
    }

    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
    }

    public String getHeaderBackground() {
        return headerBackground;
    }

    public void setHeaderBackground(String headerBackground) {
        this.headerBackground = headerBackground;
    }

    public int getCurrencyAlign() {
        return currencyAlign;
    }

    public int getDateAlign() {
        return dateAlign;
    }

    public int getNumberAlign() {
        return numberAlign;
    }

    public void setNumberAlign(int numberAlign) {
        this.numberAlign = numberAlign;
    }
}
