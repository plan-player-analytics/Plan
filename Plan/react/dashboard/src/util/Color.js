import {
    hexToRgb,
    hslaStringToArray,
    hslToHsv,
    hslToString,
    hsvToRgb,
    hsxStringToArray,
    rgbaStringToArray,
    rgbaToString,
    rgbStringToArray,
    rgbToHexString,
    rgbToHsl,
    rgbToString,
    withReducedSaturationRgba
} from "./colors.js";

export const getColorConverter = color => {
    try {
        if (typeof color === 'string') {
            if (color.startsWith('#')) return new HexColor(color);
            if (color.startsWith("rgb(") || color.startsWith("rgba(") && color.endsWith(')')) return new RgbaColor(color);
            if (color.startsWith("hsl(") || color.startsWith("hsla(") && color.endsWith(')')) return new HslaColor(color);
            if (color.startsWith("hsv(") && color.endsWith(')')) return new HsvColor(color);
        }
    } catch (e) {
        console.warn('failed to parse color', color, e);
    }
    return undefined;
}

export const getColorArrayConverter = (color, type) => {
    switch (type) {
        case 'hex':
            return new HexColor(color);
        case 'rgb':
        case 'rgba':
            return new RgbaColor(color);
        case 'hsl':
        case 'hsla':
            return new HslaColor(color);
        case 'hsv':
            return new HsvColor(color);
        default:
            return undefined;
    }
}

export class Color {
    toHex() {
        return rgbToHexString(this.toRgbArray())
    }

    toRgbString() {
        return rgbToString(this.toRgbArray())
    }

    toRgbArray() {
    }

    toRgbaString() {
        return rgbaToString(this.toRgbaArray())
    }

    toRgbaArray() {
        return [...this.toRgbArray(), 1];
    }

    toHslString() {
        return hslToString(this.toHslArray())
    }

    toHslArray() {
        return rgbToHsl(this.toRgbArray())
    }

    toHsvString() {
        return hsvToString(this.toHsvArray());
    }

    toHsvArray() {
        return hslToHsv(this.toHslArray());
    }

    toHsvaArray() {
        const rgba = this.toRgbaArray();
        return [...this.toHslArray(), rgba[3]];
    }

    reduceSaturation(reductionPercentage) {
        const rgba = this.toRgbaArray();
        return new HslaColor([...new HslaColor(withReducedSaturationRgba(rgba, reductionPercentage)).toHslArray(), rgba[3]]);
    }

    toLuminance() {
        const [r, g, b] = this.toRgbArray();
        return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    }
}

class HexColor extends Color {
    constructor(hexString) {
        super();
        this.hexString = hexString;
    }

    toHex() {
        return this.hexString;
    }

    toRgbArray() {
        return hexToRgb(this.hexString)
    }
}

class RgbaColor extends Color {
    constructor(rgba) {
        super();
        if (typeof rgba === 'string') {
            if (rgba.startsWith("rgba")) {
                this.rgba = rgbaStringToArray(rgba);
            } else {
                this.rgba = [...rgbStringToArray(rgba), 1];
            }
        } else {
            if (rgba.length === 4) {
                this.rgba = rgba;
            } else {
                this.rgba = [...rgba, 1];
            }
        }
    }

    toHex() {
        return rgbToHexString(this.toRgbArray());
    }

    toRgbArray() {
        return this.rgba.slice(0, 3);
    }

    toRgbaArray() {
        return this.rgba;
    }
}

class HslaColor extends Color {
    constructor(hsla) {
        super();
        if (typeof hsla === 'string') {
            if (hsla.startsWith("hsla")) {
                this.hsla = hslaStringToArray(hsla);
            } else {
                this.hsla = [...hsxStringToArray(hsla), 1];
            }
        } else {
            if (hsla.length === 4) {
                this.hsla = hsla;
            } else {
                this.hsla = [...hsla, 1];
            }
        }
    }

    toHex() {
        return rgbToHexString(this.toRgbArray());
    }

    toRgbArray() {
        return hsvToRgb(this.toHsvArray())
    }

    toRgbaArray() {
        return [...this.toRgbArray(), this.hsla[3]]
    }

    toHslArray() {
        return this.hsla.slice(0, 3);
    }

    toHsvArray() {
        return hslToHsv(this.toHslArray())
    }
}

class HsvColor extends Color {
    constructor(hsv) {
        super();
        if (typeof hsv === 'string') {
            this.hsv = hsxStringToArray(hsv);
        } else {
            this.hsv = hsv;
        }
    }

    toHsvArray() {
        return this.hsv;
    }

    toRgbArray() {
        return hsvToRgb(this.hsv)
    }
}