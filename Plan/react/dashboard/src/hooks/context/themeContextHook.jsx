import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useTheme} from "../themeHook.jsx";
import {fetchTheme} from "../../service/metadataService.js";

const ThemeStorageContext = createContext({});

export const ThemeStorageContextProvider = ({children}) => {
    const theme = useTheme();
    const {currentTheme, currentColor} = theme;
    const [loaded, setLoaded] = useState(false);
    const [currentColors, setCurrentColors] = useState({});
    const [currentNightColors, setCurrentNightColors] = useState({});
    const [currentUseCases, setCurrentUseCases] = useState({});
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState({});

    const loadTheme = useCallback(async (name) => {
        const response = await fetchTheme(name);
        if (response.error) {
            console.error(response.error);
            return;
        }
        const theme = response.data;
        setCurrentColors(theme.colors);
        setCurrentNightColors(theme.nightColors);
        setCurrentUseCases(theme.useCases);
        setCurrentNightModeUseCases(theme.nightModeUseCases);
        setLoaded(true);
    }, [])

    useEffect(() => {
        if (theme.loaded && currentTheme) {
            loadTheme(currentTheme);
        }
    }, [theme.loaded, currentTheme]);

    const sharedState = useMemo(() => {
        return {
            loaded, name: currentTheme, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases
        }
    }, [name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases]);
    return (<ThemeStorageContext.Provider value={sharedState}>
            {children}
        </ThemeStorageContext.Provider>
    )
}

export const useThemeStorage = () => {
    return useContext(ThemeStorageContext);
}