import { Server } from './Metadata';

export type QueryResult = {
    path: QueryPath[];
    view: QueryView;
    filters: QueryFilter[];
    timestamp: number;
    data?: QueryData;
};

export type QueryPath = {
    kind: string;
    size: number;
};

export type QueryView = {
    afterDate: string;
    afterTime: string;
    beforeDate: string;
    beforeTime: string;
    servers: Server[];
};

export type QueryFilter = {
    kind: string;
    parameters: {
        [key: string]: string;
    };
};

export type QueryData = {
    players: any; //PlayerListDto;
    activity: any; //ActivityGraphData;
    geolocation: any; //GeolocationData;
    sessions: QuerySessionSummary;
};

export type QuerySessionSummary = {
    totalPlaytime: string;
    averagePlaytime: string;
    totalAfkPlaytime: string;
    averageAfkPlaytime: string;
    totalActivePlaytime: string;
    averageActivePlaytime: string;
    totalSessions: string; // number
    averageSessions: string; // number
    averageSessionLength: string;
};
