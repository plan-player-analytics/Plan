import React, {useCallback} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {
    faAngleRight,
    faCrosshairs,
    faKhanda,
    faServer,
    faSkull,
    faSkullCrossbones
} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import Scrollable from "../Scrollable";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import FormattedDate from "../text/FormattedDate.jsx";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import DataTablesTable from "./DataTablesTable.jsx";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat.js";
import {useTimePreferences} from "../text/FormattedTime.jsx";

const VictimName = ({kill}) => {
    const {t} = useTranslation();
    const timePreferences = useTimePreferences();

    const day = 24 * 60 * 60 * 1000;
    if (kill.timeSinceRegisterMillis > 0 && kill.timeSinceRegisterMillis < day) {
        return <span className={"col-first-seen"}
                     title={t('html.label.playerKillsVictimIndicator').replace("<>",
                         formatTimeAmount(timePreferences, kill.timeSinceRegisterMillis))}>{kill.victimName}</span>
    }

    return <>{kill.victimName}</>
}

const KillRow = ({kill}) => {
    const killSeparator = <Fa
        icon={kill.killerUUID === kill.victimUUID ? faSkullCrossbones : faAngleRight}
        className={"col-player-kills"}/>;
    return (
        <tr>
            <td><FormattedDate date={kill.date} react/></td>
            <td>{kill.killerName} {killSeparator} <VictimName kill={kill}/></td>
            <td>{kill.weapon}</td>
            <td>{kill.serverName}</td>
        </tr>
    );
}

const KillsTable = ({kills, deaths}) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();

    const columns = [{
        title: <><Fa icon={faCalendar}/> {t('html.label.time.date')}</>,
        data: {_: "date", display: "dateFormatted"}
    }, {
        title: <><Fa
            icon={deaths ? faSkull : faCrosshairs}/> {t(deaths ? 'html.label.deaths' : 'html.label.playerKills')}</>,
        data: {_: "killText", display: "kill"}
    }, {
        title: <><Fa icon={faKhanda}/> {t('html.label.weapon')}</>,
        data: "weapon"
    }, {
        title: <><Fa icon={faServer}/> {t('html.label.server')}</>,
        data: "server"
    }];

    const rows = kills.map(kill => {
        const killSeparator = <Fa
            icon={kill.killerUUID === kill.victimUUID ? faSkullCrossbones : faAngleRight}
            className={"col-player-kills"}/>;
        return {
            killText: kill.killerName + ' > ' + kill.victimName,
            kill: <>{kill.killerName} {killSeparator} <VictimName kill={kill}/></>,
            date: kill.date,
            dateFormatted: <FormattedDate date={kill.date} react/>,
            weapon: kill.weapon,
            server: kill.serverName
        };
    });
    const options = {
        responsive: true,
        deferRender: true,
        columns: columns,
        data: rows,
        paginationCount: 1,
        order: [[0, "desc"]]
    }

    const rowKeyFunction = useCallback((row, column) => {
        return row.killText + row.date + "-" + (column ? JSON.stringify(column.data) : '');
    }, []);

    if (!preferencesLoaded) return <></>;

    return (
        <DataTablesTable id={"kills-table"} options={options} colorClass={"bg-player-kills"}
                         rowKeyFunction={rowKeyFunction}/>
    )
};

export const SimpleKillsTable = ({kills}) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();

    if (!preferencesLoaded) return <></>;

    return (
        <Scrollable>
            <table className={"table mb-0"}>
                <tbody>
                {kills.length ? kills.map(kill => <KillRow key={JSON.stringify(kill)} kill={kill}/>) :
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
}

export default KillsTable;