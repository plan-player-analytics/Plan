import {WorldPie} from "../graph/WorldPie";

export type FormatType = 'NONE' | 'TIME_AMOUNT' | 'TIME_SINCE' | 'DATE' | 'PERCENTAGE' | 'BYTES';

export enum DatapointType {
    WORLD_PIE = 'WORLD_PIE',
    PLAYTIME = 'PLAYTIME',
}

type TypeMap = {
    WORLD_PIE: WorldPie;
    PLAYTIME: number;
}

export type Datapoint<K extends keyof TypeMap> = {
    type: K;
    format: FormatType;
    timestamp: number;
    value: TypeMap[K];
};