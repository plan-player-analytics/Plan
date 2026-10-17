import {Datapoint, DatapointType, getDatapointUrl} from "./model/datapoint/Datapoint";
import {GenericFilter} from "./model/GenericFilter";
import {useNavigation} from "../hooks/navigationHook";
import {useEffect, useRef} from "react";
import {useQueries, useQuery} from "@tanstack/react-query";
import {queryRetry} from "./queryRetry";

export function useDatapointQuery<K extends DatapointType>(allowed: boolean, dataType: K, filter?: GenericFilter) {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const prevUpdateRef = useRef<number | undefined>(undefined);
    const query = useQuery({
        queryKey: filter ? ['datapoint', dataType, ...Object.values(filter)] : ['datapoint', dataType],
        queryFn: () => getDatapoint(dataType, filter),
        retry: queryRetry,
        enabled: Boolean(allowed)
    });
    useEffect(() => {
        if (allowed && prevUpdateRef.current && prevUpdateRef.current <= updateRequested) {
            query.refetch()
        }
        prevUpdateRef.current = updateRequested;
    }, [updateRequested, allowed]);
    return query;
}

export function useDatapointQueries<K extends DatapointType>(allowed: boolean, dataType: K, filters: GenericFilter[]) {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const prevUpdateRef = useRef<number | undefined>(undefined);
    const query = useQueries({
        queries: filters.map(filter => ({
            queryKey: filter ? ['datapoint', dataType, ...Object.values(filter)] : ['datapoint', dataType],
            queryFn: () => getDatapoint(dataType, filter),
            retry: queryRetry,
            enabled: Boolean(allowed)
        }))
    });
    useEffect(() => {
        if (allowed && prevUpdateRef.current && prevUpdateRef.current <= updateRequested) {
            query.forEach(q => q.refetch())
        }
        prevUpdateRef.current = updateRequested;
    }, [updateRequested, allowed]);
    return query;
}

async function getDatapoint<K extends DatapointType>(dataType: K, filter?: GenericFilter) {
    const url = getDatapointUrl(dataType, filter);
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<K>>;
}