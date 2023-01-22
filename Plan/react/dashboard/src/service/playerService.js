import {faMapSigns} from "@fortawesome/free-solid-svg-icons";
import {doSomeGetRequest, standard200option, staticSite} from "./backendConfiguration";

export const fetchPlayer = async (timestamp, uuid) => {
    let url = `/v1/player?player=${uuid}`;
    if (staticSite) url = `/player/${uuid}/player-${uuid}.json`
    return doSomeGetRequest(url, timestamp, [
        standard200option,
        {
            status: staticSite ? 404 : 400,
            get: () => ({
                message: 'Player not found: ' + uuid + ', try another player.' + (staticSite ? ' You can try the export players command.' : ''),
                title: '404 Player not found',
                icon: faMapSigns
            })
        }
    ])
}