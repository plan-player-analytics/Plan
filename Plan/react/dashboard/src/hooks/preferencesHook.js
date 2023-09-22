import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useAuth} from "./authenticationHook";

const PreferencesContext = createContext({});

export const PreferencesContextProvider = ({children}) => {
    const [preferences, setPreferences] = useState({});
    const {authRequired, authLoaded, loggedIn} = useAuth();

    const updatePreferences = useCallback(async () => {
        // TODO load from backend (Includes default preferences alongside regular if logged in)
        if (authRequired && (!authLoaded || !loggedIn)) {
            setPreferences({});
        } else {
            let userPref = localStorage.getItem("preferences");
            if (!userPref) userPref = {};
            setPreferences(userPref);
        }
    }, [authRequired, authLoaded, loggedIn, setPreferences]);

    useEffect(() => {
        updatePreferences();
    }, [updatePreferences, authLoaded, loggedIn]);

    const sharedState = useMemo(() => {
            return {...preferences}
        },
        [preferences]);
    return (<PreferencesContext.Provider value={sharedState}>
            {children}
        </PreferencesContext.Provider>
    )
}

export const usePreferences = () => {
    return useContext(PreferencesContext);
}