import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {useDataRequest} from "./dataFetchHook";
import {fetchExtensionData} from "../service/serverService";
import {useAuth} from "./authenticationHook";

const ServerExtensionContext = createContext({});

export const ServerExtensionContextProvider = ({identifier, children}) => {
    const {hasPermission} = useAuth();
    const [extensionData, setExtensionData] = useState(undefined);
    const [extensionDataLoadingError, setExtensionDataLoadingError] = useState(undefined);

    const seePlugins = hasPermission('page.server.plugins');
    const {data, loadingError} = useDataRequest(fetchExtensionData, [identifier], seePlugins);

    useEffect(() => {
        setExtensionData(data);
        setExtensionDataLoadingError(loadingError);
    }, [data, loadingError, setExtensionData, setExtensionDataLoadingError])

    const sharedState = useMemo(() => {
        return {extensionData, extensionDataLoadingError};
    }, [extensionData, extensionDataLoadingError]);
    return (<ServerExtensionContext.Provider value={sharedState}>
            {children}
        </ServerExtensionContext.Provider>
    )
}

export const useServerExtensionContext = () => {
    return useContext(ServerExtensionContext);
}