import {useQuery} from "@tanstack/react-query";
import {ExtensionGraph} from "../model/extension/ExtensionGraph";
import {useNavigation} from "../../hooks/navigationHook";
import {useEffect, useRef} from "react";
import {queryRetry} from "../queryRetry";

type Props = {
    graph: string;
    server: string;
    player?: string;
}

export const useExtensionGraph = ({graph, server, player}: Props) => {
    const {updateRequested} = useNavigation() as { updateRequested: number };
    const prevUpdateRef = useRef<number | undefined>(undefined);
    const query = useQuery({
        queryKey: ['extensionGraph', graph, server, player],
        queryFn: () => getExtensionGraph(graph, server, player),
        retry: queryRetry,
        enabled: true
    });
    useEffect(() => {
        if (prevUpdateRef.current && prevUpdateRef.current <= updateRequested) {
            query.refetch()
        }
        prevUpdateRef.current = updateRequested;
    }, [updateRequested]);
    return query;
}

async function getExtensionGraph(graph: string, server: string, player?: string) {
    const playerParam = player ? `&player=${player}` : '';
    const url = `/v1/extensionGraph?graph=${graph}&server=${server}${playerParam}`;
    const response = await fetch(url);
    if (!response.ok) throw {status: response.status, message: response.statusText, data: response.body};
    return await response.json() as Promise<ExtensionGraph>;
}