import axios from "axios";

const toBeReplaced = "PLAN_BASE_ADDRESS";

export const baseAddress = toBeReplaced.includes("BASE") ? "" : toBeReplaced;

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
        for (const statusOption of statusOptions) {
            if (e.response.status === statusOption.status) {
                return {
                    data: undefined,
                    error: statusOption.get(response, e)
                };
            }
        }
        console.error(e);
        return {
            data: undefined,
            error: {
                message: e.message,
                url,
                data: e.response.data
            }
        };
    }
}

export const standard200option = {status: 200, get: response => response.data}

export const doGetRequest = async url => {
    return doSomeGetRequest(url, [standard200option])
}