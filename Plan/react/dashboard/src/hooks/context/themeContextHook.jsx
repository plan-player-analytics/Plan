import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useTheme} from "../themeHook.jsx";
import {fetchTheme} from "../../service/metadataService.js";

const ThemeStorageContext = createContext({});

export const getLocallyStoredThemes = () => {
    return JSON.parse(window.localStorage.getItem('locally-stored-themes') || '[]');
}

export const ThemeStorageContextProvider = ({children}) => {
    const theme = useTheme();
    const {currentTheme, color} = theme;
    const [loaded, setLoaded] = useState(false);
    const [currentColors, setCurrentColors] = useState({});
    const [currentNightColors, setCurrentNightColors] = useState({});
    const [currentUseCases, setCurrentUseCases] = useState({});
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState({});

    const loadTheme = useCallback(async (name) => {
        let theme;
        if (getLocallyStoredThemes().includes(name)) {
            const found = window.localStorage.getItem(`locally-stored-theme-${name}`);
            if (found) theme = JSON.parse(found); // TODO catch json parse error
        }

        if (!theme) {
            const response = await fetchTheme(name);
            if (response.error) {
                console.error(response.error);
                return;
            }
            theme = response.data;
        }
        setCurrentColors(theme.colors);
        setCurrentNightColors(theme.nightColors);
        setCurrentUseCases(theme.useCases);
        setCurrentNightModeUseCases(theme.nightModeUseCases);
        setLoaded(true);
    }, []);

    const cloneThemeLocally = async (themeToClone, nameAs) => {
        const response = await fetchTheme(themeToClone);
        if (response.error) {
            console.error(response.error);
            return; // TODO alert context
        }
        const theme = response.data;
        const locallyStoredThemes = getLocallyStoredThemes();
        window.localStorage.setItem(`locally-stored-theme-${nameAs}`, JSON.stringify(theme));
        locallyStoredThemes.push(nameAs);
        window.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
    }

    const saveUploadedThemeLocally = (name, themeJson) => {
        const locallyStoredThemes = getLocallyStoredThemes();
        window.localStorage.setItem(`locally-stored-theme-${name}`, JSON.stringify(themeJson));
        locallyStoredThemes.push(name);
        window.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
    }

    useEffect(() => {
        if (theme.loaded && currentTheme) {
            loadTheme(currentTheme);
        }
    }, [theme.loaded, currentTheme]);

    const sharedState = useMemo(() => {
        return {
            loaded, name: currentTheme, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases,
            cloneThemeLocally, saveUploadedThemeLocally
        }
    }, [name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases, loaded]);
    return (<ThemeStorageContext.Provider value={sharedState}>
            {children}
        </ThemeStorageContext.Provider>
    )
}

export const useThemeStorage = () => {
    return useContext(ThemeStorageContext);
}