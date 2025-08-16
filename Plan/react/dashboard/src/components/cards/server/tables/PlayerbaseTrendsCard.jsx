import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faExchangeAlt, faUsers} from "@fortawesome/free-solid-svg-icons";
import ComparisonTable from "../../../table/ComparisonTable";
import BigTrend from "../../../trend/BigTrend";
import React from "react";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import {TableRow} from "../../../table/TableRow";
import {CardLoader} from "../../../navigation/Loader";

const PlayerbaseTrendsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <CardLoader/>;
    return (
        <Card id={"playerbase-trends"}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faExchangeAlt} className="col-players-activity-index"/> {t('html.label.trends30days')}
                </h6>
            </Card.Header>
            <ComparisonTable comparisonHeader={t('html.text.comparing30daysAgo')}
                             headers={[t('html.label.thirtyDaysAgo'), t('html.label.now'), t('html.label.trend')]}>
                <TableRow icon={faUsers} color="players-count" text={t('html.label.totalPlayers')}
                          values={[data.total_players_then, data.total_players_now,
                              <BigTrend key={JSON.stringify(data.total_players_trend)}
                                        trend={data.total_players_trend}/>]}/>
                <TableRow icon={faUsers} color="players-regular" text={t('html.label.regularPlayers')}
                          values={[data.regular_players_then, data.regular_players_now,
                              <BigTrend key={JSON.stringify(data.regular_players_trend)}
                                        trend={data.regular_players_trend}/>]}/>
                <TableRow icon={faClock} color="playtime"
                          text={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                          values={[data.playtime_avg_then, data.playtime_avg_now,
                              <BigTrend key={JSON.stringify(data.total_players_trend)}
                                        trend={data.playtime_avg_trend}/>]}/>
                <TableRow icon={faClock} color="playtime-afk"
                          text={t('html.label.afk') + ' ' + t('html.label.perPlayer')}
                          values={[data.afk_then, data.afk_now,
                              <BigTrend key={JSON.stringify(data.afk_trend)}
                                        trend={data.afk_trend}/>]}/>
                <TableRow icon={faClock} color="playtime"
                          text={t('html.label.averagePlaytime') + ' ' + t('html.label.perRegularPlayer')}
                          values={[data.regular_playtime_avg_then, data.regular_playtime_avg_now,
                              <BigTrend key={JSON.stringify(data.regular_playtime_avg_trend)}
                                        trend={data.regular_playtime_avg_trend}/>]}/>
                <TableRow icon={faClock} color="sessions"
                          text={t('html.label.averageSessionLength') + ' ' + t('html.label.perRegularPlayer')}
                          values={[data.regular_session_avg_then, data.regular_session_avg_now,
                              <BigTrend key={JSON.stringify(data.regular_session_avg_trend)}
                                        trend={data.regular_session_avg_trend}/>]}/>
                <TableRow icon={faClock} color="playtime-afk"
                          text={t('html.label.afk') + ' ' + t('html.label.perRegularPlayer')}
                          values={[data.regular_afk_avg_then, data.regular_afk_avg_now,
                              <BigTrend key={JSON.stringify(data.regular_afk_avg_trend)}
                                        trend={data.regular_afk_avg_trend}/>]}/>
            </ComparisonTable>
        </Card>
    )
}

export default PlayerbaseTrendsCard