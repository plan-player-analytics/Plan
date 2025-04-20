const colorMap = {
    PLAN: {
        name: "plan",
        hex: "#468F17"
    },
    RED: {
        name: "red",
        hex: "#F44336"
    },
    PINK: {
        name: "pink",
        hex: "#E91E63"
    },
    PURPLE: {
        name: "purple",
        hex: "#9C27B0"
    },
    DEEP_PURPLE: {
        name: "deep-purple",
        hex: "#673AB7"
    },
    INDIGO: {
        name: "indigo",
        hex: "#3F61B5"
    },
    BLUE: {
        name: "blue",
        hex: "#2196F3"
    },
    LIGHT_BLUE: {
        name: "light-blue",
        hex: "#03A9F4"
    },
    CYAN: {
        name: "cyan",
        hex: "#00BCD4"
    },
    TEAL: {
        name: "teal",
        hex: "#009688"
    },
    GREEN: {
        name: "green",
        hex: "#4CAF50"
    },
    LIGHT_GREEN: {
        name: "light-green",
        hex: "#8BC34A"
    },
    LIME: {
        name: "lime",
        hex: "#CDDC39"
    },
    YELLOW: {
        name: "yellow",
        hex: "#FFE821"
    },
    AMBER: {
        name: "amber",
        hex: "#FFC107"
    },
    ORANGE: {
        name: "orange",
        hex: "#FF9800"
    },
    DEEP_ORANGE: {
        name: "deep-orange",
        hex: "#FF5722"
    },
    BROWN: {
        name: "brown",
        hex: "#795548"
    },
    GREY: {
        name: "grey",
        hex: "#9E9E9E"
    },
    BLUE_GREY: {
        name: "blue-grey",
        hex: "#607D8B"
    },
    BLACK: {
        name: "black",
        hex: "#555555"
    },
    SUCCESS: {
        name: "success",
        hex: "#1CC88A"
    },
    WARNING: {
        name: "warning",
        hex: "#F6C23E"
    },
    DANGER: {
        name: "danger",
        hex: "#e74A3B"
    },
    NONE: ""
};

export const getColors = () => {
    return Object.values(colorMap).filter(color => color);
}

export const colorEnumToColorClass = color => {
    const mapped = "col-" + colorMap[color].name;
    return mapped ? mapped : "";
}

export const bgClassToColorClass = bgClass => {
    return "col-" + bgClass.substring(3);
}

export const colorClassToColorName = (colorClass) => {
    return colorClass.substring(4);
}

export const colorEnumToBgClass = color => {
    return "bg-" + color;
}

export const colorClassToBgClass = colorClass => {
    return "bg-" + colorClassToColorName(colorClass);
}

export const hsxStringToArray = (hsvString) => {
    const color = hsvString.substring(4, hsvString.length - 1);
    const split = color.split(',');
    const h = Number(split[0]);
    const s = Number(split[1].substring(0, split[1].length - 1));
    const x = Number(split[2].substring(0, split[2].length - 1));
    return [h, s, x];
}

export const hslToHsv = ([h, s, l]) => {
    const hsv1 = s * (l < 50 ? l : 100 - l) / 100;
    const hsvS = hsv1 === 0 ? 0 : 2 * hsv1 / (l + hsv1) * 100;
    const hsvV = l + hsv1;
    return [h, hsvS, hsvV];
}

export const hsvToRgb = ([h, s, v]) => {
    let r, g, b;

    if (s > 1) {
        h = h / 360;
        s = s / 100;
        v = v / 100;
    }

    const i = Math.floor(h * 6);
    const f = h * 6 - i;
    const p = v * (1 - s);
    const q = v * (1 - f * s);
    const t = v * (1 - (1 - f) * s);

    switch (i % 6) {
        case 0:
            r = v;
            g = t;
            b = p;
            break;
        case 1:
            r = q;
            g = v;
            b = p;
            break;
        case 2:
            r = p;
            g = v;
            b = t;
            break;
        case 3:
            r = p;
            g = q;
            b = v;
            break;
        case 4:
            r = t;
            g = p;
            b = v;
            break;
        case 5:
            r = v;
            g = p;
            b = q;
            break;
        default:
            break;
    }

    return [r * 255, g * 255, b * 255];
}

export const randomHSVColor = (i) => {
    const goldenRatioConjugate = 0.618033988749895;
    const hue = i * goldenRatioConjugate % 1;
    const saturation = 0.7;
    const value = 0.7 + (Math.random() / 10);
    return [hue, saturation, value]
}

export const rgbToHexString = ([r, g, b]) => {
    return '#' + rgbToHex(r) + rgbToHex(g) + rgbToHex(b);
}

const rgbToHex = (component) => {
    return Math.floor(component).toString(16).padStart(2, '0');
}

export const hexToRgb = (hexString) => {
    const r = parseInt(hexString.substring(1, 3), 16);
    const g = parseInt(hexString.substring(3, 5), 16);
    const b = parseInt(hexString.substring(5, 7), 16);
    return [r, g, b];
}

// https://css-tricks.com/converting-color-spaces-in-javascript/
export const rgbToHsl = ([r, g, b]) => {
    r /= 255;
    g /= 255;
    b /= 255;
    const max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h, s;
    const l = (max + min) / 2;

    if (max === min) {
        h = s = 0; // achromatic
    } else {
        const d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch (max) {
            case r:
                h = (g - b) / d + (g < b ? 6 : 0);
                break;
            case g:
                h = (b - r) / d + 2;
                break;
            case b:
                h = (r - g) / d + 4;
                break;
            default:
                break;
        }
        h /= 6;
    }
    return [h, s, l];
}

// From https://stackoverflow.com/a/3732187
export const withReducedSaturation = (hex, reduceSaturationPercentage) => {
    const saturationReduction = reduceSaturationPercentage ? reduceSaturationPercentage : 0.70;

    const rgb = hexToRgb(hex);
    const [h, s, l] = rgbToHsl(rgb);

    // To css property
    return 'hsl(' + h * 360 + ',' + s * 100 * saturationReduction + '%,' + l * 95 + '%)';
}

const createNightModeColorCss = () => {
    return ':root {' + getColors()
        .filter(color => color.name !== 'white' && color.name !== 'black' && color.name !== 'plan')
        .map(color => {
            const desaturatedColor = withReducedSaturation(color.hex);
            return `--color-${color.name}: ${desaturatedColor} !important;`
        }).join('') + '}';
}

export const createNightModeCss = () => {
    return `#content-wrapper {background-color:var(--color-night-black)!important;}` +
        `#wrapper {background-image: linear-gradient(to right, var(--color-night-dark-blue) 0%, var(--color-night-dark-blue) 14rem, var(--color-night-black) 14.01rem, var(--color-night-black) 100%);}` +
        `body,.btn,.bg-transparent-light {color: var(--color-night-text-dark-bg) !important;}` +
        `.card,.bg-white,.modal-content,.page-loader,.nav-tabs .nav-link:hover,.nav-tabs,hr,form .btn, .btn-outline-secondary{background-color:var(--color-night-dark-blue)!important;border-color:var(--color-night-blue)!important;}` +
        `.bg-white.collapse-inner {border:1px solid;}` +
        `.card-header {background-color:var(--color-night-dark-blue);border-color:var(--color-night-blue);}` +
        `#content,.col-black,.text-gray-900,.text-gray-800,.collapse-item,.modal-title,.modal-body,.page-loader,.fc-title,.fc-time,pre,.table-dark,input::placeholder{color:var(--color-night-text-dark-bg) !important;}` +
        `.collapse-item:hover,.nav-link.active {background-color: var(--color-night-dark-grey-blue) !important;}` +
        `.nav-tabs .nav-link.active {background-color: var(--color-night-dark-blue) !important;border-color:var(--color-night-blue) var(--color-night-blue) var(--color-night-dark-blue) !important;}` +
        `.fc-today {background:var(--color-night-grey-blue) !important}` +
        `.fc-popover-body,.fc-popover-header{background-color: var(--color-night-dark-blue) !important;color: var(--color-night-text-dark-bg) !important;}` +
        `select,input,.dataTables_paginate .page-item:not(.active) a,.input-group-text,.input-group-text > * {background-color:var(--color-night-dark-blue) !important;border-color:var(--color-night-blue) !important;color: var(--color-night-text-dark-bg) !important;}` +
        `input.form-check-input:checked {background-color:var(--color-night-blue) !important;border-color:var(--color-night-blue) !important;color: var(--color-night-text-dark-bg) !important;}` +
        `.topbar-divider,.fc td,.fc tr,.fc th, .fc table, .modal-header,.modal-body,.modal-footer{border-color:var(--color-night-blue) !important;}` +
        `.fc a{color:var(--color-night-text-dark-bg) !important;}` +
        `.fc-button{ background-color: ${withReducedSaturation(colorMap.PLAN.hex)} !important;}` +
        `.loader{border: 4px solid var(--color-plan); background-color: var(--color-plan);}` +
        `.dropdown-item,.dropdown-header{color: var(--color-night-text-dark-bg) !important;}` +
        `.dropdown-item:hover{background-color: var(--color-night-blue) !important;}` +
        `.dropdown-menu{border-color:var(--color-night-blue);color: var(--color-night-blue) !important;}` +
        `.col-theme{--color-theme: var(--color-night-text-dark-bg)}` +
        `:root {--bs-heading-color:var(--color-night-text-dark-bg); --bs-card-color:var(--color-night-text-dark-bg); --bs-body-color:var(--color-night-text-dark-bg); --bs-body-bg:var(--color-night-dark-grey-blue); --bs-btn-active-border-color:var(--color-night-blue);}` +
        createNightModeColorCss()
}