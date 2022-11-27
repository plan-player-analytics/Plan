import axios from "axios";

const javaReplaced = {
    isStatic: "PLAN_EXPORTED_VERSION",
    address: "PLAN_BASE_ADDRESS"
}

const isCurrentAddress = (address) => {
    const is = window.location.href.startsWith(address);
    if (!is) console.warn(`Configured address ${address} did not match start of ${window.location.href}, falling back to relative address. Configure 'Webserver.Alternative_IP' settings to point to your address.`)
    return is;
}

export const baseAddress = javaReplaced.address.startsWith('PLAN_') || !isCurrentAddress(javaReplaced.address) ? "" : javaReplaced.address;
export const staticSite = javaReplaced.isStatic === 'true';

export const doSomeGetRequest = async (url, statusOptions) => {
    return doSomeRequest(url, statusOptions, async () => axios.get(url));
}

export const doSomePostRequest = async (url, statusOptions, body) => {
    return doSomeRequest(url, statusOptions, async () => axios.post(url, body));
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
}

export const standard200option = {status: 200, get: response => response.data}

export const doGetRequest = async url => {
    return doSomeGetRequest(url, [standard200option])
}