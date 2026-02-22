import React, {createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {getLocallyStoredThemes, useTheme} from "../themeHook.jsx";
import {fetchTheme} from "../../service/metadataService.js";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {useAlertPopupContext} from "./alertPopupContext.js";
import {Trans} from "react-i18next";
import {mergeUseCases} from "../../util/mutator.js";
import {ThemeContextValue} from "../../components/theme/model/ThemeContextValue";
import {ColorPropertyMap, ThemeConfig, UseCaseMap} from "../../components/theme/model/ThemeConfig";
import {PlanResponse} from "../../service/PlanResponse";

const ThemeStorageContext = createContext<ThemeContextValue | undefined>(undefined);

type ThemeCache = {
    [key: string]: ThemeConfig | undefined;
}

// Reduce refetching theme inside the theme editor to avoid rate-limit issues.
const themeCache: ThemeCache = {};

type Props = {
    loadMissing?: boolean
} & PropsWithChildren;

export const ThemeStorageContextProvider = ({children, loadMissing = false}: Props) => {
    const theme = useTheme();
    const {currentTheme, color} = theme;
    const {addAlert} = useAlertPopupContext();
    const [loaded, setLoaded] = useState(false);
    const [currentColors, setCurrentColors] = useState<ColorPropertyMap>({});
    const [currentNightColors, setCurrentNightColors] = useState<ColorPropertyMap | {}>({});
    const [currentUseCases, setCurrentUseCases] = useState<UseCaseMap | {}>({});
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState<UseCaseMap | {}>({});
    const [error, setError] = useState(undefined);

    const loadMissingDefaults = async (original: ThemeConfig) => {
        let theme
        if (themeCache['defaultInJar']) {
            theme = themeCache['defaultInJar']
        } else {
            const response: PlanResponse<ThemeConfig> = await fetchTheme('default', true);
            if (response.error) {
                console.error(response.error);
                return;
            }
            theme = response.data;
            themeCache['defaultInJar'] = theme;
        }

        return {
            ...original,
            useCases: mergeUseCases(theme.useCases, original.useCases)
        };
    }

    const loadTheme = useCallback(async (name: string) => {
        setError(undefined);
        let theme;
        if (getLocallyStoredThemes().includes(name)) {
            const found = globalThis.localStorage.getItem(`locally-stored-theme-${name}`);
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
        if (loadMissing) {
            theme = await loadMissingDefaults(theme);
        }
        setCurrentColors(theme.colors);
        setCurrentNightColors(theme.nightColors);
        setCurrentUseCases(theme.useCases);
        setCurrentNightModeUseCases(theme.nightModeUseCases);
        setLoaded(true);
    }, []);

    const saveUploadedThemeLocally = (name: string, themeJson: ThemeConfig, originalName?: string) => {
        const locallyStoredThemes = getLocallyStoredThemes();
        globalThis.localStorage.setItem(`locally-stored-theme-${name}`, JSON.stringify(themeJson));
        if (!locallyStoredThemes.includes(name)) {
            locallyStoredThemes.push(name);
        }
        globalThis.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
        if (name !== originalName) {
            deleteThemeLocally(originalName);
        }
    }

    const deleteThemeLocally = (name?: string) => {
        if (!name) return;
        const locallyStoredThemes = getLocallyStoredThemes();
        globalThis.localStorage.removeItem(`locally-stored-theme-${name}`);
        const index = locallyStoredThemes.indexOf(name);
        if (index > -1) {
            locallyStoredThemes.splice(index, 1);
        }
        globalThis.localStorage.setItem(`locally-stored-themes`, JSON.stringify(locallyStoredThemes));
    }

    const cloneThemeLocally = async (themeToClone: string, name: string) => {
        let theme;
        if (getLocallyStoredThemes().includes(name)) {
            const found = globalThis.localStorage.getItem(`locally-stored-theme-${name}`);
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
                        <Trans i18nKey={"html.label.themeEditor.failedToClone"}
                               values={{error: response.error?.message}}/>
                    </>
                });
                return false;
            }
            theme = response.data;
            themeCache[name] = theme;
        }
        saveUploadedThemeLocally(name, theme);
        return true;
    }

    const reloadTheme = () => {
        delete themeCache[currentTheme];
        loadTheme(currentTheme);
    }

    useEffect(() => {
        if (theme.loaded && currentTheme) {
            loadTheme(currentTheme);
        }
    }, [theme.loaded, currentTheme]);

    const sharedState = useMemo(() => {
        const selectedUseCases: UseCaseMap = theme.nightModeEnabled ? currentNightModeUseCases : currentUseCases;
        const themeOptions = selectedUseCases?.themeColorOptions || [];
        const colorExistsAsOption = themeOptions.includes(color);
        return {
            loaded, error,
            name: currentTheme,
            color: colorExistsAsOption ? color : undefined,
            currentColors,
            currentNightColors,
            currentUseCases,
            currentNightModeUseCases,
            usedColors: theme.nightModeEnabled ? {...currentColors, ...currentNightColors} : currentColors,
            usedUseCases: theme.nightModeEnabled ? mergeUseCases(currentUseCases, currentNightModeUseCases) : currentUseCases,
            cloneThemeLocally, saveUploadedThemeLocally, deleteThemeLocally, reloadTheme
        }
    }, [currentTheme, color, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases, loaded, error, theme.nightModeEnabled]);
    return (<ThemeStorageContext.Provider value={sharedState}>
            {children}
        </ThemeStorageContext.Provider>
    )
}

export const useThemeStorage = () => {
    const context = useContext(ThemeStorageContext);
    if (!context) throw new Error("useThemeStorage must be used within ThemeStorageContextProvider");
    return context;
}