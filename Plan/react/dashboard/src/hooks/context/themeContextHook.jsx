import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useTheme} from "../themeHook.jsx";
import {fetchTheme} from "../../service/metadataService.js";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useAlertPopupContext} from "./alertPopupContext.jsx";
import {Trans} from "react-i18next";

const ThemeStorageContext = createContext({});

export const getLocallyStoredThemes = () => {
    return JSON.parse(window.localStorage.getItem('locally-stored-themes') || '[]');
}

// Reduce refetching theme inside the theme editor to avoid rate-limit issues.
const themeCache = {};

export const ThemeStorageContextProvider = ({children}) => {
    const theme = useTheme();
    const {currentTheme, color} = theme;
    const {addAlert} = useAlertPopupContext();
    const [loaded, setLoaded] = useState(false);
    const [currentColors, setCurrentColors] = useState({});
    const [currentNightColors, setCurrentNightColors] = useState({});
    const [currentUseCases, setCurrentUseCases] = useState({});
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState({});
    const [error, setError] = useState(null);

    const loadTheme = useCallback(async (name) => {
        setError(null);
        let theme;
        if (getLocallyStoredThemes().includes(name)) {
            const found = window.localStorage.getItem(`locally-stored-theme-${name}`);
            if (found) theme = JSON.parse(found); // TODO catch json parse error
        }

        if (!theme) {
            if (themeCache[name]) {
                theme = themeCache[name];
            } else {
                const response = await fetchTheme(name);
                if (response.error) {
                    console.error(response.error);
                    setError(response.error);
                    return;
                }
                theme = response.data;
                themeCache[name] = theme;
            }
        }
        setCurrentColors(theme.colors);
        setCurrentNightColors(theme.nightColors);
        setCurrentUseCases(theme.useCases);
        setCurrentNightModeUseCases(theme.nightModeUseCases);
        setLoaded(true);
    }, []);

    const saveUploadedThemeLocally = (name, themeJson, originalName) => {
        const locallyStoredThemes = getLocallyStoredThemes();
        window.localStorage.setItem(`locally-stored-theme-${name}`, JSON.stringify(themeJson));
        if (!locallyStoredThemes.includes(name)) {
            locallyStoredThemes.push(name);
        }
        window.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
        if (name !== originalName) {
            deleteThemeLocally(originalName);
        }
    }

    const deleteThemeLocally = (name) => {
        const locallyStoredThemes = getLocallyStoredThemes();
        window.localStorage.removeItem(`locally-stored-theme-${name}`);
        const index = locallyStoredThemes.indexOf(name);
        if (index > -1) {
            locallyStoredThemes.splice(index, 1);
        }
        window.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
    }

    const cloneThemeLocally = async (themeToClone, nameAs) => {
        let theme;
        if (getLocallyStoredThemes().includes(name)) {
            const found = window.localStorage.getItem(`locally-stored-theme-${name}`);
            if (found) theme = JSON.parse(found); // TODO catch json parse error
        }
        if (themeCache[name]) {
            theme = themeCache[name];
        } else {
            const response = await fetchTheme(themeToClone);
            if (response.error) {
                console.error(response.error);
                addAlert({
                    timeout: 15000,
                    color: "error",
                    content: <>
                        <Fa icon={faExclamationTriangle}/>
                        {" "}
                        <Trans i18nKey={"html.label.themeEditor.failedToClone"} values={{error: error?.message}}/>
                    </>
                });
                return false;
            }
            theme = response.data;
            themeCache[name] = theme;
        }
        saveUploadedThemeLocally(nameAs, theme);
        return true;
    }

    const reloadTheme = () => {
        delete themeCache[name];
        loadTheme(currentTheme);
    }

    useEffect(() => {
        if (theme.loaded && currentTheme) {
            loadTheme(currentTheme);
        }
    }, [theme.loaded, currentTheme]);

    const sharedState = useMemo(() => {
        const themeOptions = (theme.nightModeEnabled
            ? currentNightModeUseCases?.themeColorOptions
            : currentUseCases?.themeColorOptions) || [];
        const colorExistsAsOption = themeOptions.includes(color);
        return {
            loaded, error,
            name: currentTheme,
            color: colorExistsAsOption ? color : undefined,
            currentColors,
            currentNightColors,
            currentUseCases,
            currentNightModeUseCases,
            usedColors: theme.nightModeEnabled ? currentColors : {...currentColors, ...currentNightColors},
            usedUseCases: theme.nightModeEnabled ? currentUseCases : {...currentUseCases, ...currentNightModeUseCases},
            cloneThemeLocally, saveUploadedThemeLocally, deleteThemeLocally, reloadTheme
        }
    }, [name, color, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases, loaded, error, theme.nightModeEnabled]);
    return (<ThemeStorageContext.Provider value={sharedState}>
            {children}
        </ThemeStorageContext.Provider>
    )
}

export const useThemeStorage = () => {
    return useContext(ThemeStorageContext);
}