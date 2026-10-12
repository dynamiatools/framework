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
package tools.dynamia.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ActionFlowStepType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JSON examples of {@code platform/contract/fixtures} are the contract between the Java server and every client. Java
 * parses each one into its class and writes it back: the result must be the same JSON. The TypeScript tests of the SDK do
 * the same with the same files.
 */
class FixturesTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static List<Path> files(String folder) throws IOException {
        try (Stream<Path> list = Files.list(ContractFiles.fixtures().resolve(folder))) {
            return list.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
    }

    private <T> Stream<DynamicTest> roundTrip(String folder, Class<T> type) throws IOException {
        return files(folder).stream().map(file -> DynamicTest.dynamicTest(folder + "/" + file.getFileName(), () -> {
            JsonNode original = mapper.readTree(Files.readString(file));
            T parsed = mapper.treeToValue(original, type);
            assertEquals(original, mapper.valueToTree(parsed), file + " does not survive a round trip through " + type.getSimpleName());
        }));
    }

    @TestFactory
    Stream<DynamicTest> everyStepSurvivesARoundTrip() throws IOException {
        return roundTrip("steps", ActionFlowStep.class);
    }

    @TestFactory
    Stream<DynamicTest> everyAnswerSurvivesARoundTrip() throws IOException {
        return roundTrip("requests", ActionExecutionRequest.class);
    }

    @TestFactory
    Stream<DynamicTest> everyResponseSurvivesARoundTrip() throws IOException {
        return roundTrip("responses", ActionExecutionResponse.class);
    }

    @TestFactory
    Stream<DynamicTest> everyStepFixtureDeclaresItsOwnType() throws IOException {
        return files("steps").stream().map(file -> DynamicTest.dynamicTest(file.getFileName().toString(), () -> {
            var step = mapper.readValue(Files.readString(file), ActionFlowStep.class);
            assertEquals(file.getFileName().toString().replace(".json", "").split("-")[0].toUpperCase(), step.getType().name());
        }));
    }

    @org.junit.jupiter.api.Test
    void thereIsAFixtureForEveryTypeOfStep() throws IOException {
        Set<String> covered = files("steps").stream().map(f -> f.getFileName().toString().replace(".json", "").split("-")[0].toUpperCase())
                .collect(Collectors.toSet());
        var missing = Arrays.stream(ActionFlowStepType.values()).map(Enum::name).filter(n -> !covered.contains(n)).toList();

        assertTrue(missing.isEmpty(), "Add a fixture in platform/contract/fixtures/steps for: " + missing);
    }
}
