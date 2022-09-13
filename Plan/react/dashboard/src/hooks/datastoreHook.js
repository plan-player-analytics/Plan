import {useCallback} from "react";
import {useMetadata} from "./metadataHook";

export const useDataStore = () => {
    const {datastore} = useMetadata();

    if (datastore && !datastore.dataByMethod) datastore.dataByMethod = {};
    if (datastore && !datastore.lastUpdateByMethod) datastore.lastUpdateByMethod = {};
    if (datastore && !datastore.currentlyUpdatingMethods) datastore.currentlyUpdatingMethods = {};

    const storeData = useCallback((method, data) => {
        const hadPrevious = Boolean(datastore.dataByMethod[method]);
        if (data) {
            datastore.lastUpdateByMethod[method] = data.timestamp;
            datastore.dataByMethod[method] = data;
        }
        return hadPrevious;
    }, [datastore]);

    const getLastUpdate = useCallback((method) => {
        return datastore?.lastUpdateByMethod[method];
    }, [datastore]);

    const getData = useCallback((method) => {
        return datastore?.dataByMethod[method];
    }, [datastore]);

    const isCurrentlyUpdating = useCallback((method) => {
        return datastore && Boolean(datastore.currentlyUpdatingMethods[method]);
    }, [datastore]);

    const setAsUpdating = useCallback((method) => {
        datastore.currentlyUpdatingMethods[method] = true;
    }, [datastore]);

    const finishUpdate = useCallback((method) => {
        delete datastore.currentlyUpdatingMethods[method];
    }, [datastore])

    const isSomethingUpdating = useCallback(() => {
        return datastore && Boolean(Object.values(datastore.currentlyUpdatingMethods).filter(value => Boolean(value)).length);
    }, [datastore]);

    return {storeData, getLastUpdate, getData, isCurrentlyUpdating, isSomethingUpdating, setAsUpdating, finishUpdate};
}