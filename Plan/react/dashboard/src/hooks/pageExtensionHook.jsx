import {createContext, useCallback, useContext, useMemo} from "react";
import {useAuth} from "./authenticationHook.tsx";
import {useNavigation} from "./navigationHook";
import {useTheme} from "./themeHook";
import {useMetadata} from "./metadataHook";
import {withReducedSaturation} from "../util/colors";
import axios from "axios";
import {useTranslation} from "react-i18next";

const PageExtensionContext = createContext({});

export const PageExtensionContextProvider = ({children}) => {
    const onRender = useCallback(async (className, position) => {
        return await global.pageExtensionApi.onRender(className, position);
    }, []);

    const onUnmount = useCallback((className, position) => {
        global.pageExtensionApi.onUnmount(className, position);
    }, []);

    const {t} = useTranslation();
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
                colorMap: {}, // deprecated, use var(--color-plugins-red) etc. in css instead.
                withReducedSaturation
            },
            metadata: {
                ...metadata
            },
            utilities: {
                axios,
                i18nTranslate: t
            }
        };
    }, [authContext, navigationContext, themeContext, metadata, t]);

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