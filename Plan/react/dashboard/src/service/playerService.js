import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {doSomeGetRequest, standard200option} from "./backendConfiguration";

export const fetchPlayer = async (uuid, timestamp) => {
    const url = `/v1/player?player=${uuid}&timestamp=${timestamp}`;
    return doSomeGetRequest(url, [
        standard200option,
        {
            status: 400,
            get: () => ({
                message: 'Player not found: ' + uuid + ', try another player',
                title: '404 Player not found',
                icon: faMapSigns
            })
        }
    ])
}