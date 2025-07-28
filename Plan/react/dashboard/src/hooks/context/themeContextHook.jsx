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
    const [currentPieColors, setCurrentPieColors] = useState(theme.pieColors);
    const [currentDrilldownColors, setCurrentDrilldownColors] = useState(theme.pieDrilldownColors);
    const [currentThemeColorOptions, setCurrentThemeColorOptions] = useState(theme.themeColorOptions);

    const sharedState = useMemo(() => {
        return {
            name, setName, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases,
            currentPieColors, setCurrentPieColors,
            currentDrilldownColors, setCurrentDrilldownColors,
            currentThemeColorOptions, setCurrentThemeColorOptions
        }
    }, [name, currentColors, currentNightColors, currentUseCases, currentNightModeUseCases,
        currentPieColors, setCurrentPieColors,
        currentDrilldownColors, setCurrentDrilldownColors,
        currentThemeColorOptions, setCurrentThemeColorOptions]);
    return (<ThemeStorageContext.Provider value={sharedState}>
            {children}
        </ThemeStorageContext.Provider>
    )
}

export const useThemeStorage = () => {
    return useContext(ThemeStorageContext);
}