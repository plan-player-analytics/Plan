import React, {createContext, useCallback, useContext, useMemo, useState} from "react";

const ConfigurationStorageContext = createContext({});

/**
 * This context provides a way to independently manage state and its storage, while managing save and discard together.
 *
 * @returns {JSX.Element}
 * @constructor
 */
export const ConfigurationStorageContextProvider = ({children}) => {
    const [saveRequested, setSaveRequested] = useState(0);
    const [discardRequested, setDiscardRequested] = useState(0);
    const [dirty, setDirty] = useState(false);

    const requestSave = useCallback(() => {
        if (dirty) {
            setSaveRequested(Date.now());
            setDirty(false);
        }
    }, [setSaveRequested, dirty, setDirty]);

    const requestDiscard = useCallback(() => {
        if (dirty) {
            setDiscardRequested(Date.now());
            setDirty(false);
        }
    }, [setDiscardRequested, dirty, setDirty]);

    const markDirty = useCallback(() => {
        setDirty(true);
    }, [setDirty])

    const sharedState = useMemo(() => {
        return {
            dirty,
            saveRequested,
            discardRequested,
            requestSave,
            requestDiscard,
            markDirty
        }
    }, [
        dirty,
        saveRequested,
        discardRequested,
        requestSave,
        requestDiscard,
        markDirty
    ])
    return (
        <ConfigurationStorageContext.Provider value={sharedState}>
            {children}
        </ConfigurationStorageContext.Provider>
    )
}

export const useConfigurationStorageContext = () => {
    return useContext(ConfigurationStorageContext);
}