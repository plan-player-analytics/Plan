import {createContext, useCallback, useContext, useEffect, useState} from "react";
import {fetchWhoAmI} from "../service/authenticationService";

const AuthenticationContext = createContext({});

export const AuthenticationContextProvider = ({children}) => {
    const [authRequired, setAuthRequired] = useState(false);
    const [loggedIn, setLoggedIn] = useState(false);
    const [user, setUser] = useState(undefined);

    const updateLoginDetails = useCallback(async () => {
        const whoami = await fetchWhoAmI();
        setAuthRequired(whoami.authRequired);
        setLoggedIn(whoami.loggedIn);
        if (whoami.loggedIn) setUser(loggedIn.user);
    }, [loggedIn])

    const login = (username, password) => {
        // TODO implement later when login page is done with React
        updateLoginDetails();
    }

    const logout = () => {
        // TODO implement later when login page is done with React
    }

    useEffect(() => {
        updateLoginDetails();
    }, [updateLoginDetails]);

    const sharedState = {authRequired, loggedIn, user, login, logout}
    return (<AuthenticationContext.Provider value={sharedState}>
            {children}
        </AuthenticationContext.Provider>
    )
}

export const useAuth = () => {
    return useContext(AuthenticationContext);
}