import {flattenObject} from '../../util/mutator';
import {getContrastColor, hsvToHex, hsxStringToArray, withReducedSaturation} from '../../util/colors';

// Function to generate CSS variables from theme data
const generateThemeCSS = ({theme, useCases, nightModeUseCases}) => {
    const baseVariables = [];
    const nightModeVariables = [];

    // Helper to add both color and its contrast
    const addColorWithContrast = (name, color, variables) => {
        variables.push(`--col-${name}: ${color}`);
        // If color is HSL string (from withReducedSaturation), convert to hex for contrast
        const hexColor = color.startsWith('hsl') ? hsvToHex(hsxStringToArray(color)) : color;
        variables.push(`--contrast-col-${name}: ${getContrastColor(hexColor)}`);
    };

    // Add regular colors
    Object.entries(theme.colors).forEach(([key, value]) => {
        addColorWithContrast(key, value, baseVariables);
        // Add desaturated version for night mode
        const nightColor = withReducedSaturation(value);
        addColorWithContrast(key, nightColor, nightModeVariables);
    });

    // Add night mode colors
    Object.entries(theme.nightColors).forEach(([key, value]) => {
        addColorWithContrast(key, value, baseVariables);
        addColorWithContrast(key, value, nightModeVariables);
    });

    // Add pie chart colors
    theme.pieColors.forEach((color, index) => {
        addColorWithContrast(`pie-${index + 1}`, color, baseVariables);
        const nightColor = withReducedSaturation(color);
        addColorWithContrast(`pie-${index + 1}`, nightColor, nightModeVariables);
    });

    // Add use case variables
    const flattenedUseCases = flattenObject(useCases);
    Object.entries(flattenedUseCases).forEach(([key, value]) => {
        if (typeof value === 'string' && value.startsWith('var(--col-')) {
            const referencedColor = value.replace('var(--col-', '').replace(')', '');
            baseVariables.push(`--col-${key}: var(--col-${referencedColor})`);
            baseVariables.push(`--contrast-col-${key}: var(--contrast-col-${referencedColor})`);
        }
    });

    // Add night mode use case variables
    const flattenedNightUseCases = flattenObject(nightModeUseCases);
    Object.entries(flattenedNightUseCases).forEach(([key, value]) => {
        if (typeof value === 'string' && value.startsWith('var(--col-')) {
            const referencedColor = value.replace('var(--col-', '').replace(')', '');
            nightModeVariables.push(`--col-${key}: var(--col-${referencedColor})`);
            nightModeVariables.push(`--contrast-col-${key}: var(--contrast-col-${referencedColor})`);
        }
    });

    return `
:root {
  ${baseVariables.join(';\n  ')};
  background-color: var(--col-white-grey);
  color: var(--col-text-light);
}

.night-mode-colors {
  ${nightModeVariables.join(';\n  ')};
  background-color: var(--col-night-grey-blue);
  color: var(--col-night-text);
}`;
};

export const ThemeStyleCss = ({theme, useCases, nightModeUseCases}) => {
    return (
        <style>{generateThemeCSS({theme, useCases, nightModeUseCases})}</style>
    )
}