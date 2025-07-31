export const formatTimeAmount = (ms) => {
    let out = "";

    let seconds = Math.floor(ms / 1000);

    const dd = Math.floor(seconds / 86400);
    seconds -= (dd * 86400);
    const dh = Math.floor(seconds / 3600);
    seconds -= (dh * 3600);
    const dm = Math.floor(seconds / 60);
    seconds -= (dm * 60);
    seconds = Math.floor(seconds);
    if (dd !== 0) {
        out += dd.toString() + "d ";
    }
    if (dh !== 0) {
        out += dh.toString() + "h ";
    }
    if (dm !== 0) {
        out += dm.toString() + "m ";
    }
    out += seconds.toString() + "s ";

    return out;
}

export const formatDecimals = (value, formatPattern) => {
    if (!formatPattern || isNaN(value)) return value;
    const split = formatPattern.split('.');
    if (split.length <= 1) return value.toFixed(0);
    return value.toFixed(split[1].length);
}

export const capitalize = text => {
    return text.charAt(0).toUpperCase() + text.slice(1);
}