/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.viewers;

import java.util.Comparator;


/**
 * The Class IndexableComparator.
 *
 * @author Mario A. Serrano Leones
 */
public class IndexableComparator implements Comparator<Indexable> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Indexable o1, Indexable o2) {
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        } else if (o1.getIndex() > o2.getIndex()) {
            return 1;
        } else {
            return 0;
        }
    }
}
