import {filterToQueryString, GenericFilter} from "./model/GenericFilter";
import {useQuery} from "@tanstack/react-query";
import {baseAddress, staticSite} from "../service/backendConfiguration";
import {Session} from "react-router";
import {useNavigation} from "../hooks/navigationHook";
import {useEffect} from "react";
import {queryRetry} from "./queryRetry";


export const useSessions = (filter: GenericFilter) => {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const query = useQuery({
        queryKey: ['sessions', ...Object.values(filter)],
        queryFn: () => getSessions(filter),
        retry: queryRetry
    });
    useEffect(() => {
        query.refetch()
    }, [updateRequested]);
    return query;
}

const getSessions = async (filter: GenericFilter) => {
    let url = baseAddress + `/v1/sessions?${filterToQueryString(filter)}`;
    if (staticSite) url = baseAddress + `/v1/sessions`;
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Session[]>;
}