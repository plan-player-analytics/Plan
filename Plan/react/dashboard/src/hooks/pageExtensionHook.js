import {createContext, useCallback, useContext, useMemo} from "react";

const PageExtensionContext = createContext({});

export const PageExtensionContextProvider = ({children}) => {
    const onRender = useCallback(async (className, position) => {
        return await global.pageExtensionApi.onRender(className, position);
    }, []);

    const onUnmount = useCallback((className, position) => {
        global.pageExtensionApi.onUnmount(className, position);
    }, []);

    const sharedState = useMemo(() => {
        return {
            onRender, onUnmount
        };
    }, [onRender, onUnmount]);
    return (<PageExtensionContext.Provider value={sharedState}>
            {children}
        </PageExtensionContext.Provider>
    )
}

export const usePageExtension = () => {
    return useContext(PageExtensionContext);
}