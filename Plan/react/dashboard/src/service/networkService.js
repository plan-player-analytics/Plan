import {doGetRequest, staticSite} from "./backendConfiguration";

export const fetchNetworkOverview = async (updateRequested) => {
    let url = `/v1/network/overview?timestamp=${updateRequested}`;
    if (staticSite) url = `/data/network-overview.json`;
    return doGetRequest(url);
}

export const fetchServersOverview = async (updateRequested) => {
    let url = `/v1/network/servers?timestamp=${updateRequested}`;
    if (staticSite) url = `/data/network-servers.json`;
    return doGetRequest(url);
}

export const fetchServerPie = async (timestamp) => {
    let url = `/v1/graph?type=serverPie&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-serverPie.json`;
    return doGetRequest(url);
}

export const fetchNetworkSessionsOverview = async (timestamp) => {
    let url = `/v1/network/sessionsOverview?timestamp=${timestamp}`;
    if (staticSite) url = `/data/network-sessionsOverview.json`;
    return doGetRequest(url);
}

export const fetchNetworkPlayerbaseOverview = async (timestamp) => {
    let url = `/v1/network/playerbaseOverview?timestamp=${timestamp}`;
    if (staticSite) url = `/data/network-playerbaseOverview.json`;
    return doGetRequest(url);
}

export const fetchNetworkPingTable = async (timestamp) => {
    let url = `/v1/network/pingTable?timestamp=${timestamp}`;
    if (staticSite) url = `/data/network-pingTable.json`;
    return doGetRequest(url);
}

export const fetchNetworkPerformanceOverview = async (timestamp, serverUUIDs) => {
    let url = `/v1/network/performanceOverview?servers=${encodeURIComponent(JSON.stringify(serverUUIDs))}&timestamp=${timestamp}`;
    return doGetRequest(url);
}