import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";

const PerformanceInsightsCard = ({servers}) => {
    const {t} = useTranslation();

    return (
        <InsightsFor30DaysCard id={"performance-insights"}>

        </InsightsFor30DaysCard>
    )
}

export default PerformanceInsightsCard;