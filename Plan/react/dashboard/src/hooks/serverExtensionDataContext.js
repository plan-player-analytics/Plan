import {createContext, useContext, useEffect, useState} from "react";
import {useDataRequest} from "./dataFetchHook";
import {fetchExtensionData} from "../service/serverService";

const ServerExtensionContext = createContext({});

export const ServerExtensionContextProvider = ({identifier, children}) => {
    const [extensionData, setExtensionData] = useState(undefined);
    const [extensionDataLoadingError, setExtensionDataLoadingError] = useState(undefined);

    const {data, loadingError} = useDataRequest(fetchExtensionData, [identifier]);

    useEffect(() => {
        setExtensionData(data);
        setExtensionDataLoadingError(loadingError);
    }, [data, loadingError, setExtensionData, setExtensionDataLoadingError])

    const sharedState = {extensionData, extensionDataLoadingError}
    return (<ServerExtensionContext.Provider value={sharedState}>
            {children}
        </ServerExtensionContext.Provider>
    )
}

export const useServerExtensionContext = () => {
    return useContext(ServerExtensionContext);
}