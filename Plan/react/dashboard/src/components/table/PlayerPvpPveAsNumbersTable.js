import {useTranslation} from "react-i18next";
import AsNumbersTable, {TableRow} from "./AsNumbersTable";
import {faCrosshairs, faSkull} from "@fortawesome/free-solid-svg-icons";
import React from "react";

const PlayerPvpPveAsNumbersTable = ({killData}) => {
    const {t} = useTranslation();
    return (
        <AsNumbersTable
            headers={[t('html.label.allTime'), t('html.label.last30days'), t('html.label.last7days')]}
        >
            <TableRow icon={faCrosshairs} color="red" text={t('html.label.kdr')} bold
                      values={[killData.player_kdr_total,
                          killData.player_kdr_30d,
                          killData.player_kdr_7d]}/>
            <TableRow icon={faCrosshairs} color="red" text={t('html.label.playerKills')}
                      values={[killData.player_kills_total,
                          killData.player_kills_30d,
                          killData.player_kills_7d]}/>
            <TableRow icon={faSkull} color="red" text={t('html.label.playerDeaths')}
                      values={[killData.player_deaths_total,
                          killData.player_deaths_30d,
                          killData.player_deaths_7d]}/>
            <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKdr')} bold
                      values={[killData.mob_kdr_total,
                          killData.mob_kdr_30d,
                          killData.mob_kdr_7d]}/>
            <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKills')}
                      values={[killData.mob_kills_total,
                          killData.mob_kills_30d,
                          killData.mob_kills_7d]}/>
            <TableRow icon={faSkull} color="green" text={t('html.label.mobDeaths')}
                      values={[killData.mob_deaths_total,
                          killData.mob_deaths_30d,
                          killData.mob_deaths_7d]}/>
            <TableRow icon={faSkull} color="black" text={t('html.label.deaths')}
                      values={[killData.deaths_total,
                          killData.deaths_30d,
                          killData.deaths_7d]}/>
        </AsNumbersTable>
    )
}

export default PlayerPvpPveAsNumbersTable;