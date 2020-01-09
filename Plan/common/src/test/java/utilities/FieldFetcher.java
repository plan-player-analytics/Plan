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
package utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FieldFetcher {

    private FieldFetcher() {
        /* static method class*/
    }

    public static <T> List<T> getPublicStaticFields(Class<?> fromClass, Class<T> ofType) throws IllegalAccessException {
        List<T> list = new ArrayList<>();
        for (Field field : fromClass.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (field.getAnnotationsByType(Deprecated.class).length > 0) {
                continue;
            }
            T key = ofType.cast(field.get(null));
            list.add(key);
        }
        return list;
    }

}
