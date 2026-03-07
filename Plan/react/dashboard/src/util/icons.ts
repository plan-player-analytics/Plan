export enum IconFamily {
    SOLID = "SOLID",
    REGULAR = "REGULAR",
    BRAND = "BRAND",
}

export const iconTypeToFontAwesomeClass = (type: IconFamily | undefined) => {
    switch (type) {
        case IconFamily.REGULAR:
            return "far"
        case IconFamily.BRAND:
            return "fab";
        case IconFamily.SOLID:
        default:
            return "fas";
    }
}