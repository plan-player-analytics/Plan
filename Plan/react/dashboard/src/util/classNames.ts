type stringOrUndefined = string | undefined | { [key: string]: boolean }

export const classNames = (...names: stringOrUndefined[]) => {
    return names
        .filter(item => item !== undefined)
        .map(item => {
            if (typeof item === 'object') {
                return [Object.entries(item).filter(e => e[1]).map(e => e[0])].join(' ')
            }
            return item;
        }).join(' ');
}