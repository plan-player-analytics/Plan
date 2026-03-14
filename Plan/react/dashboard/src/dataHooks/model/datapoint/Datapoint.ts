import {WorldPie} from "../graph/WorldPie";

export type FormatType = 'NONE' | 'TIME_AMOUNT' | 'TIME_SINCE' | 'DATE' | 'PERCENTAGE' | 'BYTES' | 'SPECIAL';

export enum DatapointType {
    WORLD_PIE = 'WORLD_PIE',
    PLAYTIME = 'PLAYTIME',
    AFK_TIME = 'AFK_TIME',
    AFK_TIME_PERCENTAGE = 'AFK_TIME_PERCENTAGE',
}

export type DatapointTypeMap = {
    WORLD_PIE: WorldPie;
    PLAYTIME: number;
    AFK_TIME: number;
    AFK_TIME_PERCENTAGE: number;
}

export type Datapoint<K extends keyof DatapointTypeMap> = {
    type: K;
    formatType: FormatType;
    timestamp: number;
    value: DatapointTypeMap[K];
};