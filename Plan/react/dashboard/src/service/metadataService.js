import {doGetRequest} from "./backendConfiguration";

export const fetchPlanMetadata = async () => {
    const url = '/v1/metadata';
    return doGetRequest(url);
}

export const fetchPlanVersion = async () => {
    const url = '/v1/version';
    return doGetRequest(url);
}

export const fetchAvailableLocales = async () => {
    const url = '/v1/locale';
    return doGetRequest(url);
}

export const fetchErrorLogs = async () => {
    const url = '/v1/errors';
    return doGetRequest(url);
}

export const fetchNetworkMetadata = async () => {
    const url = '/v1/networkMetadata';
    return doGetRequest(url);
}