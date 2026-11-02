export type ApiToken = {
    id: number;
    identifier: string;
    token: string;
    issued: number;
    lastUsed: number | null;
};

export type ApiTokenWithPermissions = {
    token: ApiToken;
    permissions: string[];
};
