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

export const fetchPlayersTable = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayersTableServer(timestamp, identifier);
    } else {
        return await fetchPlayersTableNetwork(timestamp);
    }
}

const fetchPlayersTableServer = async (timestamp, identifier) => {
    let url = `/v1/playersTable?server=${identifier}`;
    if (staticSite) url = `/data/playersTable-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchPlayersTableNetwork = async (timestamp) => {
    let url = `/v1/playersTable`;
    if (staticSite) url = `/data/playersTable.json`;
    return doGetRequest(url, timestamp);
}

export const fetchAllowlistBounces = async (timestamp, identifier) => {
    let url = `/v1/gameAllowlistBounces?server=${identifier}`;
    if (staticSite) url = `/data/gameAllowlistBounces-${identifier}.json`;
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
    let url = `/v1/graph?type=playersOnlineProxies`;
    if (staticSite) url = `/data/graph-playersOnlineProxies.json`;
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

export const fetchNetworkCalendarGraph = async (timestamp) => {
    let url = `/v1/graph?type=serverCalendar`;
    if (staticSite) url = `/data/graph-serverCalendar.json`;
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

export const fetchJoinAddressByDay = async (timestamp, addresses, identifier) => {
    if (identifier) {
        return await fetchJoinAddressByDayServer(timestamp, addresses, identifier);
    } else {
        return await fetchJoinAddressByDayNetwork(timestamp, addresses);
    }
}

const fetchJoinAddressByDayServer = async (timestamp, addresses, identifier) => {
    let url = `/v1/graph?type=joinAddressByDay&server=${identifier}&addresses=${addresses.join(',')}`;
    if (staticSite) url = `/data/graph-joinAddressByDay_${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchJoinAddressByDayNetwork = async (timestamp, addresses) => {
    let url = `/v1/graph?type=joinAddressByDay&addresses=${addresses.join(',')}`;
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
    if (staticSite) url = `/data/retention-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchNetworkRetentionData = async (timestamp) => {
    let url = `/v1/retention`;
    if (staticSite) url = `/data/retention.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPlayerJoinAddresses = async (timestamp, identifier, justList) => {
    if (identifier) {
        return await fetchServerPlayerJoinAddresses(timestamp, identifier, justList);
    } else {
        return await fetchNetworkPlayerJoinAddresses(timestamp, justList);
    }
}

const fetchServerPlayerJoinAddresses = async (timestamp, identifier, justList) => {
    let url = `/v1/joinAddresses?server=${identifier}${justList ? "&listOnly=true" : ""}`;
    if (staticSite) url = `/data/joinAddresses-${identifier}.json`;
    return doGetRequest(url, timestamp);
}

const fetchNetworkPlayerJoinAddresses = async (timestamp, justList) => {
    let url = `/v1/joinAddresses${justList ? "?listOnly=true" : ""}`;
    if (staticSite) url = `/data/joinAddresses.json`;
    return doGetRequest(url, timestamp);
}

export const fetchPluginHistory = async (timestamp, identifier) => {
    let url = `/v1/pluginHistory?server=${identifier}`;
    if (staticSite) url = `/data/pluginHistory-${identifier}.json`;
    return doGetRequest(url, timestamp);
}