import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faExchangeAlt, faSkull, faUsers} from "@fortawesome/free-solid-svg-icons";
import ComparisonTable from "../../../table/ComparisonTable";
import BigTrend from "../../../trend/BigTrend";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import {TableRow} from "../../../table/TableRow";
import {CardLoader} from "../../../navigation/Loader";
import FormattedDay from "../../../text/FormattedDay.jsx";
import FormattedTime, {formatTimeFunction} from "../../../text/FormattedTime.jsx";

const ServerWeekComparisonCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <CardLoader/>;
    return (
        <Card id={"week-comparison"}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faExchangeAlt}/> {t('html.label.weekComparison')}
                </h6>
            </Card.Header>
            <ComparisonTable comparisonHeader={t('html.label.comparing7days')}
                             headers={[
                                 <><FormattedDay date={data.start}/> - <FormattedDay date={data.midpoint}/></>,
                                 <><FormattedDay date={data.midpoint}/> - <FormattedDay date={data.end}/></>,
                                 t('html.label.trend')]}>
                <TableRow icon={faUsers} color="players-unique"
                          text={t('html.label.uniquePlayers')}
                          values={[
                              data.unique_before,
                              data.unique_after,
                              <BigTrend key={JSON.stringify(data.unique_trend)}
                                        trend={data.unique_trend}/>
                          ]}/>
                <TableRow icon={faUsers} color="players-new" text={t('html.label.newPlayers')}
                          values={[data.new_before, data.new_after,
                              <BigTrend key={JSON.stringify(data.new_trend)}
                                        trend={data.new_trend}/>]}/>
                <TableRow icon={faUsers} color="lime" text={t('html.label.regularPlayers')}
                          values={[data.regular_before, data.regular_after,
                              <BigTrend key={JSON.stringify(data.regular_trend)}
                                        trend={data.regular_trend}/>]}/>
                <TableRow icon={faClock} color="playtime"
                          text={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                          values={[
                              <FormattedTime key={"before-ms"} timeMs={data.average_playtime_before}/>,
                              <FormattedTime key={"after-ms"} timeMs={data.average_playtime_after}/>,
                              <BigTrend key={JSON.stringify(data.average_playtime_trend)}
                                        trend={data.average_playtime_trend}
                                        format={formatTimeFunction}/>]}/>
                {data.session_length_average_before !== undefined && <TableRow
                    icon={faClock} color="sessions"
                    text={t('html.label.averageSessionLength')}
                    values={[<FormattedTime key={"before-ms"} timeMs={data.session_length_average_before}/>,
                        <FormattedTime key={"after-ms"} timeMs={data.session_length_average_after}/>,
                        <BigTrend key={JSON.stringify(data.session_length_average_trend)}
                                  trend={data.session_length_average_trend}
                                  format={formatTimeFunction}/>]}/>}
                <TableRow icon={faCalendarCheck} color="sessions" text={t('html.label.sessions')}
                          values={[data.sessions_before, data.sessions_after,
                              <BigTrend key={JSON.stringify(data.sessions_trend)}
                                        trend={data.sessions_trend}/>]}/>
                <TableRow icon={faCrosshairs} color="player-kills" text={t('html.label.playerKills')}
                          values={[data.player_kills_before, data.player_kills_after,
                              <BigTrend key={JSON.stringify(data.player_kills_trend)}
                                        trend={data.player_kills_trend}/>]}/>
                <TableRow icon={faCrosshairs} color="mob-kills" text={t('html.label.mobKills')}
                          values={[data.mob_kills_before, data.mob_kills_after,
                              <BigTrend key={JSON.stringify(data.mob_kills_trend)}
                                        trend={data.mob_kills_trend}/>]}/>
                <TableRow icon={faSkull} color="deaths" text={t('html.label.deaths')}
                          values={[data.deaths_before, data.deaths_after,
                              <BigTrend key={JSON.stringify(data.deaths_trend)}
                                        trend={data.deaths_trend}/>]}/>
            </ComparisonTable>
        </Card>
    )
}

export default ServerWeekComparisonCard