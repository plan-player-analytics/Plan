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

const getStoredColor = () => {
    const stored = window.localStorage.getItem('themeColor');
    return stored && stored !== 'undefined' ? stored : 'theme';
}

const setStoredColor = themeColor => {
    if (themeColor) {
        window.localStorage.setItem('themeColor', themeColor);
    }
}

const getStoredNightMode = () => {
    const stored = window.localStorage.getItem('nightMode');
    return stored && stored !== 'undefined' ? stored === 'true' : false;
}

const setStoredNightMode = value => {
    if (value) {
        window.localStorage.setItem('nightMode', '' + value);
    }
}

const ThemeContext = createContext({});

export const ThemeContextProvider = ({children, themeOverride}) => {
    const metadata = useMetadata();

    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedTheme, setSelectedTheme] = useState(themeOverride);
    const [selectedColor, setSelectedColor] = useState(undefined);
    const [nightMode, setNightMode] = useState(getStoredNightMode());

    useEffect(() => {
        if (!metadata.loaded) return;
        if (themeOverride) return;
        const theme = getStoredTheme(metadata.defaultTheme);
        const invalidTheme = !metadata.availableThemes.includes(theme);
        setSelectedTheme(invalidTheme ? 'default' : theme);
        setSelectedColor(getStoredColor());
    }, [metadata, setSelectedTheme, setSelectedColor]);

    const sharedState = useMemo(() => {
        return {
            selectedTheme, setSelectedTheme,
            selectedColor, setSelectedColor,
            colorChooserOpen, setColorChooserOpen,
            nightMode, setNightMode: value => {
                setStoredNightMode(value);
                setNightMode(value);
            }
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