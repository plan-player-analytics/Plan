import {
    doGetRequest,
    doSomeDeleteRequest,
    doSomePostRequest,
    standard200option,
    staticSite
} from "./backendConfiguration";

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

export const fetchPreferences = async () => {
    let url = '/v1/preferences';
    if (staticSite) url = '/metadata/preferences.json'
    return doGetRequest(url);
}

export const savePreferences = async preferences => {
    if (staticSite) return;
    let url = '/v1/storePreferences'
    return doSomePostRequest(url, [standard200option], preferences);
}

export const fetchTheme = async (name, onlyJar) => {
    let url = `/v1/theme?theme=${name}${onlyJar ? '&onlyJar=true' : ''}`;
    if (staticSite) url = `/theme/${name}.json`
    return doGetRequest(url);
}

export const saveTheme = async (name, theme, originalName) => {
    if (staticSite) return;
    let url = `/v1/saveTheme?theme=${name}&originalName=${originalName}`;
    return doSomePostRequest(url, [standard200option], JSON.stringify(theme));
}

export const deleteTheme = async (name) => {
    if (staticSite) return;
    let url = `/v1/deleteTheme?theme=${name}`;
    return doSomeDeleteRequest(url, [standard200option]);
}