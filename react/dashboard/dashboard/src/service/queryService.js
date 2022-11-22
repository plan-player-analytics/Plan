import {doGetRequest, doSomePostRequest, standard200option} from "./backendConfiguration";

export const fetchFilters = async () => {
    const url = `/v1/filters`;
    return doGetRequest(url);
}

export const postQuery = async (inputDto) => {
    const url = `/v1/query`;
    return doSomePostRequest(url, [standard200option], inputDto);
}

export const fetchExistingResults = async (timestamp) => {
    const url = `/v1/query?timestamp=${timestamp}`;
    return doGetRequest(url);
}