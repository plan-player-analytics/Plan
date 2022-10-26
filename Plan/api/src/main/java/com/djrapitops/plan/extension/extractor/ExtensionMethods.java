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

import com.djrapitops.plan.extension.extractor.dataprovider.AnnotationDataProvider;

import java.util.*;

/**
 * Implementation detail, abstracts away method type reflection to a more usable API.
 */
public class ExtensionMethods {

    private final List<AnnotationDataProvider<?, ?, ?>> providers;

    public ExtensionMethods() {
        providers = new ArrayList<>();
    }

    public List<AnnotationDataProvider<?, ?, ?>> getProviders() {
        return providers;
    }

    public void addProvider(AnnotationDataProvider<?, ?, ?> extractor) {
        providers.add(extractor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionMethods that = (ExtensionMethods) o;
        return Objects.equals(providers, that.providers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providers);
    }

    @Override
    public String toString() {
        return "ExtensionMethods{" +
                "providers=" + providers +
                '}';
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }
}
