import {LineSeries} from "../LineSeries";

export type PingGraph = {
    min_ping_series: LineSeries;
    max_ping_series: LineSeries;
    avg_ping_series: LineSeries;
}