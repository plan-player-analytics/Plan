import { useMutation, useQueryClient } from '@tanstack/react-query';
import { baseAddress } from '../service/backendConfiguration';

interface CreateApiTokenVariables {
    identifier: string;
    permissions?: string[];
}

export const useCreateApiToken = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ identifier, permissions }: CreateApiTokenVariables) =>
            createApiToken(identifier, permissions || []),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['apiTokens'] });
        },
    });
};

const createApiToken = async (
    identifier: string,
    permissions: string[],
): Promise<void> => {
    const url = baseAddress + `/v1/saveApiToken?identifier=${identifier}`;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(permissions),
    });
    if (!response.ok)
        throw {
            status: response.status,
            message: response.statusText,
            data: response.body,
        };
};
