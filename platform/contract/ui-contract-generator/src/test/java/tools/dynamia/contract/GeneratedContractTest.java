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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The TypeScript types of the SDK are generated from the Java classes, so they cannot drift. This test fails when the
 * committed file is not what the generator produces now (a step type was added, a field renamed, a port declared...).
 * Regenerate with {@code mvn -pl platform/contract/ui-contract-generator test -Dcontract.update=true}.
 */
class GeneratedContractTest {

    @Test
    void theGeneratedTypesOfTheSdkAreUpToDate() throws IOException {
        String generated = ContractGenerator.generate();
        var file = ContractFiles.generatedTypes();

        if (Boolean.getBoolean("contract.update")) {
            Files.createDirectories(file.getParent());
            Files.writeString(file, generated);
            return;
        }
        if (!Files.exists(file)) {
            fail(file + " does not exist. Generate it with -Dcontract.update=true");
        }
        assertEquals(Files.readString(file), generated, file + " is stale: the Java protocol changed. "
                + "Regenerate it with: mvn -pl platform/contract/ui-contract-generator test -Dcontract.update=true");
    }

    @Test
    void everyPortSaysWhichStepsItProduces() {
        String generated = ContractGenerator.generate();

        for (String port : new String[]{"messages", "views", "choices", "files", "progress", "navigation"}) {
            if (!generated.contains("    " + port + ": { spi: '")) {
                fail("Port '" + port + "' is not declared with @UIPort");
            }
        }
    }
}
