import {doGetRequest} from "./backendConfiguration";

export const fetchNetworkOverview = async (updateRequested) => {
    const url = `/v1/network/overview?timestamp=${updateRequested}`;
    return doGetRequest(url);
}