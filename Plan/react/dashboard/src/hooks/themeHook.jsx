import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {getChartTheming} from "../util/graphColors";
import {useMetadata} from "./metadataHook";

const getStoredTheme = (defaultTheme) => {
    const stored = window.localStorage.getItem('theme.name');
    return stored && stored !== 'undefined' ? stored : defaultTheme;
}

const setStoredTheme = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('theme.name', themeColor);
    }
}

const getStoredColor = () => {
    const stored = window.localStorage.getItem('theme.color');
    return stored && stored !== 'undefined' ? stored : 'theme';
}

const setStoredColor = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('theme.color', themeColor);
    }
}

const getStoredNightMode = () => {
    const stored = window.localStorage.getItem('theme.nightMode');
    return stored && stored !== 'undefined' ? stored !== 'false' : false;
}

const setStoredNightMode = value => {
    window.localStorage.setItem('theme.nightMode', '' + value);
}

const removeOldVariables = () => {
    window.localStorage.removeItem('themeColor');
}

const ThemeContext = createContext({});

export const ThemeContextProvider = ({children, themeOverride}) => {
    const metadata = useMetadata();

    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedTheme, setSelectedTheme] = useState(themeOverride);
    const [selectedColor, setSelectedColor] = useState(undefined);
    const [nightMode, setNightMode] = useState(getStoredNightMode());

    removeOldVariables();

    useEffect(() => {
        if (!metadata.loaded) return;
        if (themeOverride) return;
        const theme = getStoredTheme(metadata.defaultTheme);
        const invalidTheme = !metadata.getAvailableThemes().includes(theme);
        setSelectedTheme(invalidTheme ? 'default' : theme);
        setSelectedColor(getStoredColor());
    }, [metadata.loaded, metadata, setSelectedTheme, setSelectedColor]);

    const sharedState = useMemo(() => {
        return {
            selectedTheme, setSelectedTheme,
            selectedColor, setSelectedColor,
            colorChooserOpen, setColorChooserOpen,
            nightMode, setNightMode
        }
    }, [selectedTheme, selectedColor, nightMode, setSelectedColor, colorChooserOpen, setColorChooserOpen]);
    return (<ThemeContext.Provider value={sharedState}>
            {children}
        </ThemeContext.Provider>
    )
}

const chartTheming = getChartTheming();

export const useTheme = () => {
    const {
        selectedTheme,
        setSelectedTheme,
        selectedColor,
        setSelectedColor,
        colorChooserOpen,
        setColorChooserOpen,
        nightMode,
        setNightMode
    } = useContext(ThemeContext);

    const setTheme = color => {
        setStoredTheme(color);
        setSelectedTheme(color);
    }

    const setColor = color => {
        setStoredColor(color);
        setSelectedColor(color);
    }

    const toggleColorChooser = () => {
        setColorChooserOpen(!colorChooserOpen);
    }

    const isNightModeEnabled = () => {
        return nightMode;
    }

    const toggleNightMode = () => {
        setStoredNightMode(!nightMode);
        setNightMode(!nightMode);
    }

    const nightModeEnabled = isNightModeEnabled();
    return {
        loaded: selectedTheme !== undefined,
        currentTheme: selectedTheme,
        color: selectedColor,
        setTheme,
        setColor,
        nightModeEnabled,
        colorChooserOpen,
        toggleNightMode,
        toggleColorChooser,
        graphTheming: chartTheming
    };
}
export const getLocallyStoredThemes = () => {
    return JSON.parse(window.localStorage.getItem('locally-stored-themes') || '[]');
}