export type PerformanceZones = {
    tpsThresholdMed: number;
    tpsThresholdHigh: number;
    diskThresholdMed: number;
    diskThresholdHigh: number;
}
type Zone = {
    value: number;
    color: string;
}
export type ParsedPerformanceZones = {
    tps?: [Zone, Zone, Zone];
    disk?: [Zone, Zone, Zone];
    [key: string]: [Zone, Zone, Zone] | undefined;
}