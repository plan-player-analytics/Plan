import {createContext, useCallback, useContext, useEffect, useState} from "react";
import {fetchWhoAmI} from "../service/authenticationService";

const AuthenticationContext = createContext({});

export const AuthenticationContextProvider = ({children}) => {
    const [authLoaded, setAuthLoaded] = useState(false)
    const [authRequired, setAuthRequired] = useState(false);
    const [loggedIn, setLoggedIn] = useState(false);
    const [user, setUser] = useState(undefined);

    const updateLoginDetails = useCallback(async () => {
        const whoAmI = await fetchWhoAmI();
        setAuthRequired(whoAmI.authRequired);
        setLoggedIn(whoAmI.loggedIn);
        if (whoAmI.loggedIn) setUser(loggedIn.user);
        setAuthLoaded(true)
    }, [loggedIn])

    const login = async (username, password) => {
        // TODO implement later when login page is done with React
        await updateLoginDetails();
    }

    const logout = () => {
        // TODO implement later when login page is done with React
    }

    useEffect(() => {
        updateLoginDetails();
    }, [updateLoginDetails]);

    const sharedState = {authLoaded, authRequired, loggedIn, user, login, logout}
    return (<AuthenticationContext.Provider value={sharedState}>
            {children}
        </AuthenticationContext.Provider>
    )
}

export const useAuth = () => {
    return useContext(AuthenticationContext);
}