import axios from "axios";
import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {RequestError} from "../util/errors";

export const fetchPlayer = async (uuid) => {
    const url = '/v1/player?player=' + uuid;

    let response = undefined;
    try {
        response = await axios.get(url);

        if (response.status === 200) return response.data;
    } catch (e) {
        if (e.response.status === 400) throw new RequestError({
            message: 'Player not found: ' + uuid + ', try another player',
            title: '404 Player not found',
            icon: faMapSigns
        });
        throw new RequestError({message: e.message, url, data: e.response.data})
    }
}