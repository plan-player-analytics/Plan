// Function to flatten nested object into dot notation
export const flattenObject = (obj, prefix = '') => {
    return Object.entries(obj).reduce((acc, [key, value]) => {
        const newKey = prefix ? `${prefix}-${key}` : key;
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            Object.assign(acc, flattenObject(value, newKey));
        } else if (!Array.isArray(value)) {
            // Convert camelCase to kebab-case
            const cssKey = newKey.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
            acc[cssKey] = value;
        }
        return acc;
    }, {});
};

// Function to merge two objects recursively, with override taking precedence
export const mergeUseCases = (base, override) => {
    const merged = {...base};
    for (const key in override) {
        if (typeof override[key] === 'object' && !Array.isArray(override[key])) {
            merged[key] = mergeUseCases(base[key] || {}, override[key]);
        } else {
            merged[key] = override[key];
        }
    }
    return merged;
};

export const addToObject = (base, toAdd) => {
    if (!toAdd) return base;
    Object.entries(toAdd).forEach(([key, value]) => {
        base[key] = value
    });
    return base;
}

export const recursiveFindAndReplaceValue = (object, toReplace, replaceWith) => {
    // Normalize toReplace to an array for easier checking
    const toReplaceArr = Array.isArray(toReplace) ? toReplace : [toReplace];

    if (Array.isArray(object)) {
        return object.map(item => recursiveFindAndReplaceValue(item, toReplaceArr, replaceWith));
    } else if (object !== null && typeof object === 'object') {
        const result = {};
        for (const [key, value] of Object.entries(object)) {
            result[key] = recursiveFindAndReplaceValue(value, toReplaceArr, replaceWith);
        }
        return result;
    } else {
        // Primitive value: replace if in toReplaceArr
        return toReplaceArr.includes(object) ? replaceWith : object;
    }
}