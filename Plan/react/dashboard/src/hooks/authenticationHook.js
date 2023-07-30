import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
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
            if (whoAmI.loggedIn) setUser(whoAmI.user);
            setLoggedIn(whoAmI.loggedIn);
            setAuthLoaded(true)
        } else if (error) {
            setLoginError(error);
        }
    }, [])

    const hasPermission = useCallback(permission => {
        if (Array.isArray(permission)) {
            for (const permissionOption of permission) {
                if (hasPermission(permissionOption)) {
                    return true;
                }
            }
            return false;
        }
        return !authRequired || (loggedIn && user && Boolean(user.permissions.filter(perm => permission.includes(perm)).length));
    }, [authRequired, loggedIn, user]);

    const hasChildPermission = useCallback(permission => {
        if (Array.isArray(permission)) {
            for (const permissionOption of permission) {
                if (hasChildPermission(permissionOption)) {
                    return true;
                }
            }
            return false;
        }
        return !authRequired || (loggedIn && user && Boolean(user.permissions.filter(perm => perm.includes(permission) || permission.includes(perm)).length));
    }, [authRequired, loggedIn, user]);

    const hasPermissionOtherThan = useCallback(permission => {
        return !authRequired || (loggedIn && user && user.permissions.filter(perm => perm !== permission).length);
    }, [authRequired, loggedIn, user]);

    useEffect(() => {
        updateLoginDetails();
    }, [updateLoginDetails]);

    const sharedState = useMemo(() => {
        return {
            authLoaded,
            authRequired,
            loggedIn,
            user,
            loginError,
            hasPermission,
            hasChildPermission,
            hasPermissionOtherThan,
            updateLoginDetails
        }
    }, [
        authLoaded,
        authRequired,
        loggedIn,
        user,
        loginError,
        hasPermission,
        hasChildPermission,
        hasPermissionOtherThan,
        updateLoginDetails
    ])
    return (<AuthenticationContext.Provider value={sharedState}>
            {children}
        </AuthenticationContext.Provider>
    )
}

export const useAuth = () => {
    return useContext(AuthenticationContext);
}