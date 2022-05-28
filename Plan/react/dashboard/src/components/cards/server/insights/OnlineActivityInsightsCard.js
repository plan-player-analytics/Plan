import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import Datapoint from "../../../Datapoint";
import {faUserClock, faUserGroup} from "@fortawesome/free-solid-svg-icons";
import SmallTrend from "../../../trend/SmallTrend";
import {faCalendar, faCalendarPlus} from "@fortawesome/free-regular-svg-icons";
import ComparingLabel from "../../../trend/ComparingLabel";
import End from "../../../layout/End";

const OnlineActivityInsightsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <></>

    return (
        <InsightsFor30DaysCard>
            <Datapoint name={t('html.label.onlineOnFirstJoin')} icon={faUserGroup} color="light-green"
                       value={data.players_first_join_avg}
                       trend={<SmallTrend trend={data.players_first_join_avg_trend}/>}/>
            <Datapoint name={t('html.label.firstSessionLength.average')} icon={faUserClock} color="light-green"
                       value={data.first_session_length_avg}
                       trend={<SmallTrend trend={data.first_session_length_avg_trend}/>}/>
            <Datapoint name={t('html.label.firstSessionLength.median')} icon={faUserClock} color="light-green"
                       value={data.first_session_length_median}
                       trend={<SmallTrend trend={data.first_session_length_median_trend}/>}/>
            <Datapoint name={t('html.label.loneJoins')} icon={faCalendar} color="teal"
                       value={data.lone_joins}
                       trend={<SmallTrend trend={data.lone_joins_trend}/>}/>
            <Datapoint name={t('html.label.loneNewbieJoins')} icon={faCalendarPlus} color="teal"
                       value={data.lone_new_joins}
                       trend={<SmallTrend trend={data.lone_new_joins_trend}/>}/>
            <End>
                <ComparingLabel>{t('html.text.comparing15days')}</ComparingLabel>
            </End>
        </InsightsFor30DaysCard>
    )
}

export default OnlineActivityInsightsCard;