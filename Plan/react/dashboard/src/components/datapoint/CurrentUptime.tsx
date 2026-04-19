import React from 'react';
import {useTranslation} from "react-i18next";
import {faPowerOff} from "@fortawesome/free-solid-svg-icons";
import {GenericFilter} from "../../dataHooks/model/GenericFilter";
import {QueryDatapoint} from "./QueryDatapoint";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint";

type Props = {
    filter: GenericFilter;
}

const CurrentUptime = ({filter}: Props) => {
    const {t} = useTranslation();

    return (
        <QueryDatapoint dataType={DatapointType.CURRENT_UPTIME} icon={faPowerOff} color={'uptime'}
                        name={t('html.label.currentUptime')} filter={filter}
                        fallbackUnavailableExplanation={t('html.description.noUptimeCalculation')}/>
    )
};

export default CurrentUptime