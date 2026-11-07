import { useMutation, useQueryClient } from '@tanstack/react-query';
import { baseAddress } from '../service/backendConfiguration';

export const useDeleteApiToken = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (token: string) => deleteApiToken(token),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['apiTokens'] });
        },
    });
};

const deleteApiToken = async (token: string): Promise<void> => {
    const url = baseAddress + `/v1/deleteApiToken?token=${token}`;
    const response = await fetch(url, {
        method: 'DELETE',
    });
    if (!response.ok)
        throw {
            status: response.status,
            message: response.statusText,
            data: response.body,
        };
};
