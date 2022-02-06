export const iconTypeToFontAwesomeClass = type => {
    switch (type) {
        case "SOLID":
            return "fas";
        case "REGULAR":
            return "far"
        case "BRAND":
            return "fab";
        default:
            return "fas";
    }
}

export const iconNameToCamelCase = icon => {
    return icon.replace(/-([a-z])/g, (group) => group[1].toUpperCase());
}