export type PlayerKill = {
    date: number;
    killer: string;
    victim: string;
    killerUUID: string;
    victimUUID: string;
    killerName: string;
    victimName: string;
    serverUUID: string;
    serverName: string;
    timeSinceRegisterMillis: number;
    timeSinceRegisterFormatted: string;
    weapon: string;
}