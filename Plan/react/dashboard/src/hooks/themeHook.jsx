import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {getChartTheming} from "../util/graphColors";
import {useMetadata} from "./metadataHook";

const getStoredTheme = (defaultTheme) => {
    const stored = window.localStorage.getItem('themeName');
    return stored && stored !== 'undefined' ? stored : defaultTheme;
}

const setStoredTheme = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('themeName', themeColor);
    }
}

const validateTheme = (themeColor, available) => {
    const invalidTheme = !available.includes(themeColor);
    setSelectedColor(invalidTheme ? 'default' : themeColor);
}

const getStoredColor = () => {
    const stored = window.localStorage.getItem('themeColor');
    return stored && stored !== 'undefined' ? stored : 'theme';
}

const setStoredColor = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('themeColor', themeColor);
    }
}

const ThemeContext = createContext({});

export const ThemeContextProvider = ({children}) => {
    const metadata = useMetadata();

    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedColor, setSelectedColor] = useState(undefined);
    const [nightMode, setNightMode] = useState(false);

    useEffect(() => {
        if (!metadata.loaded) return;
        const theme = getStoredTheme(metadata.defaultTheme);
        const invalidTheme = !metadata.availableThemes.includes(theme);
        setSelectedColor(invalidTheme ? 'default' : theme);
    }, [metadata, setSelectedColor]);

    const sharedState = useMemo(() => {
        return {
            selectedColor, setSelectedColor,
            colorChooserOpen, setColorChooserOpen,
            nightMode, setNightMode
        }
    }, [selectedColor, setSelectedColor, colorChooserOpen, setColorChooserOpen]);
    return (<ThemeContext.Provider value={sharedState}>
            {children}
        </ThemeContext.Provider>
    )
}

const chartTheming = getChartTheming();

export const useTheme = () => {
    const {
        selectedColor,
        setSelectedColor,
        colorChooserOpen,
        setColorChooserOpen,
        nightMode,
        setNightMode
    } = useContext(ThemeContext);

    const setTheme = color => {
        setStoredTheme(color);
        setSelectedColor(color);
    }

    const toggleColorChooser = () => {
        setColorChooserOpen(!colorChooserOpen);
    }

    const isNightModeEnabled = () => {
        return nightMode;
    }

    const toggleNightMode = () => {
        setNightMode(!nightMode);
    }

    const nightModeEnabled = isNightModeEnabled();
    return {
        loaded: selectedColor !== undefined,
        currentTheme: selectedColor,
        color: selectedColor,
        setColor: setTheme,
        nightModeEnabled,
        colorChooserOpen,
        toggleNightMode,
        toggleColorChooser,
        graphTheming: chartTheming
    };
}