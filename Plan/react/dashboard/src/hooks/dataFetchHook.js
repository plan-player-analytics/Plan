import {useEffect, useState} from "react";

export const useDataRequest = (fetchMethod, parameters) => {
    const [data, setData] = useState(undefined);
    const [loadingError, setLoadingError] = useState(undefined);

    /*eslint-disable react-hooks/exhaustive-deps */
    useEffect(() => {
        fetchMethod(...parameters).then(({data: json, error}) => {
            if (json) {
                setData(json)
            } else if (error) {
                setLoadingError(error);
            }
        });
    }, [fetchMethod, ...parameters])
    /* eslint-enable react-hooks/exhaustive-deps */

    return {data, loadingError};
}