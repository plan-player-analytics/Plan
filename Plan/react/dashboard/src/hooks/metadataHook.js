import {createContext, useContext, useEffect, useState} from "react";
import {fetchPlanMetadata} from "../service/metadataService";

const MetadataContext = createContext({});

export const MetadataContextProvider = ({children}) => {
    const [metadata, setMetadata] = useState({});

    const updateMetadata = async () => {
        setMetadata(await fetchPlanMetadata());
    }

    useEffect(() => {
        updateMetadata();
    }, []);

    const sharedState = {...metadata}
    return (<MetadataContext.Provider value={sharedState}>
            {children}
        </MetadataContext.Provider>
    )
}

export const useMetadata = () => {
    return useContext(MetadataContext);
}