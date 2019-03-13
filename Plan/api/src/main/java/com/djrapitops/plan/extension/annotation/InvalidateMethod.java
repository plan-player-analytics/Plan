package com.djrapitops.plan.extension.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to invalidate old method values.
 * <p>
 * The name of the methods are used as an identifier in the database, so that a single provider does not duplicate entries.
 * Only first 50 characters of the method name are stored.
 * If you need to change a method name add this class annotation with the old method name.
 *
 * @author Rsl1122
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(InvalidateMethod.Multiple.class)
public @interface InvalidateMethod {

    /**
     * Name of the old method, values of which should be removed from the database.
     *
     * @return Name of the old method, case sensitive. Only first 50 characters are used.
     */
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Multiple {

        InvalidateMethod[] value();

    }

}
