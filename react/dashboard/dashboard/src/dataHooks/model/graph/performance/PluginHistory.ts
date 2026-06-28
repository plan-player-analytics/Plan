export type PluginHistory = {
    history: PluginHistoryMetadata[];
}

export type PluginHistoryMetadata = {
    name: string;
    version?: string;
    modified: number;
}