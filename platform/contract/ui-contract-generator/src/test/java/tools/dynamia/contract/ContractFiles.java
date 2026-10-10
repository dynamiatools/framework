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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Where the shared files of the contract are, found from the working directory of a test.
 */
final class ContractFiles {

    private ContractFiles() {
    }

    /** @return the repository root */
    static Path root() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve("platform/contract/fixtures"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new UncheckedIOException(new IOException("Repository root not found from " + Paths.get("").toAbsolutePath()));
    }

    /** @return the directory with the JSON fixtures shared by the Java and TypeScript tests */
    static Path fixtures() {
        return root().resolve("platform/contract/fixtures");
    }

    /** @return the TypeScript file generated for the SDK */
    static Path generatedTypes() {
        return root().resolve("platform/packages/sdk/src/generated/ui-contract.ts");
    }
}
