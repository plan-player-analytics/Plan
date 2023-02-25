import {createContext, useCallback, useContext, useMemo} from "react";
import {useAuth} from "./authenticationHook";
import {useNavigation} from "./navigationHook";
import {useTheme} from "./themeHook";
import {useMetadata} from "./metadataHook";
import {getColors, withReducedSaturation} from "../util/colors";
import axios from "axios";

const PageExtensionContext = createContext({});

export const PageExtensionContextProvider = ({children}) => {
    const onRender = useCallback(async (className, position) => {
        return await global.pageExtensionApi.onRender(className, position);
    }, []);

    const onUnmount = useCallback((className, position) => {
        global.pageExtensionApi.onUnmount(className, position);
    }, []);

    const authContext = useAuth();
    const navigationContext = useNavigation();
    const themeContext = useTheme();
    const metadata = useMetadata();
    const callContext = useMemo(() => {
        return {
            authentication: {
                loggedIn: authContext.loggedIn,
                user: authContext.user,
                hasPermission: authContext.hasPermission
            },
            navigation: {
                currentTab: navigationContext.currentTab
            },
            theme: {
                currentThemeColor: themeContext.selectedColor,
                colorMap: getColors(),
                withReducedSaturation
            },
            metadata: {
                ...metadata
            },
            utilities: {
                axios
            }
        };
    }, [authContext, navigationContext, themeContext, metadata]);

    const sharedState = useMemo(() => {
        return {
            onRender, onUnmount, context: callContext
        };
    }, [onRender, onUnmount, callContext]);
    return (<PageExtensionContext.Provider value={sharedState}>
            {children}
        </PageExtensionContext.Provider>
    )
}

export const usePageExtension = () => {
    return useContext(PageExtensionContext);
}