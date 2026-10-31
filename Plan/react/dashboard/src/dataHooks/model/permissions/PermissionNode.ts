export type PermissionNode = {
    permission: string;
    parentIndex: number;
    children: PermissionNode[];
    toggled: boolean;
};
