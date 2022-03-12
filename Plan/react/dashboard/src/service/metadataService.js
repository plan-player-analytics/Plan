import axios from "axios";

export const fetchPlanMetadata = async () => {
    const url = '/v1/metadata';

    let response = undefined;
    try {
        response = await axios.get(url);

        if (response.status === 200) return response.data;
    } catch (e) {
        throw {message: e.message, url, data: e.response.data}
    }
}

export const fetchPlanVersion = async () => {
    const url = '/v1/version';

    let response = undefined;
    try {
        response = await axios.get(url);

        if (response.status === 200) return response.data;
    } catch (e) {
        throw {message: e.message, url, data: e.response.data}
    }
}