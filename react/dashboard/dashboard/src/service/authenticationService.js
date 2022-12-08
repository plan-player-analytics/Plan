import {doGetRequest, doSomePostRequest, standard200option, staticSite} from "./backendConfiguration";

export const fetchWhoAmI = async () => {
    if (staticSite) {
        return {data: {authRequired: false, loggedIn: false}, error: null};
    }
    const url = '/v1/whoami';
    return doGetRequest(url);
}

export const fetchLogin = async (username, password) => {
    const url = '/auth/login';
    return doSomePostRequest(url, [standard200option], `user=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`);
}

export const postRegister = async (username, password) => {
    const url = '/auth/register';
    return doSomePostRequest(url, [standard200option], `user=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`);
}

export const fetchRegisterCheck = async (code) => {
    const url = `/auth/register?code=${encodeURIComponent(code)}`;
    return doGetRequest(url);
}