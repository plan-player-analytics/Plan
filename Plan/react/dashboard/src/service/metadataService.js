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

export const fetchPlanMetadata = async () => {
    const url = '/v1/metadata';
    return fetch(url);
}

export const fetchPlanVersion = async () => {
    const url = '/v1/version';
    return fetch(url);
}

export const fetchAvailableLocales = async () => {
    const url = '/v1/locale';
    return fetch(url);
}