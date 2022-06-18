import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import Datapoint from "../../../Datapoint";
import {useTranslation} from "react-i18next";
import {faKhanda} from "@fortawesome/free-solid-svg-icons";

const PvpPveInsightsCard = ({data}) => {
    const {t} = useTranslation();

    return (
        <InsightsFor30DaysCard>
            <Datapoint name={t('html.label.deadliestWeapon')} icon={faKhanda} color="amber"
                       value={data.weapon_1st}/>
            <Datapoint name={t('html.label.secondDeadliestWeapon')} icon={faKhanda} color="gray"
                       value={data.weapon_2nd}/>
            <Datapoint name={t('html.label.thirdDeadliestWeapon')} icon={faKhanda} color="brown"
                       value={data.weapon_3rd}/>
        </InsightsFor30DaysCard>
    )
}

export default PvpPveInsightsCard;