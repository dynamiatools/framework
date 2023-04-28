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
package tools.dynamia.actions;

import org.junit.Assert;
import org.junit.Test;

public class FastActionTest {

    int result = 0;

    @Test
    public void executeFastAction() {
        final int expected = 1;

        FastAction changeResultAction = new FastAction("ChangeResult", evt -> result = 1);
        changeResultAction.execute();

        Assert.assertEquals(expected, result);

    }
}
