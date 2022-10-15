import {doGetRequest} from "./backendConfiguration";

export const fetchFilters = async () => {
    const url = `/v1/filters`;
    return doGetRequest(url);
}
