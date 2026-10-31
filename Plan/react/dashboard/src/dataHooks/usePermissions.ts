import { useSuspenseQuery } from '@tanstack/react-query';
import { queryRetry } from './queryRetry';
import { baseAddress, staticSite } from '../service/backendConfiguration';
import { PermissionsResponse } from './model/permissions/PermissionsResponse';
import { useMemo } from 'react';

export const usePermissions = () => {
    const query = useSuspenseQuery({
        queryKey: ['permissions'],
        queryFn: () => getPermissions(),
        retry: queryRetry,
    });

    return useMemo(() => query.data.permissions, [query]);
};

const getPermissions = async () => {
    let url = baseAddress + `/v1/permissions`;
    if (staticSite) {
        return { permissions: [] };
    }
    const response = await fetch(url);
    if (!response.ok)
        throw {
            status: response.status,
            message: response.statusText,
            data: response.body,
        };
    return (await response.json()) as Promise<PermissionsResponse>;
};
