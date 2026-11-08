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
package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.commons.LocalDateRange;
import tools.dynamia.ui.UIMessages;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayDateRangeInputTest {

    private final List<LocalDateRange> picked = new ArrayList<>();

    private final Function<ActionExecutionRequest, Object> body = request -> {
        UIMessages.<LocalDateRange>showInput("Period", LocalDateRange.class, picked::add);
        return null;
    };

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    @Test
    void aDateRangeIsAskedAsAnInputOfThatKind() {
        var pending = ReplayExecutor.execute("period", new ActionExecutionRequest(), body);

        assertEquals(ActionFlowStepType.INPUT, pending.getFlow().getType());
        assertEquals("daterange", pending.getFlow().getInputType());
    }

    @Test
    void theAnswerArrivesAsARange() {
        var pending = ReplayExecutor.execute("period", new ActionExecutionRequest(), body);

        ReplayExecutor.execute("period", answer(pending, "2026-01-01/2026-01-31"), body);

        assertEquals(1, picked.size());
        assertEquals(LocalDate.of(2026, 1, 31), picked.get(0).getEndDate());
    }

    @Test
    void aMalformedAnswerIsAskedAgainWithTheReason() {
        var pending = ReplayExecutor.execute("period", new ActionExecutionRequest(), body);

        var again = ReplayExecutor.execute("period", answer(pending, "2026-02-01/2026-01-01"), body);

        assertEquals(ActionFlowStepType.INPUT, again.getFlow().getType());
        assertEquals("daterange", again.getFlow().getInputType());
        assertTrue(again.getFlow().getMessage().contains("before its start"));
        assertEquals(0, picked.size());
    }
}
