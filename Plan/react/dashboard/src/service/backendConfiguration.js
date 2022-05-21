import axios from "axios";

const toBeReplaced = "PLAN_BASE_ADDRESS";

const isCurrentAddress = (address) => {
    const is = window.location.href.startsWith(address);
    if (!is) console.warn(`Configured address ${address} did not match start of ${window.location.href}, falling back to relative address. Configure 'Webserver.Alternative_IP' settings to point to your address.`)
    return is;
}

export const baseAddress = "PLAN_BASE_ADDRESS" === toBeReplaced || !isCurrentAddress(toBeReplaced) ? "" : toBeReplaced;

export const doSomeGetRequest = async (url, statusOptions) => {
    let response = undefined;
    try {
        response = await axios.get(baseAddress + url);

        for (const statusOption of statusOptions) {
            if (response.status === statusOption.status) {
                return {
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
                        data: undefined,
                        error: statusOption.get(response, e)
                    };
                }
            }
            return {
                data: undefined,
                error: {
                    message: e.message,
                    url,
                    data: e.response.data
                }
            };
        }
        return {
            data: undefined,
            error: {
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