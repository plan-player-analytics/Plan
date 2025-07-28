import {createContext, useContext, useMemo, useState} from "react";
import {useTheme} from "../themeHook.jsx";
import theme from "../../default.json";

const ThemeStorageContext = createContext({});

export const ThemeStorageContextProvider = ({children}) => {
    const {currentTheme} = useTheme();
    const [name, setName] = useState('Default');
    const [currentColors, setCurrentColors] = useState(theme.colors);
    const [currentNightColors, setCurrentNightColors] = useState(theme.nightColors);
    const [currentUseCases, setCurrentUseCases] = useState(theme.useCases);
    const [currentNightModeUseCases, setCurrentNightModeUseCases] = useState(theme.nightModeUseCases);

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