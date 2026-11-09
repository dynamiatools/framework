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
package tools.dynamia.zk.actions;

import org.zkoss.zul.Comboitem;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.Control;
import tools.dynamia.actions.HeadlessActionRenderer;
import tools.dynamia.commons.DateTimeUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The ZK side of a {@link Control}: where a toolbar would use {@code action.getRenderer()}, it goes through {@link #adapt} so
 * an action declared with a {@link HeadlessActionRenderer} is drawn with the widget of its kind. The renderers are the ones ZK
 * already had, so a portable action looks and behaves in ZK as the hand written one did; the widget hands its value to
 * the action as before.
 */
public final class ControlRenderers {

    private ControlRenderers() {
    }

    /**
     * @param renderer the renderer of an action
     * @return the ZK renderer to use: the widget of the control for a {@link HeadlessActionRenderer}, {@code renderer} itself otherwise
     */
    @SuppressWarnings("rawtypes")
    public static ActionRenderer adapt(ActionRenderer renderer) {
        return renderer instanceof HeadlessActionRenderer headless ? forControl(headless.getControl()) : renderer;
    }

    /**
     * @param control what to draw
     * @return the ZK renderer of that kind
     */
    @SuppressWarnings("rawtypes")
    public static ActionRenderer forControl(Control control) {
        return switch (control.kind()) {
            case DATE -> new LocalDateboxActionRenderer();
            case DATE_RANGE -> {
                var range = presetDates(control.preset());
                yield range == null ? new LocalDateboxRangeActionRenderer()
                        : new LocalDateboxRangeActionRenderer(range[0], range[1]);
            }
            case DATETIME_RANGE -> {
                var range = presetDates(control.preset());
                yield range == null ? new LocalDateTimeboxRangeActionRenderer()
                        : new LocalDateTimeboxRangeActionRenderer(DateTimeUtils.getStartOfDay(range[0]), DateTimeUtils.getEndOfDay(range[1]));
            }
            case TEXT -> control.label() == null ? new FindActionRenderer() : new FindActionRenderer(control.label());
            case CHOICE -> choice(control);
        };
    }

    private static ComboboxActionRenderer choice(Control control) {
        var model = new ArrayList<Object>();
        if (control.allLabel() != null) {
            model.add(null);
        }
        model.addAll(control.options());
        var renderer = new ComboboxActionRenderer(model);
        renderer.setComboitemRenderer((Comboitem item, Object data, int index) -> {
            item.setValue(data);
            item.setLabel(data instanceof Control.Option option ? option.label() : control.allLabel());
        });
        return renderer;
    }

    /**
     * Resolves a symbolic preset of the control to dates, in the server's time zone (ZK has no other).
     *
     * @param preset {@code TODAY}, {@code THIS_MONTH}, {@code LAST_MONTH} or null
     * @return start and end, or null when there is no preset
     */
    static LocalDate[] presetDates(String preset) {
        if (preset == null) {
            return null;
        }
        var today = LocalDate.now();
        return switch (preset) {
            case "TODAY" -> new LocalDate[]{today, today};
            case "THIS_MONTH" -> new LocalDate[]{today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())};
            case "LAST_MONTH" -> {
                var first = today.minusMonths(1).withDayOfMonth(1);
                yield new LocalDate[]{first, first.withDayOfMonth(first.lengthOfMonth())};
            }
            default -> throw new IllegalArgumentException("Unknown preset " + preset);
        };
    }
}
