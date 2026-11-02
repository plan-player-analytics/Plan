import { useSuspenseQuery } from '@tanstack/react-query';
import { queryRetry } from './queryRetry';
import { baseAddress, staticSite } from '../service/backendConfiguration';
import { ApiTokenWithPermissions } from './model/permissions/ApiToken';

export const useApiTokens = () => {
    const query = useSuspenseQuery({
        queryKey: ['apiTokens'],
        queryFn: () => getApiTokens(),
        retry: queryRetry,
    });

    return query.data;
};

const getApiTokens = async (): Promise<ApiTokenWithPermissions[]> => {
    let url = baseAddress + `/v1/apiToken`;
    if (staticSite) {
        return [];
    }
    const response = await fetch(url);
    if (!response.ok)
        throw {
            status: response.status,
            message: response.statusText,
            data: response.body,
        };
    return (await response.json()) as Promise<ApiTokenWithPermissions[]>;
};
