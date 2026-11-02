import { useMutation, useQueryClient } from '@tanstack/react-query';
import { baseAddress } from '../service/backendConfiguration';

interface SaveApiTokenVariables {
    token: string;
    identifier: string;
    permissions: string[];
}

export const useSaveApiToken = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ token, identifier, permissions }: SaveApiTokenVariables) =>
            saveApiToken(token, identifier, permissions),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['apiTokens'] });
        },
    });
};

const saveApiToken = async (
    token: string,
    identifier: string,
    permissions: string[],
): Promise<void> => {
    const url = baseAddress + `/v1/saveApiToken?token=${token}&identifier=${identifier}`;
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
