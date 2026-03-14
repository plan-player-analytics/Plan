import {WorldPie} from "../graph/WorldPie";
import {OutOf} from "./OutOf";
import {OutOfCategory} from "./OutOfCategory";

export type FormatType = 'NONE' | 'TIME_AMOUNT' | 'TIME_SINCE' | 'DATE' | 'PERCENTAGE' | 'BYTES' | 'SPECIAL';

export enum DatapointType {
    WORLD_PIE = 'WORLD_PIE',
    PLAYTIME = 'PLAYTIME',
    AFK_TIME = 'AFK_TIME',
    AFK_TIME_PERCENTAGE = 'AFK_TIME_PERCENTAGE',
    SERVER_OCCUPIED = 'SERVER_OCCUPIED',
    MOST_ACTIVE_WORLD = 'MOST_ACTIVE_WORLD',
    MOST_ACTIVE_GAME_MODE = 'MOST_ACTIVE_GAME_MODE'
}

export type DatapointTypeMap = {
    WORLD_PIE: WorldPie;
    PLAYTIME: number;
    AFK_TIME: number;
    AFK_TIME_PERCENTAGE: number;
    SERVER_OCCUPIED: OutOf;
    MOST_ACTIVE_WORLD: OutOfCategory;
    MOST_ACTIVE_GAME_MODE: OutOfCategory;
}

export type Datapoint<K extends keyof DatapointTypeMap> = {
    type: K;
    formatType: FormatType;
    timestamp: number;
    value: DatapointTypeMap[K];
};