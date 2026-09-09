import React, {useCallback, useMemo} from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import {useTranslation} from "react-i18next";
import {localeService} from "../../service/localeService.js";
import {useTimeAmountFormatter} from "../../util/format/useTimeAmountFormatter.js";
import {useDateFormatter} from "../../util/format/useDateFormatter.js";
import {useMetadata} from "../../hooks/metadataHook.tsx";
import {useGenericFilter} from "../../dataHooks/genericFilterContextHook.tsx";
import {staticSite} from "../../service/backendConfiguration.js";

const PlayerSessionCalendar = ({series, firstDay}) => {
    const {t} = useTranslation();

    const {timeZoneOffsetMinutes} = useMetadata();
    const {setAfter, setBefore} = useGenericFilter();
    const onSelect = useCallback(async (selectionInfo) => {
        if (staticSite) return;
        setAfter(selectionInfo.start.getTime() - timeZoneOffsetMinutes * 60000);
        setBefore(selectionInfo.end.getTime() - timeZoneOffsetMinutes * 60000);
    }, [setAfter, setBefore]);

    const {formatTime} = useTimeAmountFormatter();
    const {formatDate} = useDateFormatter();

    const formatTitle = useCallback(entry => {
        switch (entry.title) {
            case 'html.label.session':
                return formatTime(entry.value) + ' ' + t(entry.title);
            case 'html.label.playtime':
                return t(entry.title) + ": " + formatTime(entry.value)
            case 'html.label.registered':
                return t(entry.title) + ": " + formatDate(entry.value)
            default:
                return t(entry.title) + ": " + entry.value;
        }
    }, [formatTime, formatDate])

    const actualSeries = useMemo(() => series.map(entry => {
        return {
            title: formatTitle(entry),
            start: entry.start,
            end: entry.end,
            color: entry.color
        }
    }), [formatTitle]);

    const buttonText = {
        today: t('plugin.generic.today').toLowerCase().replaceAll("'", ''),
        month: t('html.label.time.month').toLowerCase(),
        week: t('html.label.time.week').toLowerCase(),
        day: t('html.label.time.day').toLowerCase(),
    };

    return (
        <FullCalendar
            locale={localeService.getIntlFriendlyLocale()}
            plugins={[interactionPlugin, dayGridPlugin]}
            timeZone="UTC"
            themeSystem='bootstrap'
            eventColor='#009688'
            dayMaxEventRows={4}
            firstDay={firstDay}
            initialView='dayGridMonth'
            navLinks={true}
            height={560}
            contentHeight={560}
            headerToolbar={{
                left: 'title',
                center: '',
                right: 'dayGridMonth dayGridWeek dayGridDay today prev next'
            }}
            editable={false}
            selectable={Boolean(onSelect)}
            select={onSelect}
            unselectAuto={true}
            buttonText={buttonText}
            events={(_fetchInfo, successCallback) => successCallback(actualSeries)}
        />
    )
}

export default PlayerSessionCalendar