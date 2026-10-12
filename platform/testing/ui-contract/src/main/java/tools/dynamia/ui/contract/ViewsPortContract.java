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
package tools.dynamia.ui.contract;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.UIViews;
import tools.dynamia.ui.ViewOptions;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** {@code UIViews}: forms and read only views. */
public abstract class ViewsPortContract extends PortContract {

    /** The bean the forms edit. */
    public static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void submitReceivesTheBeanWithTheValuesTheUserSent() {
        var o = driver().run(fx -> UIViews.showForm(FormOptions.of("New person", Person.class, new Person()), (person, dialog) -> {
            fx.add("submitted " + person.getName());
            dialog.close();
        }), List.of(Reply.form(Map.of("name", "Ana"))));

        assertEquals(List.of("submitted Ana"), o.effects());
        assertEquals(List.of(Outcome.Asked.of("DIALOG", "New person")), o.asked());
    }

    @Test
    void cancellingNeverSubmits() {
        var o = driver().run(fx -> UIViews.showForm(FormOptions.of("New person", Person.class, new Person()), (person, dialog) -> {
            fx.add("submitted");
            dialog.close();
        }), List.of(Reply.cancel()));

        assertEquals(List.of(), o.effects());
        assertNull(o.failure());
    }

    @Test
    void aValidationErrorKeepsTheFormOpenWithTheValuesAndTheMessage() {
        var o = driver().run(fx -> UIViews.showForm(FormOptions.of("New person", Person.class, new Person()), (person, dialog) -> {
            if (person.getName() == null || person.getName().isBlank()) {
                throw new ValidationError("Name is required", null, "name", Person.class);
            }
            fx.add("saved " + person.getName());
            dialog.close();
        }), List.of(Reply.form(Map.of("name", "")), Reply.form(Map.of("name", "Ana"))));

        assertEquals(List.of("saved Ana"), o.effects());
        assertEquals(List.of(new Outcome.Asked("DIALOG", "New person", null),
                new Outcome.Asked("DIALOG", "New person", "Name is required")), o.asked());
    }

    @Test
    void aFormTheHandlerDoesNotCloseStaysOpen() {
        var o = driver().run(fx -> UIViews.showForm(FormOptions.of("New person", Person.class, new Person()), (person, dialog) ->
                fx.add("submitted " + person.getName())), List.of(Reply.form(Map.of("name", "Ana")), Reply.cancel()));

        assertEquals(List.of(Outcome.Asked.of("DIALOG", "New person"), Outcome.Asked.of("DIALOG", "New person")), o.asked());
    }

    @Test
    void aViewIsShownAndOnCloseRunsWhenTheUserClosesIt() {
        var person = new Person();
        person.setName("Ana");
        var o = driver().run(fx -> UIViews.showView(ViewOptions.of("Person", Person.class, person), () -> fx.add("closed")),
                List.of());

        assertEquals(List.of(Outcome.Asked.of("VIEW", "Person")), o.asked());
        assertEquals(List.of("closed"), o.effects());
    }

    @Test
    void aViewWithoutOnCloseIsJustShown() {
        var o = driver().run(fx -> UIViews.showView(ViewOptions.of("Person", Person.class, new Person())), List.of());

        assertEquals(List.of(Outcome.Asked.of("VIEW", "Person")), o.asked());
        assertNull(o.failure());
    }
}
