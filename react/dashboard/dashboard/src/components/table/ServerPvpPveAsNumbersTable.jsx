import {useTranslation} from "react-i18next";
import AsNumbersTable from "./AsNumbersTable";
import {faCrosshairs, faSkull} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {TableRow} from "./TableRow";

const ServerPvpPveAsNumbersTable = ({killData}) => {
    const {t} = useTranslation();

    if (!killData) return <></>

    return (
        <AsNumbersTable
            headers={[t('html.label.allTime'), t('html.label.last30days'), t('html.label.last7days')]}
        >
            <TableRow icon={faCrosshairs} color="player-kills" text={t('html.label.averageKdr')}
                      values={[killData.player_kdr_avg,
                          killData.player_kdr_avg_30d,
                          killData.player_kdr_avg_7d]}/>
            <TableRow icon={faCrosshairs} color="player-kills" text={t('html.label.playerKills')}
                      values={[killData.player_kills_total,
                          killData.player_kills_30d,
                          killData.player_kills_7d]}/>
            <TableRow icon={faCrosshairs} color="mob-kills" text={t('html.label.averageMobKdr')}
                      values={[killData.mob_kdr_total,
                          killData.mob_kdr_30d,
                          killData.mob_kdr_7d]}/>
            <TableRow icon={faCrosshairs} color="mob-kills" text={t('html.label.mobKills')}
                      values={[killData.mob_kills_total,
                          killData.mob_kills_30d,
                          killData.mob_kills_7d]}/>
            <TableRow icon={faSkull} color="mob-kills" text={t('html.label.mobDeaths')}
                      values={[killData.mob_deaths_total,
                          killData.mob_deaths_30d,
                          killData.mob_deaths_7d]}/>
            <TableRow icon={faSkull} color="deaths" text={t('html.label.deaths')}
                      values={[killData.deaths_total,
                          killData.deaths_30d,
                          killData.deaths_7d]}/>
        </AsNumbersTable>
    )
}

export default ServerPvpPveAsNumbersTable;