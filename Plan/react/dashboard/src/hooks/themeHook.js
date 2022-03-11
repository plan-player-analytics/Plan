import {createContext, useContext, useState} from "react";
import {createNightModeCss, getColors} from "../util/colors";
import {getLightModeChartTheming, getNightModeChartTheming} from "../util/graphColors";

const themeColors = getColors();
themeColors.splice(themeColors.length - 4, 4);

const getDefaultTheme = () => {
    const defaultTheme = 'plan'; // TODO Take from backend settings endpoint

    // Avoid night mode staying on if default theme is night mode
    return defaultTheme === 'night' ? 'plan' : defaultTheme;
}

const getStoredTheme = () => {
    const stored = window.localStorage.getItem('themeColor');
    return stored ? stored : 'plan';
}

const setStoredTheme = themeColor => {
    window.localStorage.setItem('themeColor', themeColor);
}

const ThemeContext = createContext({});

export const ThemeContextProvider = ({children}) => {
    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedColor, setSelectedColor] = useState(getStoredTheme());
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
            setTheme(previousColor ? previousColor : getDefaultTheme());
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