import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import {faGamepad, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook.tsx";
import {QueryDatapoint, QueryDatapointValue} from "../../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint.ts";
import {TitleWithDates} from "../../../text/TitleWithDates.tsx";

const SessionInsightsCard = ({identifier}) => {
    const {t} = useTranslation();
    const {after, before, server} = useGenericFilter();

    const filter = {
        after: after || Date.now() - 30 * 24 * 60 * 60 * 1000,
        before,
        server
    };

    const title = <TitleWithDates label={'html.label.insights'} fallback={'html.label.insights30days'} after={after}
                                  before={before}/>;

    return (
        <InsightsFor30DaysCard id={'session-insights'} title={title}>
            {identifier && <QueryDatapoint name={t('html.label.mostActiveGamemode')} icon={faGamepad} color="gamemode"
                                           dataType={DatapointType.MOST_ACTIVE_GAME_MODE} filter={filter}/>}
            {identifier &&
                <QueryDatapoint name={t('html.label.serverOccupied')} icon={faUsers} color="sessions" prefix={'~'}
                                dataType={DatapointType.SERVER_OCCUPIED} filter={filter}/>}
            <QueryDatapoint name={t('html.label.playtime')} icon={faClock} color="playtime"
                            dataType={DatapointType.PLAYTIME} filter={filter}/>
            <QueryDatapoint name={t('html.label.afkTime')} icon={faClock} color="playtime-afk"
                            dataType={DatapointType.AFK_TIME} filter={filter}
                            valueLabel={<>(<QueryDatapointValue dataType={DatapointType.AFK_TIME_PERCENTAGE}
                                                                filter={filter}/>)</>}
            />
        </InsightsFor30DaysCard>
    )
}

export default SessionInsightsCard;