import {useEffect, useMemo, useState} from "react";
import {useNavigation} from "./navigationHook";
import {useDataStore} from "./datastoreHook";
import {useMetadata} from "./metadataHook";
import {staticSite} from "../service/backendConfiguration";

export const useDataRequest = (fetchMethod, parameters, shouldRequest) => {
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const {updateRequested, finishUpdate} = useNavigation();
    const {refreshBarrierMs} = useMetadata();
    const datastore = useDataStore();

    /*eslint-disable react-hooks/exhaustive-deps */
    useEffect(() => {
        if (shouldRequest !== undefined && !shouldRequest) {
            setData(undefined);
            return;
        }
        datastore.setAsUpdating(fetchMethod);
        const handleResponse = (json, error, skipOldData, timeout) => {
            if (json) {
                const timestamp = json.timestamp;
                if (!staticSite && timestamp) {
                    // Data has timestamp, the data may come from cache
                    const acceptedTimestamp = timestamp + (refreshBarrierMs || 15000);
                    if (acceptedTimestamp < updateRequested) {
                        // Request again, received data was too old
                        setTimeout(() => {
                            fetchMethod(new Date().getTime(), ...parameters)
                                .then(({data: json, error}) => {
                                    handleResponse(json, error, true, timeout >= 12000 ? timeout : timeout * 2);
                                });
                        }, timeout);
                    } else {
                        // Received data was new enough to be shown
                        setData(json);
                        datastore.storeData(fetchMethod, json);
                        datastore.finishUpdate(fetchMethod)
                        finishUpdate(json.timestamp, json.timestamp_f, datastore.isSomethingUpdating());
                    }

                    if (!skipOldData) {
                        // Old data is shown on first pass, further passes skip old data.
                        setData(json);
                        datastore.storeData(fetchMethod, json);
                        finishUpdate(json.timestamp, json.timestamp_f, datastore.isSomethingUpdating());
                    }
                } else {
                    // Response data is not cached, no timestamp, show it immediately
                    setData(json);
                    datastore.finishUpdate(fetchMethod);
                    finishUpdate(json.timestamp, json.timestamp_f, datastore.isSomethingUpdating());
                }
            } else if (error) {
                console.warn(error);
                datastore.finishUpdate(fetchMethod)
                const isObject = error?.data !== null && typeof error?.data === 'object' && !Array.isArray(error?.data);
                if (isObject) {
                    setLoadingError({...error, ...error.data, data: undefined})
                } else {
                    setLoadingError(error);
                }
                finishUpdate(0, "Error: " + error.message, datastore.isSomethingUpdating());
            }
        };

        fetchMethod(updateRequested, ...parameters).then(({data: json, error}) => {
            handleResponse(json, error, false, 1000);
        });
    }, [fetchMethod, parameters.length, ...parameters, updateRequested, refreshBarrierMs, shouldRequest])
    /* eslint-enable react-hooks/exhaustive-deps */

    return useMemo(() => {
        return {data, loadingError}
    }, [data, loadingError]);
}