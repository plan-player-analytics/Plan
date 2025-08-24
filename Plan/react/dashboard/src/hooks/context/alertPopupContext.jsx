import {createContext, useCallback, useContext, useMemo, useState} from "react";

const AlertPopupContext = createContext({});

export const AlertPopupContextProvider = ({children}) => {
    const [alerts, setAlerts] = useState([]);

    const dismissAlert = useCallback(alert => {
        setAlerts(alerts.filter(a => a.time - alert.time < 1));
    }, [alerts, setAlerts]);

    const addAlert = useCallback(alert => {
        const time = Date.now() + Math.random();
        const addedAlert = {time, ...alert};
        setAlerts([...alerts, addedAlert]);
        setTimeout(() => {
            dismissAlert(addedAlert);
        }, alert.timeout || 5000)
    }, [alerts, setAlerts, dismissAlert]);

    const sharedState = useMemo(() => {
        return {alerts, addAlert, dismissAlert};
    }, [alerts, addAlert, dismissAlert]);
    return (<AlertPopupContext.Provider value={sharedState}>
            {children}
        </AlertPopupContext.Provider>
    )
}

export const useAlertPopupContext = () => {
    return useContext(AlertPopupContext);
}