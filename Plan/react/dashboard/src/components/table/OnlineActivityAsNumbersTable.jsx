import {useTranslation} from "react-i18next";
import {faUser, faUserCircle, faUserPlus, faUsers} from "@fortawesome/free-solid-svg-icons";
import React, {useCallback} from "react";
import {TableRow} from "./TableRow";
import ComparisonTable from "./ComparisonTable";
import SmallTrend from "../trend/SmallTrend";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendarCheck, faClock, faEye, faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {CardLoader} from "../navigation/Loader";
import {useNavigation} from "../../hooks/navigationHook";

const OnlineActivityAsNumbersTable = ({data}) => {
    const {t} = useTranslation();
    const {setHelpModalTopic} = useNavigation();
    const openHelp = useCallback(() => setHelpModalTopic('new-player-retention'), [setHelpModalTopic]);
    if (!data) return <CardLoader/>;

    return (
        <ComparisonTable
            headers={[t('html.label.last30days'), t('html.label.last7days'), t('html.label.last24hours')]}
            comparisonHeader={t('html.text.comparing15days')}
        >
            <TableRow icon={faUsers} color="players-unique" text={t('html.label.uniquePlayers')}
                      values={[
                          <>{data.unique_players_30d}{' '}<SmallTrend trend={data.unique_players_30d_trend}/></>,
                          data.unique_players_7d,
                          data.unique_players_24h
                      ]}/>
            <TableRow icon={faUser} color="players-unique"
                      text={t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')}
                      values={[
                          <>{data.unique_players_30d_avg}{' '}<SmallTrend
                              trend={data.unique_players_30d_avg_trend}/></>,
                          data.unique_players_7d_avg,
                          data.unique_players_24h_avg
                      ]}/>
            <TableRow icon={faUsers} color="players-new" text={t('html.label.newPlayers')}
                      values={[
                          <>{data.new_players_30d}{' '}<SmallTrend trend={data.new_players_30d_trend}/></>,
                          data.new_players_7d,
                          data.new_players_24h
                      ]}/>
            <TableRow icon={faUserPlus} color="players-new"
                      text={t('html.label.newPlayers') + ' ' + t('html.label.perDay')}
                      values={[
                          <>{data.new_players_30d_avg}{' '}<SmallTrend trend={data.new_players_30d_avg_trend}/></>,
                          data.new_players_7d_avg,
                          data.new_players_24h_avg
                      ]}/>
            <TableRow icon={faUserCircle} color="retention-new-players"
                      text={<>{t('html.label.newPlayerRetention')} <span>
                                <button onClick={openHelp}><Fa className={"col-text"}
                                                               icon={faQuestionCircle}/>
                                </button></span></>}
                      values={[
                          `(${data.new_players_retention_30d}/${data.new_players_30d}) ${data.new_players_retention_30d_perc}`,
                          `(${data.new_players_retention_7d}/${data.new_players_7d}) ${data.new_players_retention_7d_perc}`,
                          <>{`(${data.new_players_retention_24h}/${data.new_players_24h}) ${data.new_players_retention_24h_perc}`}<Fa
                              icon={faEye} title={t('html.description.newPlayerRetention')}/></>
                      ]}/>
            <TableRow icon={faClock} color="playtime"
                      text={t('html.label.playtime')}
                      values={[
                          <>{data.playtime_30d}{' '}<SmallTrend trend={data.playtime_30d_trend}/></>,
                          data.playtime_7d,
                          data.playtime_24h
                      ]}/>
            <TableRow icon={faClock} color="playtime"
                      text={t('html.label.averagePlaytime') + ' ' + t('html.label.perDay')}
                      values={[
                          <>{data.playtime_30d_avg}{' '}<SmallTrend trend={data.playtime_30d_avg_trend}/></>,
                          data.playtime_7d_avg,
                          data.playtime_24h_avg
                      ]}/>
            <TableRow icon={faClock} color="sessions"
                      text={t('html.label.averageSessionLength')}
                      values={[
                          <>{data.session_length_30d_avg}{' '}<SmallTrend
                              trend={data.session_length_30d_avg_trend}/></>,
                          data.session_length_7d_avg,
                          data.session_length_24h_avg
                      ]}/>
            <TableRow icon={faCalendarCheck} color="sessions"
                      text={t('html.label.sessions')}
                      values={[
                          <>{data.sessions_30d}{' '}<SmallTrend trend={data.sessions_30d_trend}/></>,
                          data.sessions_7d,
                          data.sessions_24h
                      ]}/>
        </ComparisonTable>
    )
}

export default OnlineActivityAsNumbersTable;