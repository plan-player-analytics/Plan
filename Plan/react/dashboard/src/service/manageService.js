import {doGetRequest} from "./backendConfiguration";

export const fetchGroups = async () => {
    let url = `/v1/webGroups`;
    return doGetRequest(url, new Date().getTime());
}

export const fetchGroupPermissions = async (groupName) => {
    let url = `/v1/groupPermissions?group=${groupName}`;
    return doGetRequest(url, new Date().getTime());
}

export const fetchAvailablePermissions = async () => {
    let url = `/v1/permissions`;
    return doGetRequest(url, new Date().getTime());
}