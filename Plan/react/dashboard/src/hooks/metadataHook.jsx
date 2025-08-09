import {createContext, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchNetworkMetadata, fetchPlanMetadata} from "../service/metadataService";

import terminal from '../Terminal-icon.png'
import {useAuth} from "./authenticationHook";
import {getLocallyStoredThemes} from "./context/themeContextHook.jsx";

const MetadataContext = createContext({});

export const MetadataContextProvider = ({children}) => {
    const [datastore] = useState({});
    const [metadata, setMetadata] = useState({});
    const {authRequired, authLoaded, loggedIn} = useAuth();
    const [updateThemeList, setUpdateThemeList] = useState(Date.now());

    const refreshThemeList = () => {
        setUpdateThemeList(Date.now());
        updateMetadata();
    }

    const updateMetadata = useCallback(async () => {
        if (authRequired && (!authLoaded || !loggedIn)) return;

        const {data, error} = await fetchPlanMetadata();
        if (data) {
            setMetadata(data);
            const {data: networkMetadata} = await fetchNetworkMetadata(); // error ignored
            if (networkMetadata) {
                setMetadata({...data, networkMetadata})
            }
        } else if (error) {
            setMetadata({metadataError: error})
        }
    }, [authRequired, authLoaded, loggedIn]);

    const getPlayerHeadImageUrl = useCallback((name, uuid) => {
        if (!uuid && name === 'console') {
            return terminal;
        }

        /* eslint-disable no-template-curly-in-string */
        return (metadata.playerHeadImageUrl ? metadata.playerHeadImageUrl : "https://cravatar.eu/helmavatar/${playerUUID}/120.png")
            .replace('${playerUUID}', uuid)
            .replace('${playerUUIDNoDash}', uuid ? uuid.split('-').join('') : undefined)
            .replace('${playerName}', name)
        /* eslint-enable no-template-curly-in-string */
    }, [metadata.playerHeadImageUrl]);

    useEffect(() => {
        updateMetadata();
    }, [updateMetadata, authLoaded, loggedIn]);

    const displayedServerName = metadata.isProxy ? metadata.networkName : (metadata.serverName?.startsWith('Server') ? "Plan" : metadata.serverNameserverName);

    const sharedState = useMemo(() => {
            const loaded = Object.keys(metadata).length > 0 && !metadata.metadataError;
            return {
                ...metadata,
                getAvailableThemes: () => loaded ? [...new Set([...metadata.availableThemes, ...getLocallyStoredThemes()])] : getLocallyStoredThemes(),
                getPlayerHeadImageUrl,
                datastore,
                displayedServerName,
                loaded,
                refreshThemeList
            }
        },
        [metadata, getPlayerHeadImageUrl, datastore, displayedServerName, updateThemeList]);
    return (<MetadataContext.Provider value={sharedState}>
            {children}
        </MetadataContext.Provider>
    )
}

export const useMetadata = () => {
    return useContext(MetadataContext);
}