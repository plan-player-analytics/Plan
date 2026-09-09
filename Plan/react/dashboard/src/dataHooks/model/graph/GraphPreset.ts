import {PerformanceGraphId} from "./performance/PerformanceGraphId";
import {IconDefinition} from "@fortawesome/free-regular-svg-icons";

export type GraphPreset = {
    name: string;
    color: string;
    icon: IconDefinition;
    permission: string | string[];
    series: PerformanceGraphId[];
}