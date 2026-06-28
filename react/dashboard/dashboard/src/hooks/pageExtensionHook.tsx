import {createContext, PropsWithChildren, useCallback, useContext, useMemo} from "react";
import {useAuth} from "./authenticationHook";
import {useNavigation} from "./navigationHook";
import {useTheme} from "./themeHook";
import {useMetadata} from "./metadataHook";
import {withReducedSaturation} from "../util/colors";
import axios from "axios";
import {useTranslation} from "react-i18next";

type PageExtensionContextValue = {
    onRender: (className: string, position: string, context: any) => Promise<HTMLElement[]>;
    onUnmount: (className: string, position: string) => void;
    context: any;
}

const PageExtensionContext = createContext<PageExtensionContextValue | undefined>(undefined);

type PageExtensionApi = {
    onRender: (className: string, position: string, context: any) => Promise<HTMLElement[]>;
    onUnmount: (className: string, position: string) => void;
}

export const PageExtensionContextProvider = ({children}: PropsWithChildren) => {
    const onRender = useCallback(async (className: string, position: string, context: any) => {
        if ('pageExtensionApi' in globalThis) {
            return await ((globalThis as any).pageExtensionApi as PageExtensionApi).onRender(className, position, context);
        }
        return [];
    }, []);

    const onUnmount = useCallback((className: string, position: string) => {
        if ('pageExtensionApi' in globalThis) {
            ((globalThis as any).pageExtensionApi as PageExtensionApi).onUnmount(className, position);
        }
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
                currentThemeColor: themeContext.color,
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
    const context = useContext(PageExtensionContext);
    if (!context) throw new Error("usePageExtension must be used within PageExtensionContextProvider");
    return context;
}