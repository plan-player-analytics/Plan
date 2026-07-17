import {PlanError} from "../views/ErrorView";

export type PlanResponse<T> =
    | {
    status?: number;
    data: T,
    error: undefined
} | {
    status?: number;
    data: undefined,
    error: PlanError
};

export type PlanDataResponse<T> =
    | {
    data: T;
    loadingError: undefined
} | {
    data: undefined,
    loadingError: PlanError
}