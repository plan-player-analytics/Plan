import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import Datapoint from "../../../Datapoint";
import {faLongArrowAltRight, faUser} from "@fortawesome/free-solid-svg-icons";
import SmallTrend from "../../../trend/SmallTrend";
import End from "../../../layout/End";
import ComparingLabel from "../../../trend/ComparingLabel";
import {CardLoader} from "../../../navigation/Loader.tsx";

const TwoPlayerChange = ({colorBefore, labelBefore, colorAfter, labelAfter}) => {
    return (
        <>
            <Fa icon={faUser} className={`col-${colorBefore}`}/>{' '}{labelBefore}
            {' '}<Fa icon={faLongArrowAltRight}/>{' '}
            <Fa icon={faUser} className={`col-${colorAfter}`}/>{' '}{labelAfter}
        </>
    )
}

const PlayerbaseInsightsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <CardLoader/>;
    return (
        <InsightsFor30DaysCard id={"playerbase-insights"}>
            <Datapoint name={<TwoPlayerChange colorBefore={'players-new'}
                                              labelBefore={t('html.label.new')}
                                              colorAfter={'players-regular'}
                                              labelAfter={t('html.label.regular')}/>}
                       value={data.new_to_regular}
                       trend={<SmallTrend trend={data.new_to_regular_trend}/>}
            />
            <Datapoint name={<TwoPlayerChange colorBefore={'players-regular'}
                                              labelBefore={t('html.label.regular')}
                                              colorAfter={'players-inactive'}
                                              labelAfter={t('html.label.inactive')}/>}
                       value={data.regular_to_inactive}
                       trend={<SmallTrend trend={data.regular_to_inactive_trend}/>}
            />
            <End><ComparingLabel>{t('html.text.comparing30daysAgo')}</ComparingLabel></End>
        </InsightsFor30DaysCard>
    )
}

export default PlayerbaseInsightsCard;