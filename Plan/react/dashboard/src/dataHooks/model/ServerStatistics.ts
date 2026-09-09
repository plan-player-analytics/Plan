export type ServerStatistics = {
    serverUUID: string;
    statistics: {
        [key: string]: number;
    }
}