import {WorldPie} from "../graph/WorldPie";
import {ServerPie} from "../graph/ServerPie";
import {OutOf} from "./OutOf";
import {OutOfCategory} from "./OutOfCategory";
import {baseAddress, staticSite} from "../../../service/backendConfiguration";
import {filterToQueryString, GenericFilter} from "../GenericFilter";
import {MS_24H, MS_WEEK} from "../../../util/format/useDateFormatter";

export type FormatType = 'NONE' | 'TIME_AMOUNT' | 'TIME_SINCE' | 'DATE' | 'PERCENTAGE' | 'BYTES' | 'SPECIAL';

export enum DatapointType {
    WORLD_PIE = 'WORLD_PIE',
    PLAYTIME = 'PLAYTIME',
    AFK_TIME = 'AFK_TIME',
    AFK_TIME_PERCENTAGE = 'AFK_TIME_PERCENTAGE',
    SERVER_OCCUPIED = 'SERVER_OCCUPIED',
    MOST_ACTIVE_WORLD = 'MOST_ACTIVE_WORLD',
    MOST_ACTIVE_GAME_MODE = 'MOST_ACTIVE_GAME_MODE',
    SERVER_PIE = 'SERVER_PIE',
    UNIQUE_PLAYERS = "UNIQUE_PLAYERS",
    NEW_PLAYERS = "NEW_PLAYERS",
    REGULAR_PLAYERS = "REGULAR_PLAYERS",
    PLAYERS_ONLINE_PEAK = "PLAYERS_ONLINE_PEAK",
    SESSION_COUNT = "SESSION_COUNT",
}

export type DatapointTypeMap = {
    WORLD_PIE: WorldPie;
    PLAYTIME: number;
    AFK_TIME: number;
    AFK_TIME_PERCENTAGE: number;
    UNIQUE_PLAYERS: number;
    NEW_PLAYERS: number;
    REGULAR_PLAYERS: number;
    SESSION_COUNT: number;
    SERVER_OCCUPIED: OutOf;
    MOST_ACTIVE_WORLD: OutOfCategory;
    MOST_ACTIVE_GAME_MODE: OutOfCategory;
    SERVER_PIE: ServerPie;
    PLAYERS_ONLINE_PEAK: { date: number, value: number };
}

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
            timespan = "_" + filter.afterMillisAgo;
        }
        const folder = filter?.player ? "/player/" + filter?.player : "/data";
        const server = filter?.server ? "_" + filter?.server : "";
        url = baseAddress + `${folder}/datapoint-${dataType}${timespan}${server}.json`;
    }
    return url;
}