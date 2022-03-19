import {createContext, useCallback, useContext, useEffect, useState} from "react";
import {fetchPlanMetadata} from "../service/metadataService";

const MetadataContext = createContext({});

export const MetadataContextProvider = ({children}) => {
    const [metadata, setMetadata] = useState({});

    const updateMetadata = useCallback(async () => {
        setMetadata(await fetchPlanMetadata());
    })

    const getPlayerHeadImageUrl = (name, uuid) => {
        /* eslint-disable no-template-curly-in-string */
        return (metadata.playerHeadImageUrl ? metadata.playerHeadImageUrl : "https://cravatar.eu/helmavatar/${playerUUID}/120.png")
            .replace('${playerUUID}', uuid)
            .replace('${playerUUIDNoDash}', uuid.split('-').join(''))
            .replace('${playerName}', name)
        /* eslint-enable no-template-curly-in-string */
    }

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