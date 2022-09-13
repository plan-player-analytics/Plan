import {doGetRequest, doSomePostRequest, standard200option} from "./backendConfiguration";

export const fetchWhoAmI = async () => {
    const url = '/v1/whoami';
    return doGetRequest(url);
}

export const fetchLogin = async (username, password) => {
    const url = '/auth/login';
    return doSomePostRequest(url, [standard200option], `user=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`);
}