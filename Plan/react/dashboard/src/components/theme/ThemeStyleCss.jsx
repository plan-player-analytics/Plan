import {flattenObject} from '../../util/mutator';
import {getContrastColor} from '../../util/colors';
import {getColorConverter} from "../../util/Color.js";

// Function to generate CSS variables from theme data
const generateThemeCSS = ({theme, useCases, nightModeUseCases}) => {
    const baseVariables = [];
    const nightModeVariables = [];

    // Helper to add both color and its contrast
    const addColorWithContrast = (name, color, variables) => {
        variables.push(`--color-${name}: ${color}`);
        variables.push(`--contrast-color-${name}: ${getContrastColor(color)}`);
    };

    // Add regular colors
    Object.entries(theme.colors).forEach(([key, value]) => {
        addColorWithContrast(key, value, baseVariables);
        // Add desaturated version for night mode
        const nightColor = getColorConverter(value).reduceSaturation().toRgbaString()
        addColorWithContrast(key, nightColor, nightModeVariables);
    });

    // Add night mode colors
    Object.entries(theme.nightColors).forEach(([key, value]) => {
        addColorWithContrast(key, value, baseVariables);
        addColorWithContrast(key, value, nightModeVariables);
    });

    // Add pie chart colors
    // theme.pieColors.forEach((color, index) => {
    //     addColorWithContrast(`pie-${index + 1}`, color, baseVariables);
    //     const nightColor = withReducedSaturation(color);
    //     addColorWithContrast(`pie-${index + 1}`, nightColor, nightModeVariables);
    // });

    // Add night mode use case variables
    const flattenedNightUseCases = flattenObject(nightModeUseCases);
    Object.entries(flattenedNightUseCases).forEach(([key, value]) => {
        if (typeof value === 'string' && value.startsWith('var(--color-')) {
            const referencedColor = value.replace('var(--color-', '').replace(')', '');
            nightModeVariables.push(`--color-${key}: var(--color-${referencedColor})`);
            nightModeVariables.push(`--contrast-color-${key}: var(--contrast-color-${referencedColor})`);
        }
    });

    const nightModeKeys = Object.keys(flattenedNightUseCases);
    // Add use case variables
    const flattenedUseCases = flattenObject(useCases);
    Object.entries(flattenedUseCases).forEach(([key, value]) => {
        if (typeof value === 'string' && value.startsWith('var(--color-')) {
            const referencedColor = value.replace('var(--color-', '').replace(')', '');
            baseVariables.push(`--color-${key}: var(--color-${referencedColor})`);
            baseVariables.push(`--contrast-color-${key}: var(--contrast-color-${referencedColor})`);
            if (!nightModeKeys.includes(key)) {
                nightModeVariables.push(`--color-${key}: var(--color-${referencedColor})`);
                nightModeVariables.push(`--contrast-color-${key}: var(--contrast-color-${referencedColor})`);
            }
        }
    });


    return `
:root {
  ${baseVariables.join(';\n  ')};
  background-color: var(--color-white-grey);
  color: var(--color-text-light);
}

.night-mode-colors {
  ${nightModeVariables.join(';\n  ')};
  background-color: var(--color-night-dark-blue);
  color: var(--color-night-text);
}`;
};

export const ThemeStyleCss = ({theme, useCases, nightModeUseCases}) => {
    return (
        <style>{generateThemeCSS({theme, useCases, nightModeUseCases})}</style>
    )
}