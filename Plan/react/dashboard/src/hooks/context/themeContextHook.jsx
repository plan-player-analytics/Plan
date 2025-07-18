import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {useTheme} from "../themeHook.jsx";
import useCases from "../../useCases.json";
import nightModeUseCases from "../../nightModeUseCases.json";
import theme from "../../theme.json";

const ThemeStorageContext = createContext({});

export const ThemeStorageContextProvider = ({children}) => {
    const {currentTheme} = useTheme();
    const [name, setName] = useState('Default');
    const [currentColors, setCurrentColors] = useState(theme.colors);
    const [currentNightColors, setCurrentNightColors] = useState(theme.nightColors);
    const [currentUseCases, setCurrentUseCases] = useState(useCases);
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState(nightModeUseCases);

    useEffect(() => {
        setCurrentColors(theme.colors)
    }, [theme.colors]);
    useEffect(() => {
        setCurrentUseCases(useCases);
    }, [useCases]);
    useEffect(() => {
        setCurrentNightModeUseCases(nightModeUseCases);
    }, [nightModeUseCases]);

    const sharedState = useMemo(() => {
        return {
            name, setName, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases
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