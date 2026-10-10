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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Lists the files of a repository for the rules. Build output and tool folders are never entered, and a file that disappears
 * while the repository is read (a temporary file of a build running in parallel) is skipped instead of failing the test.
 */
final class RepoFiles {

    private static final Set<String> SKIPPED_DIRECTORIES = Set.of("target", "node_modules", ".git", ".idea", "dist", "build");

    private RepoFiles() {
    }

    /**
     * @param root  the repository root
     * @param match which files to keep
     * @return the files below {@code root} that match, in no particular order
     */
    static List<Path> list(Path root, Predicate<Path> match) {
        var found = new ArrayList<Path>();
        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                return !dir.equals(root) && SKIPPED_DIRECTORIES.contains(name) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (match.test(file)) {
                    found.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE; // it vanished or cannot be read: it is not part of the sources
            }
        };
        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return found;
    }
}
