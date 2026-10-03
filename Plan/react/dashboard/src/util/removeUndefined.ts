export function removeUndefined<T>(value: T): T {
    if (Array.isArray(value)) {
        return value
            .filter((item) => item !== undefined)
            .map((item) => removeUndefined(item)) as T;
    }

    if (value !== null && typeof value === "object") {
        return Object.fromEntries(
            Object.entries(value)
                .filter(([, v]) => v !== undefined)
                .map(([key, v]) => [key, removeUndefined(v)])
        ) as T;
    }

    return value;
}