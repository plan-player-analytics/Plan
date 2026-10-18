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
    total_playtime: string;
    average_playtime: string;
    total_afk_playtime: string;
    average_afk_playtime: string;
    total_active_playtime: string;
    average_active_playtime: string;
    total_sessions: string; // number
    average_sessions: string; // number
    average_session_length: string;
};
