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
package com.djrapitops.plan.storage.database.queries.objects.lookup;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author AuroraLS3
 */
public class LookupTable<I> {

    private final Map<I, Integer> identifierToId;

    public LookupTable() {
        this.identifierToId = new HashMap<>();
    }

    public LookupTable(Map<I, Integer> identifierToId) {
        this.identifierToId = identifierToId;
    }

    public Integer put(I identifier, Integer id) {return identifierToId.put(identifier, id);}

    public void putAll(@NotNull Map<? extends I, ? extends Integer> map) {identifierToId.putAll(map);}

    public void putAll(LookupTable<I> lookupTable) {
        putAll(lookupTable.identifierToId);
    }

    public Optional<Integer> find(I id) {
        return Optional.ofNullable(identifierToId.get(id));
    }

    public Optional<Integer> find(Predicate<I> predicate) {
        return identifierToId.entrySet().stream()
                .filter(entry -> predicate.test(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public boolean contains(Predicate<I> predicate) {
        return find(predicate).isPresent();
    }

    public boolean contains(I identifier) {
        return identifierToId.containsKey(identifier);
    }

    public LookupTable<Integer> constructIdToIdLookupTable(LookupTable<I> oldIds) {
        LookupTable<Integer> oldIdToNewId = new LookupTable<>();
        oldIds.identifierToId.forEach((identifier, oldId) -> find(identifier)
                .ifPresent(newId -> oldIdToNewId.put(oldId, newId)));
        identifierToId.forEach((identifier, newId) -> oldIds.find(identifier)
                .ifPresent(oldId -> oldIdToNewId.put(oldId, newId)));
        return oldIdToNewId;
    }

    public Set<I> keySet() {return identifierToId.keySet();}

}
