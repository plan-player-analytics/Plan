import {PerformanceGraphId} from "./performance/PerformanceGraphId";
import {IconDefinition} from "@fortawesome/free-regular-svg-icons";
import {LineSeries} from "./LineSeries";
import {YAxisById} from "./YAxis";

export type GraphSeries = | {
    id: PerformanceGraphId;
    name: string;
    icon: IconDefinition;
    color: string;
    permission: string;
    data: LineSeries | undefined;
    options: {
        type: string;
        tooltip: { valueDecimals: number },
        yAxis: keyof YAxisById
    },
    show?: boolean;
} | {
    id: PerformanceGraphId;
    name: string;
    icon: IconDefinition;
    color: string;
    permission: string;
    series: PerformanceGraphId[];
    show?: boolean;
}