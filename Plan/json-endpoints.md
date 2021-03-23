# JSON endpoints

This document details what endpoints are available on a Plan webserver and what parameters they require.

What the endpoints return is not detailed to save time, as this document is written during implementation.

Parameters are given in the URL: `address/v1/<endpoint>?parameter=value&another=value`

If invalid parameters are given, the server will return 400 Bad Request.  
The body of the response is the error message

## Endpoints

### `GET /<playername>/raw` `GET /<player UUID>/raw`

Obtain all data in the database for a player.

### Tab endpoints

Endpoints for tab specific number data, processed for display.

Required parameters: `server`

Parameter|Expected value|Description
--|--|--
`server` | Name or UUID of a Plan server | Used for identifying Plan server that the data should be about

#### `GET /v1/serverOverview` - Server Overview tab

#### `GET /v1/onlineOverview` - Online Activity Overview tab.

#### `GET /v1/sessionsOverview` - Sessions tab.

#### `GET /v1/playerVersus` - PvP & PvE tab.

#### `GET /v1/playerbaseOverview` - Playerbase Overview tab.

#### `GET /v1/performanceOverview`

### `GET /v1/players`

Obtain data for `/server` player list.

Required parameters: `server`

Parameter|Expected value|Description
--|--|--
`server` | Name or UUID of a Plan server | Used for identifying Plan server that the data should be about

### `GET /v1/sessions`

Obtain data for `/server` session accordion. Returns configurable amount of sessions.

Required parameters: `server`

Parameter|Expected value|Description
--|--|--
`server` | Name or UUID of a Plan server | Used for identifying Plan server that the data should be about

### `GET /v1/kills`

Obtain data for `/server` kills table. Returns 100 most recent kills.

Required parameters: `server`

Parameter|Expected value|Description
--|--|--
`server` | Name or UUID of a Plan server | Used for identifying Plan server that the data should be about

### `GET /v1/graph`

Obtain data for graphs.

Required parameters: `server` and `type`

Parameter|Expected value|Description
--|--|--
`server` | Name or UUID of a Plan server | Used for identifying Plan server that the data should be about
`type` | `performance`, `uniqueAndNew` | What kind of graph data should be given

Type | Description
-- | --
`performance` | TPS data points for last 6 months: Players Online, TPS, CPU, RAM, Chunks, Entities, Disk Space
`uniqueAndNew` | Player data points for each day, how many unique and how many new players were there each day. Last 180 days
`calendar` | Calendar data points for each day there is data for. Last 2 years.
`worldPie` | World Pie data of all sessions combined
`activity` | Activity stack graph and pie graph data
`geolocation` | World Map and bar graph data. World Map uses iso-a3 specification of Country codes and Bar graph uses country names.
`aggregatedPing` | Min, Avg, Max ping graph data.
`punchCard` | PunchCard graph data. (Scatter plot)

### `GET /v1/player`

Fetch all data for a player page

Required parameters: `player`

Parameter|Expected value|Description
--|--|--
`player` | Name or UUID of a player | Used for identifying player the data should be about
