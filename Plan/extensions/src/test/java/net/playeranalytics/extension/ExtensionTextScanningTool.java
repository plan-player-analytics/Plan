/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package net.playeranalytics.extension;

import com.djrapitops.plan.extension.annotation.DataBuilderProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author AuroraLS3
 */
public class ExtensionTextScanningTool {

    public static void main(String[] args) throws Exception {
        try (ScanResult scanResult =
                     new ClassGraph()
                             .verbose()
                             .enableAllInfo()
                             .acceptPackages("net.playeranalytics", "com.djrapitops")
                             .scan()) {

            Map<String, String> translations = new LinkedHashMap<>();
            scanResult.getAllClassesAsMap().values().stream()
                    .filter(classInfo -> classInfo.getAnnotationInfo(PluginInfo.class) != null)
                    .map(classInfo -> {
                        // What we need from each extension:
                        // - A list of Provider annotations
                        //   - text
                        //   - description
                        // - DataExtensionBuilder
                        //   - Static analysis of text & description
                        // - TableProvider
                        //   - Static analysis of column names
                        Object pluginName = classInfo.getAnnotationInfo(PluginInfo.class).getParameterValues().getValue("name");

                        Map<String, String> foundTranslations = new LinkedHashMap<>();
                        for (MethodInfo methodInfo : classInfo.getMethodInfo()) {
                            for (AnnotationInfo annotationInfo : methodInfo.getAnnotationInfo()) {
                                if (annotationInfo.getName().equals(DataBuilderProvider.class.getName())) {
                                    // TODO
                                } else if (annotationInfo.getName().equals(TableProvider.class.getName())) {
                                    // Needs the source file rather than compiled code
                                    // https://github.com/mstrobel/procyon/wiki/Decompiler-API could be used
//                                    getTranslationsForTableProvider(methodInfo, foundTranslations, pluginName);
                                } else if (annotationInfo.getName().endsWith("Provider")) {
                                    getTranslationsForProvider(methodInfo, annotationInfo, foundTranslations, pluginName);
                                }
                            }
                        }

                        return foundTranslations;
                    }).forEach(translations::putAll);

            List<String> lines = translations.entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .sorted()
                    .toList();
            Path file = new File("extensions/src/main/resources/assets/plan/plugin_translations.yml").toPath();
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
    }

    private static void getTranslationsForTableProvider(MethodInfo methodInfo, Map<String, String> foundTranslations, Object pluginName) {
        if (!methodInfo.hasBody()) return;

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, StandardCharsets.UTF_8);
            Iterable<? extends JavaFileObject> sources = manager.getJavaFileObjects(methodInfo.getClassInfo().getClasspathElementFile());

            final EmptyTryBlockScanner scanner = new EmptyTryBlockScanner();
            final EmptyTryBlockProcessor processor = new EmptyTryBlockProcessor(scanner);

            final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                    null, null, sources);
            task.setProcessors(List.of(processor));
            task.call();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e);
        }
    }

    private static void getTranslationsForProvider(MethodInfo methodInfo, AnnotationInfo annotationInfo, Map<String, String> foundTranslations, Object pluginName) {
        for (AnnotationParameterValue parameterValue : annotationInfo.getParameterValues(true)) {
            String parameter = parameterValue.getName();
            if ("text".equals(parameter) || "description".equals(parameter)) {
                foundTranslations.put(mapToKey(methodInfo, pluginName, parameter), parameterValue.getValue().toString());
            }
        }
    }

    private static @NotNull String mapToKey(MethodInfo methodInfo, Object pluginName, String parameter) {
        return "extensions." + mapPluginNameToKey(pluginName) + '.' + methodInfo.getName() + '.' + parameter;
    }

    private static String mapPluginNameToKey(Object pluginName) {
        return pluginName.toString().replace(':', '_').replace(' ', '_');
    }

    static class EmptyTryBlockScanner extends TreePathScanner<Object, Trees> {
        private int numberOfEmptyTryBlocks;

        @Override
        public Object visitTry(final TryTree tree, Trees trees) {
            List<? extends StatementTree> statements = tree.getBlock().getStatements();


            return super.visitTry(tree, trees);
        }

        public int getNumberOfEmptyTryBlocks() {
            return numberOfEmptyTryBlocks;
        }
    }

    static class EmptyTryBlockProcessor extends AbstractProcessor {
        private final EmptyTryBlockScanner scanner;
        private Trees trees;

        public EmptyTryBlockProcessor(final EmptyTryBlockScanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public synchronized void init(final ProcessingEnvironment processingEnvironment) {
            super.init(processingEnvironment);
            trees = Trees.instance(processingEnvironment);
        }

        public boolean process(final Set<? extends TypeElement> types,
                               final RoundEnvironment environment) {

            if (!environment.processingOver()) {

                for (final Element element : environment.getElementsAnnotatedWithAny(Set.of(TableProvider.class))) {
                    scanner.scan(trees.getPath(element), trees);
                }
            }

            return true;
        }
    }

}
