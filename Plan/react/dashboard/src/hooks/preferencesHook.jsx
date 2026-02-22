import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useAuth} from "./authenticationHook.tsx";
import {fetchPreferences} from "../service/metadataService";

const PreferencesContext = createContext({});

export const PreferencesContextProvider = ({children}) => {
    const [preferences, setPreferences] = useState({});
    const [defaultPreferences, setDefaultPreferences] = useState({});
    const {authRequired, authLoaded, loggedIn} = useAuth();

    const updatePreferences = useCallback(async () => {
        const {data: {defaultPreferences, preferences}} = await fetchPreferences();
        setDefaultPreferences(defaultPreferences);
        if (authRequired && authLoaded && loggedIn) {
            // Preferences are only available if logged in.
            // Use defaultPreferences when one is not specified.
            setPreferences({...defaultPreferences, ...(preferences || {})});
        } else {
            let userPref = JSON.parse(localStorage.getItem("preferences"));
            setPreferences({...defaultPreferences, ...userPref});
        }
    }, [authRequired, authLoaded, loggedIn, setPreferences, setDefaultPreferences]);

    const storePreferences = useCallback(async userPref => {
        const withDefaultsRemoved = userPref;
        for (const key of Object.keys(defaultPreferences)) {
            if (defaultPreferences[key] === withDefaultsRemoved[key]) delete withDefaultsRemoved[key]
        }

        if (authRequired && authLoaded && loggedIn) {
            await storePreferences(withDefaultsRemoved);
        } else {
            localStorage.setItem("preferences", JSON.stringify(withDefaultsRemoved));
        }
        setPreferences({...defaultPreferences, ...(withDefaultsRemoved || {})});
    }, [defaultPreferences, authRequired, authLoaded, loggedIn, setPreferences]);

    const setSomePreferences = useCallback(userPref => {
        storePreferences({...preferences, ...userPref});
    }, [storePreferences, preferences]);
    const getKeyedPreference = useCallback(key => {
        return preferences[key];
    }, [preferences]);

    useEffect(() => {
        updatePreferences();
    }, [updatePreferences, authLoaded, loggedIn]);

    const sharedState = useMemo(() => {
            return {
                ...preferences,
                getKeyedPreference,
                setSomePreferences,
                defaultPreferences,
                preferencesLoaded: Object.keys(defaultPreferences || {}).length > 0
            }
        },
        [preferences, defaultPreferences, storePreferences, getKeyedPreference, setSomePreferences]);
    return (<PreferencesContext.Provider value={sharedState}>
            {children}
        </PreferencesContext.Provider>
    )
}

export const usePreferences = () => {
    return useContext(PreferencesContext);
}