const colorMap = {
    RED: "col-red",
    PINK: "col-pink",
    PURPLE: "col-purple",
    DEEP_PURPLE: "col-deep-purple",
    INDIGO: "col-indigo",
    BLUE: "col-blue",
    LIGHT_BLUE: "col-light-blue",
    CYAN: "col-cyan",
    TEAL: "col-teal",
    GREEN: "col-green",
    LIGHT_GREEN: "col-light-green",
    LIME: "col-lime",
    YELLOW: "col-yellow",
    AMBER: "col-amber",
    ORANGE: "col-orange",
    DEEP_ORANGE: "col-deep-orange",
    BROWN: "col-brown",
    GREY: "col-grey",
    BLUE_GREY: "col-blue-grey",
    BLACK: "col-black",
    NONE: ""
};

export const colorEnumToColorClass = color => {
    const mapped = colorMap[color];
    return mapped ? mapped : "";
}

export const bgClassToColorClass = bgClass => {
    return "col-" + bgClass.substring(3);
}

export const colorClassToColorName = (colorClass) => {
    return colorClass.substring(4);
}

export const colorClassToBgClass = colorClass => {
    return "bg-" + colorClassToColorName(colorClass);
}

