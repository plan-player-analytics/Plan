import {doGetRequest} from "./backendConfiguration";

export const fetchWhoAmI = async () => {
    const url = '/v1/whoami';
    return doGetRequest(url);
}