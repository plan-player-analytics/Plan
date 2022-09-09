import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCaretSquareRight, faLineChart, faLink, faServer, faUser, faUsers} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import Scrollable from "../Scrollable";
import {NavLink} from "react-router-dom";
import {calculateSum} from "../../util/calculation";

const ServerRow = ({server, onQuickView}) => {
    const {t} = useTranslation();
    return (
        <tr>
            <td>{server.name}</td>
            <td className="p-1">
                <NavLink to={"/server/" + encodeURIComponent(server.name)}
                         className={'btn bg-transparent col-light-green'}><Fa
                    icon={faLink}/> {t('html.label.serverAnalysis')}
                </NavLink>
            </td>
            <td>{server.players}</td>
            <td>{server.online}</td>
            <td className="p-1">
                <button className={'btn bg-light-blue float-right'}
                        title={t('html.label.quickView') + ': ' + server.name}
                        onClick={onQuickView}
                >
                    <Fa icon={faCaretSquareRight}/>
                </button>
            </td>
        </tr>
    );
}

const sortBySometimesNumericProperty = (propertyName) => (a, b) => {
    if (typeof (a[propertyName]) === 'number' && typeof (b[propertyName]) === 'number') return a[propertyName] - b[propertyName];
    if (typeof (a[propertyName]) === 'number') return 1;
    if (typeof (b[propertyName]) === 'number') return -1;
    return 0;
}
const sortByNumericProperty = (propertyName) => (a, b) => b[propertyName] - a[propertyName]; // Biggest first
const sortBeforeReverse = (servers, sortBy) => {
    const sorting = [...servers];
    switch (sortBy) {
        case ServerSortOption.PLAYERS_ONLINE:
            return sorting.sort(sortBySometimesNumericProperty('online'));
        case ServerSortOption.AVERAGE_TPS:
            return sorting.sort(sortBySometimesNumericProperty('avg_tps'));
        case ServerSortOption.UNIQUE_PLAYERS:
            return sorting.sort(sortByNumericProperty('unique_players'));
        case ServerSortOption.NEW_PLAYERS:
            return sorting.sort(sortByNumericProperty('new_players'));
        case ServerSortOption.REGISTERED_PLAYERS:
            return sorting.sort(sortByNumericProperty('players'));
        // case ServerSortOption.DOWNTIME:
        //     return servers.sort(sortByNumericProperty('downtime_raw'));
        case ServerSortOption.ALPHABETICAL:
        default:
            return sorting;
    }
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

export const ServerSortOption = {
    ALPHABETICAL: 'html.label.alphabetical',
    AVERAGE_TPS: 'html.label.averageTps7days',
    // DOWNTIME: 'html.label.downtime',
    LOW_TPS_SPIKES: 'html.label.lowTpsSpikes7days',
    NEW_PLAYERS: 'html.label.newPlayers7days',
    PLAYERS_ONLINE: 'html.label.playersOnlineNow',
    REGISTERED_PLAYERS: 'html.label.registeredPlayers',
    UNIQUE_PLAYERS: 'html.label.uniquePlayers7days',
}

const ServersTable = ({servers, onSelect, sortBy, sortReversed}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const sortedServers = sort(servers, sortBy, sortReversed);

    return (
        <Scrollable>
            <table className={"table mb-0 table-striped" + (nightModeEnabled ? " table-dark" : '')}>
                <thead>
                <tr>
                    <th><Fa icon={faServer}/> {t('html.label.server')}</th>
                    <th><Fa icon={faLineChart}/> {t('html.label.serverAnalysis')}</th>
                    <th><Fa icon={faUsers}/> {t('html.label.registeredPlayers')}</th>
                    <th><Fa icon={faUser}/> {t('html.label.playersOnline')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {sortedServers.length ? sortedServers.map((server, i) => <ServerRow key={i} server={server}
                                                                                    onQuickView={() => onSelect(servers.indexOf(server))}/>) :
                    <tr>
                        <td>{t('html.generic.none')}</td>
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>}
                </tbody>
                {sortedServers.length && <tfoot>
                <tr>
                    <td><b>{t('html.label.total')}</b></td>
                    <td></td>
                    <td>{calculateSum(servers.map(s => s.players))}</td>
                    <td>{calculateSum(servers.map(s => s.online))}</td>
                </tr>
                </tfoot>}
            </table>
        </Scrollable>
    )
};

export default ServersTable;