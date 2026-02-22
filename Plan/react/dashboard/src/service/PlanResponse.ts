import {PlanError} from "../views/ErrorView";

export type PlanResponse<T> = {
    status?: number;
    data: T,
    error: undefined
} | {

    status?: number;
    data: undefined,
    error: PlanError
}