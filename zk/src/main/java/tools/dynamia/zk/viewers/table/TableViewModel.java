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

package tools.dynamia.zk.viewers.table;

import org.zkoss.lang.Objects;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.event.ListDataEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TableViewModel<E> extends ListModelList<E> {

    /**
     *
     */
    private static final long serialVersionUID = 128026768974491029L;
    private boolean _sortDir;
    private Comparator<E> _sorting;

    public TableViewModel() {
        super();

    }

    public TableViewModel(Collection<? extends E> c) {
        super(c);

    }

    public TableViewModel(E[] array) {
        super(array);

    }

    public TableViewModel(int initialCapacity) {
        super(initialCapacity);

    }

    public TableViewModel(List<E> list, boolean live) {
        super(list, live);

    }

    public TableViewModel(List<E> list, boolean live, boolean multiple) {
        super(list, live);
        setMultiple(multiple);

    }

    @Override
    public void sort(Comparator<E> cmpr, boolean ascending) {
        _sorting = cmpr;
        _sortDir = ascending;
        fireEvent(ListDataEvent.STRUCTURE_CHANGED, -1, -1);
    }

    @Override
    public String getSortDirection(Comparator<E> cmpr) {
        if (Objects.equals(_sorting, cmpr)) {
            return _sortDir
                    ? "ascending" : "descending";
        }
        return "natural";
    }

}
