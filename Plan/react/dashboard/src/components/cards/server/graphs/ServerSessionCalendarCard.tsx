import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchNetworkCalendarGraph, fetchServerCalendarGraph} from "../../../../service/serverService";
import React, {useCallback} from "react";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {ChartLoader} from "../../../navigation/Loader";
import ServerCalendar from "../../../calendar/ServerCalendar";
import {PlanDataResponse} from "../../../../service/PlanResponse";
import {Card} from "react-bootstrap";
import CardHeader from "../../CardHeader";
import {faCalendar, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useTranslation} from "react-i18next";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook";
import {useDateConverter} from "../../../../util/format/useDateConverter";
import {staticSite} from "../../../../service/backendConfiguration";

type CalendarResponse = {
    data: any,
    firstDay: number,
}

type SelectionInfo = {
    start: Date,
    startStr: string,
    end: Date,
    endStr: string
}

export const NetworkSessionCalendarCard = () => {
    const {
        data,
        loadingError
    } = useDataRequest(fetchNetworkCalendarGraph, []) as unknown as PlanDataResponse<CalendarResponse>;

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;
    return (
        <SessionCalendarCard data={data.data} firstDay={data.firstDay}/>
    )
}

type Props = {
    identifier: string;
}

export const ServerSessionCalendarCard = ({identifier}: Props) => {
    const {
        data,
        loadingError
    } = useDataRequest(fetchServerCalendarGraph, [identifier]) as unknown as PlanDataResponse<CalendarResponse>;

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;
    return (
        <SessionCalendarCard data={data.data} firstDay={data.firstDay}/>
    )
}

const SessionCalendarCard = ({data, firstDay}: CalendarResponse) => {
    const {t} = useTranslation();
    const {convert} = useDateConverter();
    const {setTimeframe} = useGenericFilter();
    const onSelect = useCallback(async (selectionInfo: SelectionInfo) => {
        if (staticSite) return;
        setTimeframe(convert(selectionInfo.startStr).toUTCEpochMs(), convert(selectionInfo.endStr).toUTCEpochMs());
    }, [setTimeframe, convert]);

    return <Card>
        <CardHeader icon={faCalendar} color={"sessions"} label={'html.label.sessionCalendar'}>
            {!staticSite && <span className="float-end">
                <FontAwesomeIcon icon={faHandPointer}/>{' '}<small>{t('html.text.clickAndDrag')}</small>
            </span>}
        </CardHeader>
        <ServerCalendar series={data} firstDay={firstDay} onSelect={onSelect} height={600}/>
    </Card>
}
