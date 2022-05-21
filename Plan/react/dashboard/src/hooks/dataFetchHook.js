import {useEffect, useState} from "react";
import {useNavigation} from "./navigationHook";

export const useDataRequest = (fetchMethod, parameters) => {
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);
    const {updateRequested, finishUpdate} = useNavigation();

    /*eslint-disable react-hooks/exhaustive-deps */
    useEffect(() => {
        fetchMethod(...parameters, updateRequested).then(({data: json, error}) => {
            if (json) {
                setData(json);
                finishUpdate(json.timestamp, json.timestamp_f);
            } else if (error) {
                setLoadingError(error);
            }
        });
    }, [fetchMethod, ...parameters, updateRequested])
    /* eslint-enable react-hooks/exhaustive-deps */

    return {data, loadingError};
}