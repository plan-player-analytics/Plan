import {createContext, PropsWithChildren, useContext, useEffect, useMemo, useState} from "react";
import {getChartTheming} from "../util/graphColors";
import {useMetadata} from "./metadataHook";

const getStoredTheme = (defaultTheme: string) => {
    const stored = globalThis.localStorage.getItem('theme.name');
    return stored && stored !== 'undefined' ? stored : defaultTheme;
}

const setStoredTheme = (themeColor?: string) => {
    if (themeColor) {
        globalThis.localStorage.setItem('theme.name', themeColor);
    }
}

const getStoredColor = () => {
    const stored = globalThis.localStorage.getItem('theme.color');
    return stored && stored !== 'undefined' ? stored : 'theme';
}

const setStoredColor = (themeColor?: string) => {
    if (themeColor) {
        globalThis.localStorage.setItem('theme.color', themeColor);
    }
}

const getStoredNightMode = () => {
    const stored = globalThis.localStorage.getItem('theme.nightMode');
    return stored && stored !== 'undefined' ? stored !== 'false' : undefined;
}

const setStoredNightMode = (value: boolean) => {
    globalThis.localStorage.setItem('theme.nightMode', '' + value);
}

const removeOldVariables = () => {
    globalThis.localStorage.removeItem('themeColor');
}

type ThemeContextValue = {
    selectedTheme: string;
    setSelectedTheme: (selectedTheme: string) => void;
    selectedColor: string;
    setSelectedColor: (selectedColor: string) => void;
    colorChooserOpen: boolean;
    setColorChooserOpen: (open: boolean) => void;
    nightMode: boolean | undefined;
    setNightMode: (value: boolean) => void;
}

type ProviderProps = {
    themeOverride?: string;
} & PropsWithChildren

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined);

export const ThemeContextProvider = ({children, themeOverride = undefined}: ProviderProps) => {
    const metadata = useMetadata();

    const [colorChooserOpen, setColorChooserOpen] = useState(false);
    const [selectedTheme, setSelectedTheme] = useState<string>(themeOverride || 'default');
    const [selectedColor, setSelectedColor] = useState<string>('undefined');
    const [nightMode, setNightMode] = useState(getStoredNightMode());

    removeOldVariables();

    useEffect(() => {
        if (!metadata.loaded) return;
        if (themeOverride) return;
        const theme = getStoredTheme(metadata.defaultTheme);
        const invalidTheme = !metadata.getAvailableThemes().includes(theme);
        if (nightMode === undefined) setNightMode(metadata.defaultNightMode);
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
    const context = useContext(ThemeContext);
    if (!context) throw new Error('useTheme must be called inside ThemeContextProvider');

    return useMemo(() => {
        const {
            selectedTheme,
            setSelectedTheme,
            selectedColor,
            setSelectedColor,
            colorChooserOpen,
            setColorChooserOpen,
            nightMode,
            setNightMode
        } = context;

        const setTheme = (color: string) => {
            setStoredTheme(color);
            setSelectedTheme(color);
        }

        const setColor = (color: string) => {
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
    }, [context]);
}
export const getLocallyStoredThemes = () => {
    return JSON.parse(globalThis.localStorage.getItem('locally-stored-themes') || '[]');
}