import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import Datapoint from "../../../Datapoint";
import {useTranslation} from "react-i18next";
import {faHandSparkles} from "@fortawesome/free-solid-svg-icons";
import {CardLoader} from "../../../navigation/Loader";

const PvpPveInsightsCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>;

    return (
        <InsightsFor30DaysCard id={"pvp-pve-insights"}>
            <Datapoint name={t('html.label.deadliestWeapon')} icon={faHandSparkles} color="top-3-first"
                       value={data.weapon_1st}/>
            <Datapoint name={t('html.label.secondDeadliestWeapon')} icon={faHandSparkles} color="top-3-second"
                       value={data.weapon_2nd}/>
            <Datapoint name={t('html.label.thirdDeadliestWeapon')} icon={faHandSparkles} color="top-3-third"
                       value={data.weapon_3rd}/>
        </InsightsFor30DaysCard>
    )
}

export default PvpPveInsightsCard;