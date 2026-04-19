import React from 'react';
import {useTranslation} from "react-i18next";
import {faPowerOff} from "@fortawesome/free-solid-svg-icons";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import Datapoint, {Datapoint as DatapointComponent} from "./Datapoint";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import FormattedTime from "../text/FormattedTime.jsx";
import {isNumber} from "../../util/isNumber.js";
import {GenericFilter} from "../../dataHooks/model/GenericFilter";
import {useAuth} from "../../hooks/authenticationHook";
import {calculatePermission, useDatapointQuery} from "./QueryDatapoint";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint";
import {DatapointLoader} from "../navigation/Loader";

type Props = {
    filter: GenericFilter;
}

const CurrentUptime = ({filter}: Props) => {
    const {t} = useTranslation();

    const {hasPermission} = useAuth();
    const isAllowed = hasPermission(calculatePermission(DatapointType.CURRENT_UPTIME, undefined, filter))
    const {data, isFetching, error} = useDatapointQuery(isAllowed, DatapointType.CURRENT_UPTIME, filter);

    if (!isAllowed) return null;

    const infoBubble = error?.status === 404
        ? <span title={t('html.description.noUptimeCalculation')}><FontAwesomeIcon icon={faQuestionCircle}/></span>
        : undefined;

    if (error && error.status !== 404) {
        console.error(error);
        return <DatapointComponent
            icon={faPowerOff} color={'uptime'}
            name={t('html.label.currentUptime')}
            value={error.message}
        />
    }

    return (
        <Datapoint icon={faPowerOff} color={'uptime'}
                   name={t('html.label.currentUptime')}
                   value={(data || error?.status === 404) && !isFetching ? (isNumber(data?.value) &&
                       <FormattedTime timeMs={data?.value}/> || 'plugin.generic.unavailable') : <DatapointLoader/>}
                   valueLabel={infoBubble}/>
    )
};

export default CurrentUptime