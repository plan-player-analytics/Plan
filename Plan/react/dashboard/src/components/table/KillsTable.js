import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faAngleRight, faSkullCrossbones} from "@fortawesome/free-solid-svg-icons";
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import Scrollable from "../Scrollable";

const VictimName = ({kill}) => {
    const {t} = useTranslation();

    const day = 24 * 60 * 60 * 1000;
    if (kill.timeSinceRegisterMillis > 0 && kill.timeSinceRegisterMillis < day) {
        return <span className={"col-light-green"}
                     title={t('html.label.playerKillsVictimIndicator').replace("<>", kill.timeSinceRegisterFormatted)}>{kill.victimName}</span>
    }

    return <>{kill.victimName}</>
}

const KillRow = ({kill}) => {
    const killSeparator = <Fa
        icon={kill.killerUUID === kill.victimUUID ? faSkullCrossbones : faAngleRight}
        className={"col-red"}/>;
    return (
        <tr>
            <td>{kill.date}</td>
            <td>{kill.killerName} {killSeparator} <VictimName kill={kill}/></td>
            <td>{kill.weapon}</td>
            <td>{kill.serverName}</td>
        </tr>
    );
}

const KillsTable = ({kills}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    return (
        <Scrollable>
            <table className={"table mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                <tbody>
                {kills.length ? kills.map((kill, i) => <KillRow key={i} kill={kill}/>) : <tr>
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

export default KillsTable;