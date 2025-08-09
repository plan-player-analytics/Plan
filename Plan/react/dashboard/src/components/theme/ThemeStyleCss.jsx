import {addToObject, flattenObject} from '../../util/mutator';
import {getContrastColor, nameToCssVariable} from '../../util/colors';
import {getColorConverter} from "../../util/Color.js";
import {useThemeEditContext} from "../../hooks/context/themeEditContextHook.jsx";
import {useThemeStorage} from "../../hooks/context/themeContextHook.jsx";

// Function to generate CSS variables from theme data
const generateThemeCSS = ({applyToClass, colors, nightColors, useCases, nightModeUseCases, color}) => {
    const baseVariables = [];
    const nightModeVariables = [];

    // Helper to add both color and its contrast
    const addColorWithContrast = (name, color, variables) => {
        variables.push(`--color-${name}: ${color}`);
        variables.push(`--contrast-color-${name}: ${getContrastColor(color)}`);
    };

    // Add regular colors
    Object.entries(colors).forEach(([key, value]) => {
        addColorWithContrast(key, value, baseVariables);
        // Add desaturated version for night mode
        const converter = getColorConverter(value);

        const nightColor = converter ? converter.reduceSaturation().toRgbaString() : value;
        addColorWithContrast(key, nightColor, nightModeVariables);
    });

    // Add night mode colors
    Object.entries(nightColors).forEach(([key, value]) => {
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
    let flattenedNightUseCases = addToObject(flattenObject(nightModeUseCases), nightModeUseCases.referenceColors);
    // Override with user selected theme color
    if (color && color !== 'theme') flattenedNightUseCases = addToObject(flattenedNightUseCases, {theme: nameToCssVariable(color)});
    Object.entries(flattenedNightUseCases).forEach(([key, value]) => {
        if (typeof value === 'string' && value.startsWith('var(--color-')) {
            const referencedColor = value.replace('var(--color-', '').replace(')', '');
            nightModeVariables.push(`--color-${key}: var(--color-${referencedColor})`);
            nightModeVariables.push(`--contrast-color-${key}: var(--contrast-color-${referencedColor})`);
        }
    });

    const nightModeKeys = Object.keys(flattenedNightUseCases);
    // Add use case variables
    let flattenedUseCases = addToObject(flattenObject(useCases), useCases.referenceColors);
    // Override with user selected theme color
    if (color && color !== 'theme') flattenedUseCases = addToObject(flattenedUseCases, {theme: nameToCssVariable(color)});
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
${applyToClass ? `.${applyToClass}` : ':root'} {
  ${baseVariables.join(';\n  ')};
  --editor-bg-color: var(--color-white-grey);
  color: var(--color-text-light);
}

${applyToClass ? `.${applyToClass}.night-mode-colors,.${applyToClass} .night-mode-colors` : '.night-mode-colors'} {
  ${nightModeVariables.join(';\n  ')};
  --editor-bg-color: var(--color-night-dark-blue);
  color: var(--color-night-text);
}`;
};

export const ThemeStyleCss = ({editMode, applyToClass}) => {
    const {
        loaded, color,
        currentColors: colors, currentNightColors: nightColors,
        currentUseCases: useCases, currentNightModeUseCases: nightModeUseCases
    } = editMode ? useThemeEditContext() : useThemeStorage();

    if (!loaded) return <></>
    return (
        <style>{generateThemeCSS({applyToClass, colors, nightColors, useCases, nightModeUseCases, color})}</style>
    )
}