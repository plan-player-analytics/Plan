// Function to flatten nested object into dot notation
import { cssVariableToName } from './colors.js';
import {
    ColorPropertyList,
    ColorPropertyMap,
    UseCase,
    UseCaseMap,
} from '../components/theme/model/ThemeConfig';

export const flattenObject = (
    obj: UseCaseMap,
    prefix = '',
): ColorPropertyMap => {
    return Object.entries(obj).reduce<ColorPropertyMap>((acc, [key, value]) => {
        const newKey = prefix ? `${prefix}-${key}` : key;
        if (
            typeof value === 'object' &&
            value !== null &&
            !Array.isArray(value)
        ) {
            Object.assign(acc, flattenObject(value, newKey));
        } else if (!Array.isArray(value)) {
            // Convert camelCase to kebab-case
            const cssKey = newKey
                .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
                .toLowerCase();
            acc[cssKey] = value;
        }
        return acc;
    }, {});
};

type Mergeable = { [key: string]: unknown };

// Function to merge two objects recursively, with override taking precedence
export const mergeUseCases = <T extends Mergeable>(base: T, override: T): T => {
    const merged: Mergeable = { ...base };
    for (const key in override) {
        if (
            typeof override[key] === 'object' &&
            !Array.isArray(override[key])
        ) {
            merged[key] = mergeUseCases(
                (base[key] || {}) as Mergeable,
                override[key] as Mergeable,
            );
        } else {
            merged[key] = override[key];
        }
    }
    return merged as T;
};

export const addToObject = <T extends Record<string, unknown>>(
    base: T,
    toAdd?: T,
): T => {
    if (!toAdd) return base;
    Object.entries(toAdd).forEach(([key, value]) => {
        base[key as keyof T] = value as T[keyof T];
    });
    return base;
};

type Replaced<T extends UseCase> = T extends string
    ? string
    : T extends ColorPropertyList
      ? ColorPropertyList
      : UseCaseMap;

export const recursiveFindAndReplaceValue = <T extends UseCase>(
    object: T,
    toReplace: string,
    replaceWith: string,
): Replaced<T> => {
    if (Array.isArray(object)) {
        const toReplaceName = cssVariableToName(toReplace);
        const toReplaceWithName = cssVariableToName(replaceWith);
        return object.map((item) =>
            item === toReplaceName ? toReplaceWithName : item,
        ) as Replaced<T>;
    } else if (object !== null && typeof object === 'object') {
        const result: UseCaseMap = {};
        for (const [key, value] of Object.entries(object)) {
            result[key] = recursiveFindAndReplaceValue(
                value,
                toReplace,
                replaceWith,
            );
        }
        return result as Replaced<T>;
    } else {
        return (toReplace === object ? replaceWith : object) as Replaced<T>;
    }
};
