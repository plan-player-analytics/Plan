export const queryRetry = (failureCount: number, error: any) => {
    if ([400, 403, 404, 500].includes(error.status)) return false;
    return failureCount < 3;
}