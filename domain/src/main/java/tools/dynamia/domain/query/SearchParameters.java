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
package tools.dynamia.domain.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class SearchParameters.
 *
 * @author Mario A. Serrano Leones
 */
public class SearchParameters implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 3485379808386399353L;

    /**
     * The fields.
     */
    private List<String> fields;

    /**
     * The boots factors.
     */
    private Map<String, Float> bootsFactors;

    /**
     * The paginator.
     */
    private DataPaginator paginator;

    /**
     * The text.
     */
    private String text;

    /**
     * Instantiates a new search parameters.
     *
     * @param fields the fields
     */
    private SearchParameters(String... fields) {
        this.fields = Arrays.asList(fields);
    }

    /**
     * Adds the factor.
     *
     * @param field the field
     * @param bootFactor the boot factor
     * @return the search parameters
     */
    public SearchParameters addFactor(String field, float bootFactor) {
        if (bootsFactors == null) {
            bootsFactors = new HashMap<>();
        }
        bootsFactors.put(field, bootFactor);
        return this;
    }

    /**
     * Adds the field.
     *
     * @param field the field
     * @return the search parameters
     */
    public SearchParameters addField(String field) {
        this.fields.add(field);
        return this;
    }

    /**
     * Paginate.
     *
     * @param paginator the paginator
     * @return the search parameters
     */
    public SearchParameters paginate(DataPaginator paginator) {
        this.paginator = paginator;
        return this;
    }

    /**
     * Paginate.
     *
     * @param pageSize the page size
     * @return the search parameters
     */
    public SearchParameters paginate(int pageSize) {
        return paginate(new DataPaginator(0, pageSize, 0));
    }

    /**
     * Creates the.
     *
     * @param fields the fields
     * @return the search parameters
     */
    public static SearchParameters create(String... fields) {
        return new SearchParameters(fields);
    }

    /**
     * Gets the paginator.
     *
     * @return the paginator
     */
    public DataPaginator getPaginator() {
        return paginator;
    }

    /**
     * Gets the boots factors.
     *
     * @return the boots factors
     */
    public Map<String, Float> getBootsFactors() {
        return bootsFactors;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public String[] getFields() {
        return fields.toArray(new String[0]);
    }

    /**
     * Sets the fields.
     *
     * @param fields the new fields
     */
    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        this.text = text;
    }
}
