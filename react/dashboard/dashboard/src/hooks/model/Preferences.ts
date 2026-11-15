export type PreferencesResponse = {
    defaultPreferences: Preferences;
    preferences: Partial<Preferences>;
};

export type Preferences = {
    recentDaysInDateFormat: boolean;
    dateFormatFull: string;
    dateFormatNoSeconds: string;
    dateFormatClock: string;
    timeFormat: TimeFormat;
    decimalFormat: string;
    firstDay: number;

    playerHeadImageUrl: string;

    tpsThresholds: GraphThresholds;
    diskThresholds: GraphThresholds;

    joinAddresses: {
        [key: string]: AddressGroup[];
    };
};

export type TimeFormat = {
    year: string;
    years: string;
    month: string;
    months: string;
    day: string;
    days: string;
    hours: string;
    minutes: string;
    seconds: string;
    zero: string;
};

export type GraphThresholds = {
    highThreshold: number;
    mediumThreshold: number;
};

export type AddressGroup = {
    name: string;
    uuid: string;
    addresses: string[];
};
