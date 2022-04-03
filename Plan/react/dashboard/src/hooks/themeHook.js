import {createContext, useContext, useState} from "react";
import {createNightModeCss, getColors} from "../util/colors";
import {getLightModeChartTheming, getNightModeChartTheming} from "../util/graphColors";
import {useMetadata} from "./metadataHook";

const themeColors = getColors();
themeColors.splice(themeColors.length - 4, 4);

const getDefaultTheme = (metadata) => {
    const defaultTheme = metadata.defaultTheme;

    // Use 'plan' if default or if default is undefined.
    // Avoid night mode staying on if default theme is night mode
    const invalidColor = !defaultTheme
        || defaultTheme === 'default'
        || defaultTheme === 'black'
        || defaultTheme === 'white'
        || !themeColors.map(color => color.name).includes(defaultTheme)

    return invalidColor ? 'plan' : defaultTheme;
}

const getStoredTheme = (defaultTheme) => {
    const stored = window.localStorage.getItem('themeColor');
    return stored && stored !== 'undefined' ? stored : defaultTheme;
}

const setStoredTheme = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('themeColor', themeColor);
    }
}

const ThemeContext = createContext({});

export const ThemeContextProvider = ({children}) => {
    const metadata = useMetadata();

    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedColor, setSelectedColor] = useState(getStoredTheme(getDefaultTheme(metadata)));
    const [previousColor, setPreviousColor] = useState(undefined);

    const sharedState = {
        selectedColor,
        setSelectedColor,
        previousColor,
        setPreviousColor,
        colorChooserOpen,
        setColorChooserOpen
    }
    return (<ThemeContext.Provider value={sharedState}>
            {children}
        </ThemeContext.Provider>
    )
}

const lightModeChartTheming = getLightModeChartTheming();
const nightModeChartTheming = getNightModeChartTheming();

export const useTheme = () => {
    const {
        selectedColor,
        setSelectedColor,
        previousColor,
        setPreviousColor,
        colorChooserOpen,
        setColorChooserOpen
    } = useContext(ThemeContext);

    const metadata = useMetadata();

    const setTheme = color => {
        setStoredTheme(color);
        setSelectedColor(color);
    }

    if (!selectedColor) setTheme(selectedColor);

    const toggleColorChooser = () => {
        setColorChooserOpen(!colorChooserOpen);
    }

    const isNightModeEnabled = () => {
        return selectedColor === 'night';
    }

    const toggleNightMode = () => {
        if (isNightModeEnabled()) {
            setTheme(previousColor ? previousColor : getDefaultTheme(metadata));
        } else {
            setPreviousColor(selectedColor);
            setTheme('night');
        }
    }

    const nightModeEnabled = isNightModeEnabled();
    return {
        color: selectedColor,
        setColor: setTheme,
        nightModeEnabled: nightModeEnabled,
        colorChooserOpen: colorChooserOpen,
        nightModeCss: nightModeEnabled ? createNightModeCss() : undefined,
        toggleNightMode: toggleNightMode,
        toggleColorChooser: toggleColorChooser,
        themeColors: themeColors,
        graphTheming: nightModeEnabled ? nightModeChartTheming : lightModeChartTheming
    };
}

export const NightModeCss = () => {
    const theme = useTheme();

    return (
        <>
            {theme.nightModeEnabled ? <style>
                {theme.nightModeCss}
            </style> : ''}
        </>
    );
}