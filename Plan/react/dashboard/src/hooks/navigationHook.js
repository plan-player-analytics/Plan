import {createContext, useContext, useState} from "react";

const NavigationContext = createContext({});

export const NavigationContextProvider = ({children}) => {
    const [currentTab, setCurrentTab] = useState(undefined);
    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [updating, setUpdating] = useState(false);
    const [lastUpdate, setLastUpdate] = useState({date: 0, formatted: ""});

    const requestUpdate = () => {
        if (!updating) {
            setUpdateRequested(Date.now());
            setUpdating(true);
        }
    }

    const finishUpdate = (date, formatted) => {
        setLastUpdate({date, formatted});
        setUpdating(false);
    }

    const sharedState = {
        currentTab, setCurrentTab,
        lastUpdate, updateRequested, updating, requestUpdate, finishUpdate
    }
    return (<NavigationContext.Provider value={sharedState}>
            {children}
        </NavigationContext.Provider>
    )
}

export const useNavigation = () => {
    return useContext(NavigationContext);
}