import {createContext, PropsWithChildren, ReactNode, useCallback, useContext, useMemo, useState} from "react";

export type Alert = {
    color: string;
    content: ReactNode;
    timeout: number;
}

export type AddedAlert = {
    time: number;
} & Alert;

type AlertPopupContextValue = {
    alerts: Alert[];
    addAlert: (alert: Alert) => void;
    dismissAlert: (alert: AddedAlert) => void;
}

const AlertPopupContext = createContext<AlertPopupContextValue | undefined>(undefined);

type Props = {} & PropsWithChildren

export const AlertPopupContextProvider = ({children}: Props) => {
    const [alerts, setAlerts] = useState<AddedAlert[]>([]);

    const dismissAlert = useCallback((alert: AddedAlert) => {
        setAlerts(alerts.filter(a => a.time - alert.time < 1));
    }, [alerts, setAlerts]);

    const addAlert = useCallback((alert: Alert) => {
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
    const context = useContext(AlertPopupContext);
    if (!context) throw new Error('useAlertPopupContext must be used within AlertPopupContextProvider');
    return context;
}