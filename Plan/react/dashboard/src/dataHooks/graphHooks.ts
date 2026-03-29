import {GenericFilter} from "./model/GenericFilter";
import {useNavigation} from "../hooks/navigationHook";
import {useQuery} from "@tanstack/react-query";
import {queryRetry} from "./queryRetry";
import {useEffect} from "react";
import {Datapoint, DatapointType, getDatapointUrl} from "./model/datapoint/Datapoint";

export const useWorldPie = (filter: GenericFilter) => {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const query = useQuery({
        queryKey: ['world-pie', ...Object.values(filter)],
        queryFn: () => getWorldPie(filter),
        retry: queryRetry
    });
    useEffect(() => {
        query.refetch()
    }, [updateRequested]);
    return query;
}

const getWorldPie = async (filter: GenericFilter) => {
    const url = getDatapointUrl(DatapointType.WORLD_PIE, filter);
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<DatapointType.WORLD_PIE>>;
}

export const useServerPie = (filter: GenericFilter) => {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const query = useQuery({
        queryKey: ['server-pie', ...Object.values(filter)],
        queryFn: () => getServerPie(filter),
        retry: queryRetry
    });
    useEffect(() => {
        query.refetch()
    }, [updateRequested]);
    return query;
}

const getServerPie = async (filter: GenericFilter) => {
    const url = getDatapointUrl(DatapointType.SERVER_PIE, filter);
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<DatapointType.SERVER_PIE>>;
}