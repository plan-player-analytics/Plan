import {getColorArrayConverter, getColorConverter} from "./Color.js";

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

export const nameToCssVariable = name => {
    return `var(--color-${name})`;
}

export const nameToContrastCssVariable = name => {
    return `var(--contrast-color-${name})`;
}

export const cssVariableToName = cssVariable => {
    return cssVariable?.replace('var(--color-', '').replace(')', '')
}

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

export const hsxStringToArray = (hsxString) => {
    const color = hsxString.substring(4, hsxString.length - 1);
    const split = color.split(',');
    const h = Number(split[0]);
    const s = Number(split[1].substring(0, split[1].length - 1));
    const x = Number(split[2].substring(0, split[2].length - 1));
    return [h, s, x];
}

export const hslaStringToArray = (hslaString) => {
    const color = hslaString.substring(4, hslaString.length - 1);
    const split = color.split(',');
    const h = Number(split[0]);
    const s = Number(split[1].substring(0, split[1].length - 1));
    const l = Number(split[2].substring(0, split[2].length - 1));
    const a = Number(split[3].substring(0, split[3].length - 1));
    return [h, s, l, a];
}

export const hslToHsv = ([h, s, l]) => {
    const hsv1 = s * (l < 50 ? l : 100 - l) / 100;
    const hsvS = hsv1 === 0 ? 0 : 2 * hsv1 / (l + hsv1) * 100;
    const hsvV = l + hsv1;
    return [h, hsvS, hsvV];
}

export const hsvToHex = (hsv) => {
    return rgbToHexString(hsvToRgb(hsv));
}

export const hsvToRgb = ([h, s, v]) => {
    let r, g, b;

    if (h > 1 || s > 1 || v > 1) {
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

export const rgbStringToArray = (rgbString) => {
    const colors = rgbString.substring(4, rgbString.length - 1);
    const split = colors.split(',');
    return [
        Number(split[0].trim()),
        Number(split[1].trim()),
        Number(split[2].trim())
    ];
}

export const rgbaStringToArray = (rgbaString) => {
    const colors = rgbaString.substring(5, rgbaString.length - 1);
    const split = colors.split(',');
    return [
        Number(split[0].trim()),
        Number(split[1].trim()),
        Number(split[2].trim()),
        Number(split[3].trim())
    ];
}

export const rgbToHexString = ([r, g, b]) => {
    return '#' + rgbToHex(r) + rgbToHex(g) + rgbToHex(b);
}

const rgbToHex = (component) => {
    return Math.floor(component).toString(16).padStart(2, '0');
}

export const hexToRgb = (hexString) => {
    const hex = hexString.replace('#', '');
    if (hex.length === 6) {
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);
        return [r, g, b];
    } else {
        // 3 digit hex
        const rLetter = hex.substring(0, 1);
        const gLetter = hex.substring(1, 2);
        const bLetter = hex.substring(2, 3);
        const r = parseInt(rLetter + rLetter, 16);
        const g = parseInt(gLetter + gLetter, 16);
        const b = parseInt(bLetter + bLetter, 16);
        return [r, g, b];
    }
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

export const withReducedSaturationRgba = (rgba, reduceSaturationPercentage) => {
    const saturationReduction = reduceSaturationPercentage ? reduceSaturationPercentage : 0.70;

    const [h, s, l] = getColorArrayConverter(rgba, 'rgba').toHslArray();
    if (isNaN(h)) console.log(rgba, [h, s, l]);

    return 'hsl(' + h * 360 + ',' + s * 100 * saturationReduction + '%,' + l * 95 + '%)';
}

export const rgbToString = ([r, g, b]) => {
    return `rgb(${r}, ${g}, ${b})`;
}

export const rgbaToString = ([r, g, b, a]) => {
    return `rgba(${r}, ${g}, ${b}, ${a})`;
}

export const hslToString = ([h, s, l]) => {
    return `hsl(${h}, ${s}%, ${l}%)`;
}

export const hsvToString = ([h, s, v]) => {
    return `hsv(${h}, ${s}%, ${v}%)`;
}

export const calculateCssHexColor = (cssColor) => {
    const colorCalculationElement = document.createElement('div');
    colorCalculationElement.style.display = 'none';
    colorCalculationElement.style.color = cssColor;
    document.body.appendChild(colorCalculationElement);
    const rgbString = window.getComputedStyle(colorCalculationElement, null).getPropertyValue("color");
    const hex = rgbToHexString(rgbStringToArray(rgbString));
    document.body.removeChild(colorCalculationElement);
    return hex;
}

export const calculateCssColors = (cssSelector) => {
    const colors = {
        color: null,
        backgroundColor: null,
        borderColor: null
    };

    // Search through all document stylesheets
    for (const stylesheet of document.styleSheets) {
        try {
            // Skip if we can't access the rules (e.g., cross-origin stylesheets)
            if (!stylesheet.cssRules) continue;

            // Look through all rules in the stylesheet
            for (const rule of stylesheet.cssRules) {
                if (rule instanceof CSSStyleRule && rule.selectorText === cssSelector) {
                    const style = rule.style;

                    // Get color if set
                    if (style.color) {
                        colors.color = style.color;
                    }

                    // Get background-color if set
                    if (style.backgroundColor) {
                        colors.backgroundColor = style.backgroundColor;
                    }

                    // Get border-color if set
                    if (style.borderColor) {
                        colors.borderColor = style.borderColor;
                    }
                }
            }
        } catch (e) {
            // Skip stylesheets we can't access

        }
    }

    return colors;
}

export const extractUniqueSelectors = (cssString) => {
    // Remove line breaks and extra spaces to simplify parsing
    const normalizedCss = cssString.replace(/\n/g, ' ').replace(/\s+/g, ' ');

    // Match all CSS selectors before curly braces, handling multiple selectors separated by commas
    const selectorMatches = normalizedCss.match(/[^}]+?{/g) || [];

    // List of pseudo-classes we want to keep
    const keepPseudoClasses = [':hover', ':checked', ':active', ':focus'];

    // Process each selector group
    const allSelectors = selectorMatches
        .map(match => {
            // Remove the trailing curly brace and trim
            const selectorGroup = match.slice(0, -1).trim();
            // Split by comma and trim each selector
            return selectorGroup.split(',').map(s => s.trim());
        })
        .flat();

    // Remove unwanted selectors and deduplicate
    const uniqueSelectors = [...new Set(allSelectors)]
        .filter(selector => {
            if (!selector ||
                selector === ':root' ||
                selector.includes('@') ||  // Remove any @media or other @ rules
                selector.includes('::') || // Remove pseudo-elements
                selector === '*'          // Remove universal selector
            ) {
                return false;
            }

            // Check if selector contains any pseudo-class
            if (selector.includes(':')) {
                // Only keep selector if it contains one of our wanted pseudo-classes
                return keepPseudoClasses.some(pseudo => selector.includes(pseudo));
            }

            return true;
        });

    return uniqueSelectors;
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

export const getContrastColor = (color) => {
    const converter = getColorConverter(color);
    if (!converter) return undefined;
    const luminance = converter.toLuminance();
    return luminance < 0.5 ? '#ffffff' : '#000000';
};
