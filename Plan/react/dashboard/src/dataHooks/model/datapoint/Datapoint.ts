import {WorldPie} from "../graph/WorldPie";
import {ServerPie} from "../graph/ServerPie";
import {OutOf} from "./OutOf";
import {OutOfCategory} from "./OutOfCategory";
import {baseAddress, staticSite} from "../../../service/backendConfiguration";
import {filterToQueryString, GenericFilter} from "../GenericFilter";
import {MS_24H, MS_WEEK} from "../../../util/format/useDateFormatter";

export type FormatType =
    'NONE'
    | 'TIME_AMOUNT'
    | 'TIME_SINCE'
    | 'DATE'
    | 'DECIMAL'
    | 'PERCENTAGE'
    | 'BYTES'
    | 'SPECIAL';

export enum DatapointType {
    WORLD_PIE = 'WORLD_PIE',
    PLAYTIME = 'PLAYTIME',
    AFK_TIME = 'AFK_TIME',
    AFK_TIME_PERCENTAGE = 'AFK_TIME_PERCENTAGE',
    SERVER_OCCUPIED = 'SERVER_OCCUPIED',
    MOST_ACTIVE_WORLD = 'MOST_ACTIVE_WORLD',
    MOST_ACTIVE_GAME_MODE = 'MOST_ACTIVE_GAME_MODE',
    SERVER_PIE = 'SERVER_PIE',
    UNIQUE_PLAYERS_COUNT = "UNIQUE_PLAYERS_COUNT",
    UNIQUE_PLAYERS_AVERAGE = "UNIQUE_PLAYERS_AVERAGE",
    NEW_PLAYERS = "NEW_PLAYERS",
    REGULAR_PLAYERS = "REGULAR_PLAYERS",
    PLAYERS_ONLINE = "PLAYERS_ONLINE",
    PLAYERS_ONLINE_PEAK = "PLAYERS_ONLINE_PEAK",
    UPTIME_CURRENT = "UPTIME_CURRENT",
    SESSION_COUNT = "SESSION_COUNT",
    SESSION_LENGTH_AVERAGE = "SESSION_LENGTH_AVERAGE",
    PLAYTIME_PER_PLAYER_AVERAGE = "PLAYTIME_PER_PLAYER_AVERAGE",
    DEATHS = "DEATHS",
    PLAYER_KILLS = "PLAYER_KILLS",
    MOB_KILLS = "MOB_KILLS",
    TPS_AVERAGE = "TPS_AVERAGE",
}

export type DatapointTypeMap = {
    WORLD_PIE: WorldPie;
    PLAYTIME: number;
    AFK_TIME: number;
    AFK_TIME_PERCENTAGE: number;
    UNIQUE_PLAYERS_COUNT: number;
    UNIQUE_PLAYERS_AVERAGE: number;
    NEW_PLAYERS: number;
    REGULAR_PLAYERS: number;
    PLAYERS_ONLINE: number;
    UPTIME_CURRENT: number;
    SESSION_COUNT: number;
    SESSION_LENGTH_AVERAGE: number;
    PLAYTIME_PER_PLAYER_AVERAGE: number;
    DEATHS: number;
    PLAYER_KILLS: number;
    MOB_KILLS: number;
    TPS_AVERAGE: number;
    SERVER_OCCUPIED: OutOf;
    MOST_ACTIVE_WORLD: OutOfCategory;
    MOST_ACTIVE_GAME_MODE: OutOfCategory;
    SERVER_PIE: ServerPie;
    PLAYERS_ONLINE_PEAK: { date: number, value: number };
}

export type NumericDatapointType = {
    [K in DatapointType]:
    DatapointTypeMap[K] extends number ? K : never
}[DatapointType];

export type Datapoint<K extends keyof DatapointTypeMap> = {
    type: K;
    formatType: FormatType;
    timestamp: number;
    value: DatapointTypeMap[K];
};

export function getDatapointPermission(suffix: string, filter?: GenericFilter) {
    if (filter) {
        if (filter.player) return 'data.player.' + suffix;
        if (filter.server?.length) return 'data.server.' + suffix;
    }
    return 'data.network.' + suffix;
}

export function getDatapointUrl(dataType: DatapointType, filter?: GenericFilter) {
    let url = baseAddress + `/v1/datapoint?type=${dataType}&${filterToQueryString(filter)}`;
    if (staticSite) {
        let timespan = "";
        if (filter?.after) {
            const diff = (filter?.before || Date.now()) - filter.after;
            if (diff < MS_24H * 2) {
                timespan = "_1d";
            } else if (diff < MS_WEEK * 2) {
                timespan = "_7d";
            } else {
                timespan = "_30d";
            }
        }
        if (filter?.afterMillisAgo) {
            timespan = "_" + filter.afterMillisAgo + (filter?.beforeMillisAgo ? "_" + filter.beforeMillisAgo : '');
        }
        const folder = filter?.player ? "/player/" + filter?.player : "/data";
        const server = filter?.server ? "_" + filter?.server : "";
        url = baseAddress + `${folder}/datapoint-${dataType}${timespan}${server}.json`;
    }
    return url;
}