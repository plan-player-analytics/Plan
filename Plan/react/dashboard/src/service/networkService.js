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