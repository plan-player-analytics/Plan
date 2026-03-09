type stringOrUndefined = string | undefined

export const classNames = (...names: stringOrUndefined[]) => {
    return names.filter(Boolean).join(' ');
}