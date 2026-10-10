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
package tools.dynamia.arch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A list of violations that exist today and may only shrink: a new one fails the test, and so does a listed one that was
 * fixed but not removed, so progress is recorded and never lost. Regenerate with {@code -Dui.baseline.update=true}.
 */
public final class ArchBaseline {

    private ArchBaseline() {
    }

    /**
     * @param file       the baseline file
     * @param actual     the violations found now
     * @param addedHint  what to tell the developer when something new appears
     */
    public static void check(Path file, Set<String> actual, String addedHint) {
        try {
            if (Boolean.getBoolean("ui.baseline.update")) {
                Files.createDirectories(file.getParent());
                Files.write(file, new TreeSet<>(actual));
                return;
            }
            var expected = new TreeSet<String>();
            if (Files.exists(file)) {
                Files.readAllLines(file).stream().filter(l -> !l.isBlank() && !l.startsWith("#")).forEach(expected::add);
            }
            var added = new TreeSet<>(actual);
            added.removeAll(expected);
            var fixed = new TreeSet<>(expected);
            fixed.removeAll(actual);
            assertTrue(added.isEmpty(), addedHint + ":\n" + String.join("\n", added));
            assertTrue(fixed.isEmpty(), "These are fixed, remove them from " + file + " (or regenerate it with "
                    + "-Dui.baseline.update=true):\n" + String.join("\n", fixed));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
