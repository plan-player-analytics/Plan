import {createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState} from "react";
import {fetchNetworkMetadata, fetchPlanMetadata} from "../service/metadataService";

import terminal from '../Terminal-icon.png'
import {useAuth} from "./authenticationHook";

import {getLocallyStoredThemes} from "./themeHook";
import {PlanResponse} from "../service/PlanResponse";
import {Metadata, NetworkMetadata} from "./model/Metadata";
import {PlanError} from "../views/ErrorView";

type MetadataContextValue =
    | ({
    loaded: true;
    displayedServerName: string;
    metadataError?: PlanError;
    getPlayerHeadImageUrl: (name: string, uuid: string) => string;
    getAvailableThemes: () => string[];
    refreshThemeList: () => void;
} & Metadata)
    | {
    loaded: false;
    displayedServerName: string;
    metadataError?: PlanError;
    getPlayerHeadImageUrl: (name: string, uuid: string) => string;
    getAvailableThemes: () => string[];
    refreshThemeList: () => void;
};

const MetadataContext = createContext<MetadataContextValue | undefined>(undefined);

export const MetadataContextProvider = ({children}: PropsWithChildren) => {
    const [metadataError, setMetadataError] = useState<PlanError | undefined>();
    const [metadata, setMetadata] = useState<Metadata | undefined>(undefined);
    const {authRequired, authLoaded, loggedIn} = useAuth();
    const [updateThemeList, setUpdateThemeList] = useState(Date.now());

    const updateMetadata = useCallback(async () => {
        if (authRequired && (!authLoaded || !loggedIn)) return;

        const {data, error} = await fetchPlanMetadata() as PlanResponse<Metadata>;
        if (data) {
            setMetadata(data);
            const {data: networkMetadata} = await fetchNetworkMetadata() as PlanResponse<NetworkMetadata>; // error ignored
            if (networkMetadata) {
                setMetadata({...data, networkMetadata})
            }
        } else if (error) {
            setMetadataError(error);
        }
    }, [authRequired, authLoaded, loggedIn]);

    const getPlayerHeadImageUrl = useCallback((name: string, uuid: string) => {
        if (!metadata) return name;
        if (!uuid && name === 'console') {
            return terminal;
        }

        /* eslint-disable no-template-curly-in-string */
        return (metadata.playerHeadImageUrl ? metadata.playerHeadImageUrl : "https://cravatar.eu/helmavatar/${playerUUID}/120.png")
            .replace('${playerUUID}', uuid)
            .replace('${playerUUIDNoDash}', uuid ? uuid.split('-').join('') : '')
            .replace('${playerName}', name)
        /* eslint-enable no-template-curly-in-string */
    }, [metadata]);

    const refreshThemeList = useCallback(() => {
        setUpdateThemeList(Date.now());
        updateMetadata();
    }, [updateMetadata, setUpdateThemeList]);

    useEffect(() => {
        updateMetadata();
    }, [updateMetadata, authLoaded, loggedIn]);

    const displayedServerName = useMemo(() => {
        if (!metadata) return "Plan";
        if (metadata.isProxy) return metadata.networkName;
        if (metadata.serverName?.startsWith("Server")) return "Plan";
        return metadata.serverName;
    }, [metadata]);

    const getAvailableThemes = useCallback(() => {
        return metadata ? [...new Set([...metadata.availableThemes, ...getLocallyStoredThemes()])] : getLocallyStoredThemes();
    }, [metadata])

    const sharedState = useMemo(() => {
            const loaded = !metadata || Object.keys(metadata).length > 0 && !metadataError;
            return {
                ...metadata,
                metadataError,
                getAvailableThemes,
                getPlayerHeadImageUrl,
                displayedServerName,
                loaded,
                refreshThemeList
            }
        },
        [metadata, metadataError, getPlayerHeadImageUrl, displayedServerName, updateThemeList, getAvailableThemes, refreshThemeList]);
    return (<MetadataContext.Provider value={sharedState as MetadataContextValue}>
            {children}
        </MetadataContext.Provider>
    )
}

export const useMetadata = () => {
    const context = useContext(MetadataContext);
    if (!context) throw new Error('useMetadata must be used within MetadataContextProvider.');
    return context;
}