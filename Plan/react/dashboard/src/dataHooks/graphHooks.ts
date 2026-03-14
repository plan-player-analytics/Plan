import {filterToQueryString, GenericFilter} from "./model/GenericFilter";
import {useNavigation} from "../hooks/navigationHook";
import {useQuery} from "@tanstack/react-query";
import {queryRetry} from "./queryRetry";
import {useEffect} from "react";
import {baseAddress, staticSite} from "../service/backendConfiguration";
import {Datapoint, DatapointType} from "./model/datapoint/Datapoint";

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
    let url = baseAddress + `/v1/datapoint?type=${DatapointType.WORLD_PIE}&${filterToQueryString(filter)}`;
    if (staticSite) return undefined;
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Datapoint<DatapointType.WORLD_PIE>>;
}