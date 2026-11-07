import { LineSeries } from '../LineSeries';

export type PingGraph = {
    minPingSeries: LineSeries;
    maxPingSeries: LineSeries;
    avgPingSeries: LineSeries;
};
