import {createContext, useCallback, useContext, useMemo, useState} from "react";

const NavigationContext = createContext({});

export const NavigationContextProvider = ({children}) => {
    const [currentTab, setCurrentTab] = useState(undefined);
    const [updateRequested, setUpdateRequested] = useState(Date.now());
    const [updating, setUpdating] = useState({});
    const [lastUpdate, setLastUpdate] = useState({});

    const [items, setItems] = useState([]);
    const [sidebarExpanded, setSidebarExpanded] = useState(window.innerWidth > 1350);
    const [helpModalTopic, setHelpModalTopic] = useState(undefined);

    const setSidebarItems = useCallback((items) => {
        const pathname = window.location.href;
        setItems(items);
        for (const item of items) {
            if (!item) continue;
            if ('/' !== item.href && pathname.includes(item.href)) setCurrentTab(item.name);
            if (item.contents) {
                for (const subItem of item.contents) {
                    if (!subItem) continue;
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

    // TODO currently not possible due to extensionData getting updated off-tab
    // useEffect(requestUpdate, [currentTab]); // Force data to update when changing tab

    const finishUpdate = useCallback((date, formatted, isStillUpdating) => {
        if (date) {
            if (!lastUpdate.date || date > lastUpdate.date) {
                setLastUpdate({date, formatted});
            }
            setUpdating(isStillUpdating);
        }
    }, [setLastUpdate, setUpdating, lastUpdate]);

    const toggleSidebar = useCallback(() => {
        setSidebarExpanded(!sidebarExpanded);
    }, [setSidebarExpanded, sidebarExpanded])

    const sharedState = useMemo(() => {
        return {
            currentTab, setCurrentTab,
            lastUpdate, updateRequested, updating, requestUpdate, finishUpdate,
            sidebarExpanded, setSidebarExpanded, toggleSidebar, sidebarItems: items, setSidebarItems,
            helpModalTopic, setHelpModalTopic
        }
    }, [
        currentTab, setCurrentTab,
        lastUpdate, updateRequested, updating, requestUpdate, finishUpdate,
        sidebarExpanded, setSidebarExpanded, toggleSidebar, items, setSidebarItems,
        helpModalTopic, setHelpModalTopic
    ]);
    return (<NavigationContext.Provider value={sharedState}>
            {children}
        </NavigationContext.Provider>
    )
}

export const useNavigation = () => {
    return useContext(NavigationContext);
}