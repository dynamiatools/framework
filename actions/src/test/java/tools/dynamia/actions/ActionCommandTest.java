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
package tools.dynamia.actions;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ActionCommandTest {

    @Test
    public void testSimpleActionCommand() {
        CalculatorForm form = new CalculatorForm();
        form.setA(1);
        form.setB(2);

        final int expectedResult = 3;

        List<Action> actions = ActionLoader.loadActionCommands(form);
        for (Action action : actions) {
            Assert.assertTrue(action instanceof FastAction);

            if (action.getName().equals("sum")) {
                FastAction fastAction = (FastAction) action;
                fastAction.execute();
            }
        }

        Assert.assertEquals(expectedResult, form.getR());

    }

    @Test
    public void testComplexActionCommand() {
        CalculatorForm form = new CalculatorForm();
        form.setA(10);
        form.setB(7);

        final int expectedResult = 3;

        List<Action> actions = ActionLoader.loadActionCommands(form);
        for (Action action : actions) {
            Assert.assertTrue(action instanceof FastAction);

            if (action.getName().equals("Subtract")) {
                FastAction fastAction = (FastAction) action;
                Assert.assertEquals("minus", fastAction.getImage());
                Assert.assertNull(fastAction.getRenderer());
                fastAction.execute();
            }
        }

        Assert.assertEquals(expectedResult, form.getR());

    }

    static class CalculatorForm {

        private int a;
        private int b;
        private int r;

        @ActionCommand
        public void sum() {
            r = a + b;
        }

        @ActionCommand(name = "Subtract", image = "minus")
        public void subtract() {
            r = a - b;
        }

        public void setA(int a) {
            this.a = a;
        }

        public void setB(int b) {
            this.b = b;
        }

        public int getR() {
            return r;
        }
    }

}
