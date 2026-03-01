import {createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchWhoAmI} from "../service/authenticationService";
import {PlanError} from "../views/ErrorView";

type Permission = string | string[];

type User = {
    username: string;
    playerName?: string;
    playerUUID?: string;
    permissions: string[];
}

type WhoAmI =
    | {
    loggedIn: false,
    authRequired: boolean
}
    | {
    loggedIn: true;
    authRequired: boolean;
    user: User
}

type AuthenticationContextValues = {
    authLoaded: boolean;
    authRequired: boolean;
    loggedIn: boolean;
    user?: User;
    loginError?: PlanError;
    hasPermission: (permission: Permission) => boolean;
    hasChildPermission: (permission: Permission) => boolean;
    hasPermissionOtherThan: (permission: Permission) => boolean;
    updateLoginDetails: () => Promise<void>
}

const AuthenticationContext = createContext<AuthenticationContextValues | undefined>(undefined);

export const AuthenticationContextProvider = ({children}: PropsWithChildren) => {
    const [loginError, setLoginError] = useState(undefined);

    const [authLoaded, setAuthLoaded] = useState(false)
    const [authRequired, setAuthRequired] = useState(false);
    const [loggedIn, setLoggedIn] = useState(false);
    const [user, setUser] = useState<User | undefined>(undefined);

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

    const hasPermission = useCallback((permission: Permission) => {
        if (Array.isArray(permission)) {
            for (const permissionOption of permission) {
                if (hasPermission(permissionOption)) {
                    return true;
                }
            }
            return false;
        }
        return !authRequired || (loggedIn && !!user && Boolean(user.permissions.filter(perm => permission.includes(perm)).length));
    }, [authRequired, loggedIn, user]);

    const hasChildPermission = useCallback((permission: Permission) => {
        if (Array.isArray(permission)) {
            for (const permissionOption of permission) {
                if (hasChildPermission(permissionOption)) {
                    return true;
                }
            }
            return false;
        }
        return !authRequired || (loggedIn && !!user && Boolean(user.permissions.filter(perm => perm.includes(permission) || permission.includes(perm)).length));
    }, [authRequired, loggedIn, user]);

    const hasPermissionOtherThan = useCallback((permission: Permission) => {
        return !authRequired || (loggedIn && !!user && Boolean(user.permissions.filter(perm => perm !== permission).length));
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
    const context = useContext(AuthenticationContext);
    if (!context) throw new Error('useAuth must be used inside AuthenticationContextProvider');
    return context;
}