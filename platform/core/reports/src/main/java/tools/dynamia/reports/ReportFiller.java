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
package tools.dynamia.reports;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
public class ReportFiller {

    private static final LoggingService logger = new SLF4JLoggingService(ReportFiller.class);

    private static final List<Consumer<ReportDescriptor>> preFillConsumers = new ArrayList<>();

    /**
     *
     */
    public static Report fill(ReportDescriptor reportDescriptor) {
        ReportCompiler compiler = getReportCompiler(reportDescriptor);
        preFillConsumers.forEach(c -> c.accept(reportDescriptor));
        return compiler.fill(reportDescriptor);
    }

    public static Report fill(ReportDescriptor reportDescriptor, boolean inMemory) {
        ReportCompiler compiler = getReportCompiler(reportDescriptor);
        preFillConsumers.forEach(c -> c.accept(reportDescriptor));
        return compiler.fill(reportDescriptor, inMemory);
    }


    private static ReportCompiler getReportCompiler(ReportDescriptor reportDescriptor) {
        ReportCompiler compiler = null;


        if (reportDescriptor.getPreferedCompiler() != null && !reportDescriptor.getPreferedCompiler().isBlank()) {
            compiler = Containers.get().findObjects(ReportCompiler.class)
                    .stream().filter(rc -> rc.getId().equals(reportDescriptor.getPreferedCompiler()))
                    .findFirst().orElse(null);
        }

        if (compiler == null) {
            compiler = Containers.get().findObject(ReportCompiler.class);
        }

        if (compiler == null) {
            throw new ReportCompileException("No report compiler found. Register a @Bean that implement "
                    + ReportCompiler.class.getName() + " interface like tools.dynamia.app.JasperReportCompiler ");
        }
        return compiler;
    }

    public static void beforeFill(Consumer<ReportDescriptor> consumer) {
        preFillConsumers.add(consumer);
    }


    private ReportFiller() {
    }
}
