import {createContext, useCallback, useContext, useState} from "react";

const NavigationContext = createContext({});

export const NavigationContextProvider = ({children}) => {
    const [currentTab, setCurrentTab] = useState(undefined);
    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [updating, setUpdating] = useState(false);
    const [lastUpdate, setLastUpdate] = useState({date: 0, formatted: ""});

    const [items, setItems] = useState([]);
    const [sidebarExpanded, setSidebarExpanded] = useState(window.innerWidth > 1350);

    const setSidebarItems = useCallback((items) => {
        const pathname = window.location.href;
        setItems(items);
        for (const item of items) {
            if ('/' !== item.href && pathname.includes(item.href)) setCurrentTab(item.name);
            if (item.contents) {
                for (const subItem of item.contents) {
                    if ('/' !== subItem.href && pathname.includes(subItem.href)) setCurrentTab(subItem.name);
                }
            }
        }
    }, [setItems]);

    const requestUpdate = useCallback(() => {
        if (!updating) {
            setUpdateRequested(Date.now());
            setUpdating(true);
        }
    }, [updating, setUpdateRequested, setUpdating]);

    const finishUpdate = useCallback((date, formatted) => {
        // TODO Logic to retry if received data is too old
        if (date) {
            setLastUpdate({date, formatted});
            setUpdating(false);
        }
    }, [setLastUpdate, setUpdating]);

    const toggleSidebar = useCallback(() => {
        setSidebarExpanded(!sidebarExpanded);
    }, [setSidebarExpanded, sidebarExpanded])

    const sharedState = {
        currentTab, setCurrentTab,
        lastUpdate, updateRequested, updating, requestUpdate, finishUpdate,
        sidebarExpanded, setSidebarExpanded, toggleSidebar, sidebarItems: items, setSidebarItems
    }
    return (<NavigationContext.Provider value={sharedState}>
            {children}
        </NavigationContext.Provider>
    )
}

export const useNavigation = () => {
    return useContext(NavigationContext);
}