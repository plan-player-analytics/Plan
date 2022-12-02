import {doGetRequest, staticSite} from "./backendConfiguration";

export const fetchPlanMetadata = async () => {
    let url = '/v1/metadata';
    if (staticSite) url = '/metadata/metadata.json'
    return doGetRequest(url);
}

export const fetchPlanVersion = async () => {
    let url = '/v1/version';
    if (staticSite) url = '/metadata/version.json'
    return doGetRequest(url);
}

export const fetchAvailableLocales = async () => {
    let url = '/v1/locale';
    if (staticSite) url = '/locale/locale.json'
    return doGetRequest(url);
}

export const fetchErrorLogs = async () => {
    let url = '/v1/errors';
    return doGetRequest(url);
}

export const fetchNetworkMetadata = async () => {
    let url = '/v1/networkMetadata';
    if (staticSite) url = '/metadata/networkMetadata.json'
    return doGetRequest(url);
}