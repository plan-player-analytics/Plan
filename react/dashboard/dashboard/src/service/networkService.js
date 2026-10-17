import {doGetRequest, staticSite} from "./backendConfiguration";

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

export const fetchNetworkStatistics = async (timestamp, aggregate) => {
    let url = `/v1/statistics${aggregate ? '?aggregate=true' : ''}`;
    if (staticSite) url = `/data/statistics-${aggregate ? '-aggregate' : ''}.json`;
    return doGetRequest(url, timestamp);
}