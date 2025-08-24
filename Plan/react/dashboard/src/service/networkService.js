import {doGetRequest, staticSite} from "./backendConfiguration";

export const fetchNetworkOverview = async (updateRequested) => {
    let url = `/v1/network/overview`;
    if (staticSite) url = `/data/network-overview.json`;
    return doGetRequest(url, updateRequested);
}

export const fetchServersOverview = async (updateRequested) => {
    let url = `/v1/network/servers`;
    if (staticSite) url = `/data/network-servers.json`;
    return doGetRequest(url, updateRequested);
}

export const fetchServerPie = async (timestamp) => {
    let url = `/v1/graph?type=serverPie`;
    if (staticSite) url = `/data/graph-serverPie.json`;
    return doGetRequest(url, timestamp);
}

export const fetchNetworkSessionsOverview = async (timestamp) => {
    let url = `/v1/network/sessionsOverview`;
    if (staticSite) url = `/data/network-sessionsOverview.json`;
    return doGetRequest(url, timestamp);
}

export const fetchNetworkPlayerbaseOverview = async (timestamp) => {
    let url = `/v1/network/playerbaseOverview`;
    if (staticSite) url = `/data/network-playerbaseOverview.json`;
    return doGetRequest(url, timestamp);
}

export const fetchNetworkPingTable = async (timestamp) => {
    let url = `/v1/network/pingTable`;
    if (staticSite) url = `/data/network-pingTable.json`;
    return doGetRequest(url, timestamp);
}

export const fetchNetworkPerformanceOverview = async (timestamp, serverUUIDs) => {
    let url = `/v1/network/performanceOverview?servers=${encodeURIComponent(JSON.stringify(serverUUIDs))}`;
    return doGetRequest(url, timestamp);
}