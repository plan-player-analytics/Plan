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
package com.djrapitops.plan.extension.extractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation detail, utility class for handling method annotations.
 *
 * @author AuroraLS3
 */
@Deprecated
public class MethodAnnotations {

    private final Map<Class<?>, Map<Method, Annotation>> byAnnotationType;

    public MethodAnnotations() {
        byAnnotationType = new HashMap<>();
    }

    public static boolean hasAnyOf(Method method, Class<?>... annotationClasses) {
        for (Annotation annotation : method.getAnnotations()) {
            for (Class<?> annotationClass : annotationClasses) {
                if (annotationClass.isAssignableFrom(annotation.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T extends Annotation> Optional<T> get(Method from, Class<T> ofClass) {
        return Optional.ofNullable(from.getAnnotation(ofClass));
    }

    public <T extends Annotation> void put(Method method, Class<T> annotationClass, T annotation) {
        Map<Method, Annotation> methods = byAnnotationType.getOrDefault(annotationClass, new HashMap<>());
        methods.put(method, annotation);
        byAnnotationType.put(annotationClass, methods);
    }

    public <T extends Annotation> Map<Method, T> getMethodAnnotations(Class<T> ofType) {
        return (Map<Method, T>) byAnnotationType.getOrDefault(ofType, new HashMap<>());
    }

    public <T extends Annotation> Collection<T> getAnnotations(Class<T> ofType) {
        return getMethodAnnotations(ofType).values();
    }

    public boolean isEmpty() {
        return byAnnotationType.isEmpty();
    }

    @Override
    public String toString() {
        return "MethodAnnotations{" + byAnnotationType + '}';
    }

    void makeMethodsAccessible() {
        byAnnotationType.values().stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .distinct()
                .filter(method -> !method.isAccessible())
                .forEach(method -> method.setAccessible(true));
    }
}