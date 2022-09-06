import {useEffect, useState} from "react";
import {useNavigation} from "./navigationHook";
import {useDataStore} from "./datastoreHook";
import {useMetadata} from "./metadataHook";

export const useDataRequest = (fetchMethod, parameters) => {
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const {updateRequested, finishUpdate} = useNavigation();
    const {refreshBarrierMs} = useMetadata();
    const datastore = useDataStore();

    /*eslint-disable react-hooks/exhaustive-deps */
    useEffect(() => {
        datastore.setAsUpdating(fetchMethod);
        const handleResponse = (json, error, skipOldData, timeout) => {
            if (json) {
                const timestamp = json.timestamp;
                if (timestamp) {
                    // Data has timestamp, the data may come from cache
                    const acceptedTimestamp = timestamp + (refreshBarrierMs ? refreshBarrierMs : 15000);
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
                setLoadingError(error);
                finishUpdate(new Date().getTime(), "Error", datastore.isSomethingUpdating());
            }
        };

        fetchMethod(updateRequested, ...parameters).then(({data: json, error}) => {
            handleResponse(json, error, false, 1000);
        });
    }, [fetchMethod, ...parameters, updateRequested, refreshBarrierMs])
    /* eslint-enable react-hooks/exhaustive-deps */

    return {data, loadingError};
}