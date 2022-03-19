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

export const fetchServerOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/serverOverview?server=${identifier}&timestamp=${timestamp}`;
    return fetch(url);
}

export const fetchPlayersOnlineGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}`;
    return fetch(url);
}