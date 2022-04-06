import {createContext, useCallback, useContext, useEffect, useState} from "react";
import {fetchPlanMetadata} from "../service/metadataService";

const MetadataContext = createContext({});

export const MetadataContextProvider = ({children}) => {
    const [metadata, setMetadata] = useState({});

    const updateMetadata = useCallback(async () => {
        const {data, error} = await fetchPlanMetadata();
        if (data) {
            setMetadata(data);
        } else if (error) {
            setMetadata({metadataError: error})
        }
    }, [])

    const getPlayerHeadImageUrl = useCallback((name, uuid) => {
        if (!uuid && name === 'console') {
            return '../../Terminal-icon.png';
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
    }, [updateMetadata]);

    const sharedState = {...metadata, getPlayerHeadImageUrl}
    return (<MetadataContext.Provider value={sharedState}>
            {children}
        </MetadataContext.Provider>
    )
}

export const useMetadata = () => {
    return useContext(MetadataContext);
}