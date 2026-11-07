import { PlayerKill } from './PlayerKill';
import { PieSlice } from '../graph/PieSlice';
import { PieDrilldown } from '../graph/PieDrilldown';

export type FinishedSession = {
    playerName: string;
    playerUrlName: string;
    playerUUID: string;
    serverName: string;
    serverUrlName: string;
    serverUUID: string;
    name: string;
    online: boolean;
    start: number;
    end: number;
    mostUsedWorld: string;
    length: number;
    afkTime: number;
    mobKills: number;
    deaths: number;
    firstSession: boolean;
    joinAddress: string;
    playerKills: PlayerKill[];
    worldSeries: PieSlice[];
    gmSeries: PieDrilldown[];
    avgPing?: number;
};
