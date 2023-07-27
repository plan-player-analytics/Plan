import {doGetRequest, doSomeDeleteRequest, doSomePostRequest, standard200option} from "./backendConfiguration";

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

export const saveGroupPermissions = async (groupName, permissions) => {
    let url = `/v1/saveGroupPermissions?group=${groupName}`;
    return doSomePostRequest(url, [standard200option], JSON.stringify(permissions));
}

export const addGroup = async (groupName) => {
    let url = `/v1/saveGroupPermissions?group=${groupName}`;
    return doSomePostRequest(url, [standard200option], JSON.stringify([]));
}

export const deleteGroup = async (groupName, moveTo) => {
    let url = `/v1/deleteGroup?group=${groupName}&moveTo=${moveTo}`;
    return doSomeDeleteRequest(url, [standard200option]);
}