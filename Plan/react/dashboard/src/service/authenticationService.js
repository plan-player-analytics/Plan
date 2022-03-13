import axios from "axios";
import {RequestError} from "../util/errors";

async function fetch(url) {
    let response = undefined;
    try {
        response = await axios.get(url);

        if (response.status === 200) return response.data;
    } catch (e) {
        throw RequestError({message: e.message, url, data: e.response.data})
    }
}

export const fetchWhoAmI = async () => {
    const url = '/v1/whoami';
    return fetch(url);
}