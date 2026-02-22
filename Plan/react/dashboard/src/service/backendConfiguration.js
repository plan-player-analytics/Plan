import axios from "axios";

// Using var here intentionally to avoid rollup tree-shake removing the if-blocks using these variables.
var javaReplaced = {
    isStatic: "PLAN_EXPORTED_VERSION",
    address: "PLAN_BASE_ADDRESS"
}

const isCurrentAddress = (address) => {
    let is = window.location.href.startsWith(address);
    const usingProxyHttps = window.location.href.startsWith("https") && !address.startsWith("https");
    if (usingProxyHttps) {
        is = window.location.href.replace('https', '').startsWith(address.replace('http'));
    }
    if (!is) {
        console.warn(`Configured address ${address} did not match start of ${window.location.href}, falling back to relative address. Configure 'Webserver.Alternative_IP' settings to point to your address.`)
    }
    return is;
}

// Using var here intentionally to avoid rollup tree-shake removing the if-blocks using these variables.
export var baseAddress = javaReplaced.address.startsWith('PLAN_') || !isCurrentAddress(javaReplaced.address) ? "" : javaReplaced.address;
export var staticSite = javaReplaced.isStatic === 'true';

export const doSomeGetRequest = async (url, updateRequested, statusOptions) => {
    return doSomeRequest(url, statusOptions, async () => axios.get(baseAddress + url,
        updateRequested ? {headers: {"X-Plan-Timestamp": updateRequested}} : {}));
}

export const doSomePostRequest = async (url, statusOptions, body) => {
    return doSomeRequest(url, statusOptions, async () => axios.post(baseAddress + url, body));
}

export const doSomeDeleteRequest = async (url, statusOptions, body) => {
    return doSomeRequest(url, statusOptions, async () => axios.delete(baseAddress + url, body));
}

export const doSomeRequest = async (url, statusOptions, axiosFunction) => {
    let response = undefined;
    try {
        response = await axiosFunction.call();

        for (const statusOption of statusOptions) {
            if (response.status === statusOption.status) {
                return {
                    status: response.status,
                    data: statusOption.get(response),
                    error: undefined
                };
            }
        }
    } catch (e) {
        console.error(e);
        if (e.response !== undefined) {
            for (const statusOption of statusOptions) {
                if (e.response.status === statusOption.status) {
                    return {
                        status: e.response.status,
                        data: undefined,
                        error: statusOption.get(response, e)
                    };
                }
            }
            return {
                data: undefined,
                error: {
                    status: e.response.status,
                    message: e.message,
                    url,
                    data: e.response.data
                }
            };
        }
        return {
            data: undefined,
            error: {
                status: undefined,
                message: e.message,
                url
            }
        };
    }
    return {
        data: undefined,
        error: {
            status: undefined,
            message: 'Reached unknown section of the code',
            url
        }
    };
}

export const standard200option = {status: 200, get: response => response.data}
const exported404options = {status: 404, get: () => 'Data not yet exported'}

export const doGetRequest = async (url, updateRequested) => {
    return doSomeGetRequest(url, updateRequested, staticSite ? [standard200option, exported404options] : [standard200option])
}