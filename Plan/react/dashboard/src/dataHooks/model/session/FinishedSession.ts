import {PlayerKill} from "./PlayerKill";
import {PieSlice} from "../graph/PieSlice";
import {GameModeDrilldown} from "../graph/GameModeDrilldown";

export type FinishedSession = {
    player_name: string;
    player_url_name: string;
    player_uuid: string;
    server_name: string;
    server_url_name: string;
    server_uuid: string;
    name: string;
    online: boolean;
    start: number;
    end: number;
    most_used_world: string;
    length: number;
    afk_time: number;
    mob_kills: number;
    deaths: number;
    first_session: boolean;
    join_address: string;
    player_kills: PlayerKill[];
    world_series: PieSlice[];
    gm_series: GameModeDrilldown[];
    avgPing?: number;
}