import {doGetRequest, staticSite} from "./backendConfiguration";

export const fetchServerIdentity = async (timestamp, identifier) => {
    let url = `/v1/serverIdentity?server=${identifier}`;
    if (staticSite) url = `/data/serverIdentity-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchServerOverview = async (timestamp, identifier) => {
    let url = `/v1/serverOverview?server=${identifier}`;
    if (staticSite) url = `/data/serverOverview-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchOnlineActivityOverview = async (timestamp, identifier) => {
    let url = `/v1/onlineOverview?server=${identifier}`;
    if (staticSite) url = `/data/onlineOverview-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayerbaseOverview = async (timestamp, identifier) => {
    let url = `/v1/playerbaseOverview?server=${identifier}`;
    if (staticSite) url = `/data/playerbaseOverview-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchSessionOverview = async (timestamp, identifier) => {
    let url = `/v1/sessionsOverview?server=${identifier}`;
    if (staticSite) url = `/data/sessionsOverview-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPvpPve = async (timestamp, identifier) => {
    let url = `/v1/playerVersus?server=${identifier}`;
    if (staticSite) url = `/data/playerVersus-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPerformanceOverview = async (timestamp, identifier) => {
    let url = `/v1/performanceOverview?server=${identifier}`;
    if (staticSite) url = `/data/performanceOverview-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchExtensionData = async (timestamp, identifier) => {
    let url = `/v1/extensionData?server=${identifier}`;
    if (staticSite) url = `/data/extensionData-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchSessions = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchSessionsServer(timestamp, identifier);
    } else {
        return await fetchSessionsNetwork(timestamp);
    }
}

const fetchSessionsServer = async (timestamp, identifier) => {
    let url = `/v1/sessions?server=${identifier}`;
    if (staticSite) url = `/data/sessions-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchSessionsNetwork = async (timestamp) => {
    let url = `/v1/sessions`;
    if (staticSite) url = `/data/sessions.json`;
    return doGetRequest(url, timestamp);
}

export const fetchKills = async (timestamp, identifier) => {
    let url = `/v1/kills?server=${identifier}`;
    if (staticSite) url = `/data/kills-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayers = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayersServer(timestamp, identifier);
    } else {
        return await fetchPlayersNetwork(timestamp);
    }
}
const fetchPlayersServer = async (timestamp, identifier) => {
    let url = `/v1/players?server=${identifier}`;
    if (staticSite) url = `/data/players-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchPlayersNetwork = async (timestamp) => {
    let url = `/v1/players`;
    if (staticSite) url = `/data/players.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPingTable = async (timestamp, identifier) => {
    let url = `/v1/pingTable?server=${identifier}`;
    if (staticSite) url = `/data/pingTable-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayersOnlineGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayersOnlineGraphServer(timestamp, identifier);
    } else {
        return await fetchPlayersOnlineGraphNetwork(timestamp);
    }
}

const fetchPlayersOnlineGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=playersOnline&server=${identifier}`;
    if (staticSite) url = `/data/graph-playersOnline_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchPlayersOnlineGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=playersOnline`;
    if (staticSite) url = `/data/graph-playersOnline.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayerbaseDevelopmentGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayerbaseDevelopmentGraphServer(timestamp, identifier);
    } else {
        return await fetchPlayerbaseDevelopmentGraphNetwork(timestamp);
    }
}

const fetchPlayerbaseDevelopmentGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=activity&server=${identifier}`;
    if (staticSite) url = `/data/graph-activity_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchPlayerbaseDevelopmentGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=activity`;
    if (staticSite) url = `/data/graph-activity.json`;
    return doGetRequest(url, timestamp);
}

export const fetchDayByDayGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchDayByDayGraphServer(timestamp, identifier);
    } else {
        return await fetchDayByDayGraphNetwork(timestamp);
    }
}

const fetchDayByDayGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=uniqueAndNew&server=${identifier}`;
    if (staticSite) url = `/data/graph-uniqueAndNew_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchDayByDayGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=uniqueAndNew`;
    if (staticSite) url = `/data/graph-uniqueAndNew.json`;
    return doGetRequest(url, timestamp);
}

export const fetchHourByHourGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchHourByHourGraphServer(timestamp, identifier);
    } else {
        return await fetchHourByHourGraphNetwork(timestamp);
    }
}

const fetchHourByHourGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=hourlyUniqueAndNew&server=${identifier}`;
    if (staticSite) url = `/data/graph-hourlyUniqueAndNew_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchHourByHourGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=hourlyUniqueAndNew`;
    if (staticSite) url = `/data/graph-hourlyUniqueAndNew.json`;
    return doGetRequest(url, timestamp);
}

export const fetchServerCalendarGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=serverCalendar&server=${identifier}`;
    if (staticSite) url = `/data/graph-serverCalendar_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPunchCardGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=punchCard&server=${identifier}`;
    if (staticSite) url = `/data/graph-punchCard_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchWorldPie = async (timestamp, identifier) => {
    let url = `/v1/graph?type=worldPie&server=${identifier}`;
    if (staticSite) url = `/data/graph-worldPie_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchGeolocations = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchGeolocationsServer(timestamp, identifier);
    } else {
        return await fetchGeolocationsNetwork(timestamp);
    }
}

const fetchGeolocationsServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=geolocation&server=${identifier}`;
    if (staticSite) url = `/data/graph-geolocation_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchGeolocationsNetwork = async (timestamp) => {
    let url = `/v1/graph?type=geolocation`;
    if (staticSite) url = `/data/graph-geolocation.json`;
    return doGetRequest(url, timestamp);
}

export const fetchOptimizedPerformance = async (timestamp, identifier) => {
    let url = `/v1/graph?type=optimizedPerformance&server=${identifier}`;
    if (staticSite) url = `/data/graph-optimizedPerformance_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPingGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=aggregatedPing&server=${identifier}`;
    if (staticSite) url = `/data/graph-aggregatedPing_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

export const fetchJoinAddressPie = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchJoinAddressPieServer(timestamp, identifier);
    } else {
        return await fetchJoinAddressPieNetwork(timestamp);
    }
}

const fetchJoinAddressPieServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=joinAddressPie&server=${identifier}`;
    if (staticSite) url = `/data/graph-joinAddressPie_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchJoinAddressPieNetwork = async (timestamp) => {
    let url = `/v1/graph?type=joinAddressPie`;
    if (staticSite) url = `/data/graph-joinAddressPie.json`;
    return doGetRequest(url, timestamp);
}

export const fetchJoinAddressByDay = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchJoinAddressByDayServer(timestamp, identifier);
    } else {
        return await fetchJoinAddressByDayNetwork(timestamp);
    }
}

const fetchJoinAddressByDayServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=joinAddressByDay&server=${identifier}`;
    if (staticSite) url = `/data/graph-joinAddressByDay_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchJoinAddressByDayNetwork = async (timestamp) => {
    let url = `/v1/graph?type=joinAddressByDay`;
    if (staticSite) url = `/data/graph-joinAddressByDay.json`;
    return doGetRequest(url, timestamp);
}

export const fetchRetentionData = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchServerRetentionData(timestamp, identifier);
    } else {
        return await fetchNetworkRetentionData(timestamp);
    }
}

const fetchServerRetentionData = async (timestamp, identifier) => {
    let url = `/v1/retention?server=${identifier}`;
    if (staticSite) url = `/data/retention_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchNetworkRetentionData = async (timestamp) => {
    let url = `/v1/retention`;
    if (staticSite) url = `/data/retention.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayerJoinAddresses = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchServerPlayerJoinAddresses(timestamp, identifier);
    } else {
        return await fetchNetworkPlayerJoinAddresses(timestamp);
    }
}

const fetchServerPlayerJoinAddresses = async (timestamp, identifier) => {
    let url = `/v1/joinAddresses?server=${identifier}`;
    if (staticSite) url = `/data/joinAddresses_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchNetworkPlayerJoinAddresses = async (timestamp) => {
    let url = `/v1/joinAddresses`;
    if (staticSite) url = `/data/joinAddresses.json`;
    return doGetRequest(url, timestamp);
}