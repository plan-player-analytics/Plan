import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import Datapoint from "../../../Datapoint";
import {faUserClock, faUserGroup} from "@fortawesome/free-solid-svg-icons";
import SmallTrend from "../../../trend/SmallTrend";
import {faCalendar, faCalendarPlus} from "@fortawesome/free-regular-svg-icons";
import ComparingLabel from "../../../trend/ComparingLabel";
import End from "../../../layout/End";
import {CardLoader} from "../../../navigation/Loader";
import FormattedTime from "../../../text/FormattedTime.jsx";

const OnlineActivityInsightsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <CardLoader/>;

    return (
        <InsightsFor30DaysCard id={'online-activity-insights'}>
            <Datapoint name={t('html.label.onlineOnFirstJoin')} icon={faUserGroup} color="players-new"
                       value={data.players_first_join_avg}
                       trend={<SmallTrend trend={data.players_first_join_avg_trend}/>}/>
            <Datapoint name={t('html.label.firstSessionLength.average')} icon={faUserClock} color="players-new"
                       value={<FormattedTime timeMs={data.first_session_length_avg}/>}
                       trend={<SmallTrend trend={<FormattedTime timeMs={data.first_session_length_avg_trend}/>}/>}/>
            <Datapoint name={t('html.label.firstSessionLength.median')} icon={faUserClock} color="players-new"
                       value={<FormattedTime timeMs={data.first_session_length_median}/>}
                       trend={<SmallTrend trend={<FormattedTime timeMs={data.first_session_length_median_trend}/>}/>}/>
            <Datapoint name={t('html.label.loneJoins')} icon={faCalendar} color="sessions"
                       value={data.lone_joins}
                       trend={<SmallTrend trend={data.lone_joins_trend}/>}/>
            <Datapoint name={t('html.label.loneNewbieJoins')} icon={faCalendarPlus} color="sessions"
                       value={data.lone_new_joins}
                       trend={<SmallTrend trend={data.lone_new_joins_trend}/>}/>
            <End>
                <ComparingLabel>{t('html.text.comparing15days')}</ComparingLabel>
            </End>
        </InsightsFor30DaysCard>
    )
}

export default OnlineActivityInsightsCard;