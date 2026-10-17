import {createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useAuth} from "../authenticationHook";
import {fetchPreferences, savePreferences} from "../../service/metadataService.js";
import {Preferences} from "../model/Preferences";

type ContextProps = {
    getKeyedPreference: (key: keyof Preferences) => Preferences[keyof Preferences] | undefined;
    setSomePreferences: (somePreferences: Partial<Preferences>) => void;
    defaultPreferences: Partial<Preferences>;
    preferencesLoaded: boolean;
} & Partial<Preferences>

const PreferencesContext = createContext<ContextProps | undefined>(undefined);

export const PreferencesContextProvider = ({children}: PropsWithChildren) => {
    const [preferences, setPreferences] = useState<Partial<Preferences>>({});
    const [defaultPreferences, setDefaultPreferences] = useState<Partial<Preferences>>({});
    const {authRequired, authLoaded, loggedIn} = useAuth();

    const updatePreferences = useCallback(async () => {
        const {data: {defaultPreferences, preferences}} = await fetchPreferences();
        setDefaultPreferences(defaultPreferences);
        if (authRequired && authLoaded && loggedIn) {
            // Preferences are only available if logged in.
            // Use defaultPreferences when one is not specified.
            setPreferences({...defaultPreferences, ...preferences});
        } else {
            const item = localStorage.getItem("preferences");
            if (item) {
                let userPref = JSON.parse(item);
                setPreferences({...defaultPreferences, ...userPref});
            } else {
                setPreferences({...defaultPreferences});
            }
        }
    }, [authRequired, authLoaded, loggedIn, setPreferences, setDefaultPreferences]);

    const storePreferences = useCallback(async (userPref: Partial<Preferences>) => {
        const withDefaultsRemoved = userPref;
        for (const key of Object.keys(defaultPreferences) as (keyof Preferences)[]) {
            if (defaultPreferences[key] === withDefaultsRemoved[key]) delete withDefaultsRemoved[key]
        }

        if (authRequired && authLoaded && loggedIn) {
            await savePreferences(withDefaultsRemoved);
        } else {
            localStorage.setItem("preferences", JSON.stringify(withDefaultsRemoved));
        }
        setPreferences({...defaultPreferences, ...withDefaultsRemoved});
    }, [defaultPreferences, authRequired, authLoaded, loggedIn, setPreferences]);

    const setSomePreferences = useCallback((userPref: Partial<Preferences>) => {
        storePreferences({...preferences, ...userPref});
    }, [storePreferences, preferences]);

    const getKeyedPreference = useCallback((key: keyof Preferences) => {
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
    const context = useContext(PreferencesContext);
    if (!context) throw new Error('usePreferences must be used within the PrefrencesContextProvider');
    return context;
}