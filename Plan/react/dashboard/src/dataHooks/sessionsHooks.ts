import {filterToQueryString, GenericFilter} from "./model/GenericFilter";
import {useQuery} from "@tanstack/react-query";
import {baseAddress, staticSite} from "../service/backendConfiguration";
import {Session} from "react-router";
import {useNavigation} from "../hooks/navigationHook";
import {useEffect} from "react";
import {queryRetry} from "./queryRetry";
import {useDateConverter} from "../util/format/useDateConverter";
import {useAuth} from "../hooks/authenticationHook";

function getPermission(filter: GenericFilter) {
    if (filter) {
        if (filter.player) return 'page.player.sessions';
        if (filter.server?.length) return 'page.server.sessions.list';
    }
    return 'page.network.sessions.list';
}

export const useSessions = (filter: GenericFilter) => {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const {convert} = useDateConverter();
    const {hasPermission} = useAuth();

    const ownsPermission = hasPermission(getPermission(filter))
    const serverFilter = {
        ...filter,
        after: filter.after ? convert(filter.after).toServerEpochMs() : undefined,
        before: filter.before ? convert(filter.before).toServerEpochMs() : undefined,
    };
    const query = useQuery({
        queryKey: ['sessions', ...Object.values(serverFilter)],
        queryFn: () => getSessions(serverFilter),
        retry: queryRetry,
        enabled: ownsPermission
    });
    useEffect(() => {
        query.refetch()
    }, [updateRequested]);
    return query;
}

const getSessions = async (filter: GenericFilter) => {
    let url = baseAddress + `/v1/sessions?${filterToQueryString(filter)}`;
    if (staticSite) {
        if (filter.player) {
            url = baseAddress + `/player/${filter.player}/sessions-${filter.player}.json`;
        } else if (filter.server && filter.server.length > 0) {
            url = baseAddress + `/data/sessions-${Array.isArray(filter.server) ? filter.server[0] : filter.server}.json`;
        } else {
            url = baseAddress + `/data/sessions.json`;
        }
    }
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<Session[]>;
}