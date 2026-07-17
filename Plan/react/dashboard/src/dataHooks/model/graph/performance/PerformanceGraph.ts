import {LineSeries} from "../LineSeries";
import {PerformanceZones} from "./PerformanceZones";

export enum PerformanceGraphKeys {
    DATE = "date",
    PLAYERS_ONLINE = "playersOnline",
    TPS = "tps",
    CPU = "cpu",
    RAM = "ram",
    ENTITIES = "entities",
    CHUNKS = "chunks",
    DISK = "disk",
    MSPT_AVERAGE = "msptAverage",
    MSPT_95TH = "mspt95thPercentile",
    MSPT_JITTER_AVERAGE = "msptJitterAverage",
    MSPT_JITTER_MAX = "msptJitterMax"
}

export type PerformanceGraph = {
    keys: PerformanceGraphKeys[];
    values: LineSeries[];
    zones: PerformanceZones;
    serverName: string;
    serverUUID: string;
}

export type ParsedPerformanceGraph = {
    playersOnline: LineSeries;
    tps: LineSeries;
    cpu: LineSeries;
    ram: LineSeries;
    entities: LineSeries;
    chunks: LineSeries;
    disk: LineSeries;
    msptAverage: LineSeries;
    mspt95thPercentile: LineSeries;
    msptJitterAverage: LineSeries;
    msptJitterMax: LineSeries;
}