import {createContext, useCallback, useContext, useEffect, useState} from "react";
import {fetchWhoAmI} from "../service/authenticationService";

const AuthenticationContext = createContext({});

export const AuthenticationContextProvider = ({children}) => {
    const [loginError, setLoginError] = useState(undefined);

    const [authLoaded, setAuthLoaded] = useState(false)
    const [authRequired, setAuthRequired] = useState(false);
    const [loggedIn, setLoggedIn] = useState(false);
    const [user, setUser] = useState(undefined);

    const updateLoginDetails = useCallback(async () => {
        const {data: whoAmI, error} = await fetchWhoAmI();
        if (whoAmI) {
            setAuthRequired(whoAmI.authRequired);
            setLoggedIn(whoAmI.loggedIn);
            if (whoAmI.loggedIn) setUser(loggedIn.user);
            setAuthLoaded(true)
        } else if (error) {
            setLoginError(error);
        }
    }, [loggedIn])

    const login = async (username, password) => {
        // TODO implement later when login page is done with React
        await updateLoginDetails();
    }

    const logout = () => {
        // TODO implement later when login page is done with React
    }

    const hasPermission = permission => {
        return !authRequired || (loggedIn && user.permissions.some(perm => perm === permission));
    }

    const hasPermissionOtherThan = permission => {
        return !authRequired || (loggedIn && user.permissions.some(perm => perm !== permission));
    }

    useEffect(() => {
        updateLoginDetails();
    }, [updateLoginDetails]);

    const sharedState = {
        authLoaded,
        authRequired,
        loggedIn,
        user,
        login,
        logout,
        loginError,
        hasPermission,
        hasPermissionOtherThan
    }
    return (<AuthenticationContext.Provider value={sharedState}>
            {children}
        </AuthenticationContext.Provider>
    )
}

export const useAuth = () => {
    return useContext(AuthenticationContext);
}