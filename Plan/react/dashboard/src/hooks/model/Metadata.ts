enum ContributedFor {
    LANG = "LANG",
    CODE = "CODE"
}

export type Contributor = {
    name: string,
    contributed: ContributedFor[]
}

export type Server = {
    serverUUID: string;
    serverName: string;
    proxy: boolean;
}

export type NetworkMetadata = {
    servers: Server[];
    currentServer: Server;
    usingRedisBungee: boolean;
}

export type Metadata = {
    timestamp: number;
    timeZoneId: string;
    timeZoneOffsetHours: number;
    timeZoneOffsetMinutes: number;
    contributors: Contributor[];
    defaultTheme: string;
    defaultNightMode: boolean;
    availableThemes: string[];
    gmPieColors: string[];
    playerHeadImageUrl: string;
    isProxy: boolean;
    serverName: string;
    serverUUID: string;
    networkName: string;
    mainCommand: string;
    refreshBarrierMs: number;
    registrationDisabled: boolean;
    networkMetadata?: NetworkMetadata;
}