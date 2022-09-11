import {doGetRequest} from "./backendConfiguration";

export const fetchNetworkOverview = async (updateRequested) => {
    const url = `/v1/network/overview?timestamp=${updateRequested}`;
    return doGetRequest(url);
}

export const fetchServersOverview = async (updateRequested) => {
    const url = `/v1/network/servers?timestamp=${updateRequested}`;
    return doGetRequest(url);
}

export const fetchServerPie = async (timestamp) => {
    const url = `/v1/graph?type=serverPie&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchNetworkSessionsOverview = async (timestamp) => {
    const url = `/v1/network/sessionsOverview?timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchNetworkPingTable = async (timestamp) => {
    const url = `/v1/network/pingTable?timestamp=${timestamp}`;
    return doGetRequest(url);
}