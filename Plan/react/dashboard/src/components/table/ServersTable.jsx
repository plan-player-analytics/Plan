import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {
    faBoxArchive,
    faCaretSquareRight,
    faExclamationTriangle,
    faLineChart,
    faLink,
    faServer,
    faSortAlphaDown,
    faSortAlphaUp,
    faSortNumericDown,
    faSortNumericUp,
    faUser,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook.tsx";
import {useTranslation} from "react-i18next";
import Scrollable from "../Scrollable";
import {NavLink} from "react-router";
import ActionButton from "../input/button/ActionButton.jsx";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint.ts";
import {
    calculatePermission,
    QueryDatapointValue,
    useDatapointQueries,
    useDatapointQuery
} from "../datapoint/QueryDatapoint.tsx";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../util/format/useDateFormatter.js";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ServerRow = ({server, sortedBy, onQuickView}) => {
    const {t} = useTranslation();

    const {error: error30d} = useDatapointQuery(true, DatapointType.TPS_AVERAGE, {
        server: server.serverUUID,
        afterMillisAgo: MS_MONTH
    })
    const {error: error7d} = useDatapointQuery(true, DatapointType.TPS_AVERAGE, {
        server: server.serverUUID,
        afterMillisAgo: MS_WEEK
    })
    const {error: error24h} = useDatapointQuery(true, DatapointType.TPS_AVERAGE, {
        server: server.serverUUID,
        afterMillisAgo: MS_24H
    })

    let noDataWarning = '';
    if (error30d?.status === 404) {
        noDataWarning = <>&nbsp;<span title={t('html.description.noData30d')}><Fa icon={faBoxArchive}/></span></>
    } else if (error24h?.status === 404) {
        noDataWarning = <>&nbsp;<span title={t('html.description.noData24h')}>
            <Fa icon={faExclamationTriangle} className={error7d?.status === 404 ? '' : "col-deep-orange"}/>
        </span></>
    }

    return (
        <tr>
            <td>{server.serverName}{noDataWarning}</td>
            <td className="p-1">
                <NavLink to={"/server/" + encodeURIComponent(server.serverUUID)}
                         title={t('html.label.serverAnalysis') + ': ' + server.name}
                         className={'btn bg-transparent col-servers'}><Fa
                    icon={faLink}/> {t('html.label.serverAnalysis')}
                </NavLink>
            </td>
            <td>
                <QueryDatapointValue dataType={sortedBy.data || ServerSortOption.REGISTERED_PLAYERS.data}
                                     filter={{
                                         server: server.serverUUID,
                                         afterMillisAgo: sortedBy.noFilterDates ? undefined : MS_WEEK
                                     }}/>
            </td>
            <td>
                <QueryDatapointValue dataType={DatapointType.PLAYERS_ONLINE}
                                     filter={{server: server.serverUUID}}/>
            </td>
            <td className="p-1">
                <ActionButton className={'btn bg-players-online float-right'}
                              title={t('html.label.quickView') + ': ' + server.name}
                              onClick={onQuickView}
                >
                    <Fa icon={faCaretSquareRight}/>
                </ActionButton>
            </td>
        </tr>
    );
}

const sortKeepOrder = () => 0;
const sortBySometimesNumericProperty = (propertyName) => (a, b) => {
    if (typeof (a[propertyName]) === 'number' && typeof (b[propertyName]) === 'number') return a[propertyName] - b[propertyName];
    if (typeof (a[propertyName]) === 'number') return 1;
    if (typeof (b[propertyName]) === 'number') return -1;
    return 0;
}
const sortByNumericProperty = (propertyName) => (a, b) => b[propertyName] - a[propertyName]; // Biggest first
const sortBeforeReverse = (servers, sortBy) => {
    return [...servers].sort(sortBy.sortFunction);
}

const reverse = (array) => {
    const reversedArray = [];
    for (let i = array.length - 1; i >= 0; i--) {
        reversedArray.push(array[i]);
    }
    return reversedArray;
}

const sort = (servers, sortBy, sortReversed) => {
    return sortReversed ? reverse(sortBeforeReverse(servers, sortBy)) : sortBeforeReverse(servers, sortBy);
}

const SortOptionIcon = {
    LETTERS: {
        iconAsc: faSortAlphaDown,
        iconDesc: faSortAlphaUp
    },
    NUMBERS: {
        iconAsc: faSortNumericUp,
        iconDesc: faSortNumericDown
    }
}

export const ServerSortOption = {
    ALPHABETICAL: {
        key: "ALPHABETICAL",
        label: 'html.label.alphabetical',
        data: undefined,
        noFilterDates: true,
        sortFunction: sortKeepOrder,
        ...SortOptionIcon.LETTERS
    },
    AVERAGE_TPS: {
        key: "AVERAGE_TPS",
        label: 'html.label.averageTps7days',
        data: DatapointType.TPS_AVERAGE,
        sortFunction: sortBySometimesNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
    // DOWNTIME: 'html.label.downtime',
    LOW_TPS_SPIKES: {
        key: "LOW_TPS_SPIKES",
        label: 'html.label.lowTpsSpikes7days',
        data: DatapointType.TPS_LOW_SPIKES,
        sortFunction: sortByNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
    NEW_PLAYERS: {
        key: "NEW_PLAYERS",
        label: 'html.label.newPlayers7days',
        data: DatapointType.NEW_PLAYERS,
        sortFunction: sortByNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
    PLAYERS_ONLINE: {
        key: "PLAYERS_ONLINE",
        label: 'html.label.playersOnlineNow',
        data: DatapointType.PLAYERS_ONLINE,
        noFilterDates: true,
        sortFunction: sortBySometimesNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
    REGISTERED_PLAYERS: {
        key: "REGISTERED_PLAYERS",
        label: 'html.label.registeredPlayers',
        data: DatapointType.NEW_PLAYERS,
        noFilterDates: true,
        sortFunction: sortByNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
    UNIQUE_PLAYERS: {
        key: "UNIQUE_PLAYERS",
        label: 'html.label.uniquePlayers7days',
        data: DatapointType.UNIQUE_PLAYERS_COUNT,
        sortFunction: sortByNumericProperty('value'),
        ...SortOptionIcon.NUMBERS
    },
}

const ServersTable = ({servers, onSelect, sortBy, sortReversed}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    const {hasPermission} = useAuth();

    const sortedBy = ServerSortOption[sortBy.key];
    const permission = calculatePermission(sortedBy.data);
    const queries = useDatapointQueries(hasPermission(permission), sortedBy.data || ServerSortOption.REGISTERED_PLAYERS.data,
        servers.map(server => ({
            server: server.serverUUID,
            afterMillisAgo: sortedBy.noFilterDates ? undefined : MS_WEEK
        })))
    const sortedServers = sort(servers.map((s, i) => ({...s, value: queries[i].data?.value})), sortBy, sortReversed);

    return (
        <Scrollable>
            <table className={"table mb-0 table-striped" + (nightModeEnabled ? " table-dark" : '')}>
                <thead>
                <tr>
                    <th><Fa icon={faServer}/> {t('html.label.server')}</th>
                    <th><Fa icon={faLineChart}/> {t('html.label.serverAnalysis')}</th>
                    <th><Fa
                        icon={faUsers}/> {t(sortedBy.data ? sortedBy.label : ServerSortOption.REGISTERED_PLAYERS.label)}
                    </th>
                    <th><Fa icon={faUser}/> {t('html.label.playersOnline')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {sortedServers.length ? sortedServers.map(server => <ServerRow key={server.serverUUID + sortedBy.key}
                                                                               server={server}
                                                                               sortedBy={sortedBy}
                                                                               onQuickView={() => onSelect(servers.findIndex(s => s.serverUUID === server.serverUUID))}/>) :
                    <tr>
                        <td>{t('html.generic.none')}</td>
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>}
                </tbody>
            </table>
        </Scrollable>
    )
};

export default ServersTable;