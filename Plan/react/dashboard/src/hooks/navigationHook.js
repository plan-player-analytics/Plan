import {createContext, useCallback, useContext, useState} from "react";

const NavigationContext = createContext({});

export const NavigationContextProvider = ({children}) => {
    const [currentTab, setCurrentTab] = useState(undefined);
    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [updating, setUpdating] = useState(false);
    const [lastUpdate, setLastUpdate] = useState({date: 0, formatted: ""});

    const [sidebarExpanded, setSidebarExpanded] = useState(window.innerWidth > 1350);

    const requestUpdate = useCallback(() => {
        if (!updating) {
            setUpdateRequested(Date.now());
            setUpdating(true);
        }
    }, [updating, setUpdateRequested, setUpdating]);

    const finishUpdate = useCallback((date, formatted) => {
        setLastUpdate({date, formatted});
        setUpdating(false);
    }, [setLastUpdate, setUpdating]);

    const toggleSidebar = useCallback(() => {
        setSidebarExpanded(!sidebarExpanded);
    }, [setSidebarExpanded, sidebarExpanded])

    const sharedState = {
        currentTab, setCurrentTab,
        lastUpdate, updateRequested, updating, requestUpdate, finishUpdate,
        sidebarExpanded, setSidebarExpanded, toggleSidebar
    }
    return (<NavigationContext.Provider value={sharedState}>
            {children}
        </NavigationContext.Provider>
    )
}

export const useNavigation = () => {
    return useContext(NavigationContext);
}