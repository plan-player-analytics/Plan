import React, {useCallback, useMemo} from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import {useTranslation} from "react-i18next";
import {localeService} from "../../service/localeService.js";
import {useTimeAmountFormatter} from "../../util/format/useTimeAmountFormatter.js";

const ServerCalendar = ({series, firstDay, onSelect, height}) => {
    const {t} = useTranslation();
    const {formatTime} = useTimeAmountFormatter();

    const formatTitle = useCallback(entry => {
        switch (entry.title) {
            case 'html.label.playtime':
                return t(entry.title) + ": " + formatTime(entry.value)
            case 'html.calendar.unique':
            case 'html.calendar.new':
                return t(entry.title) + " " + entry.value
            default:
                return t(entry.title) + ": " + entry.value;
        }
    }, [formatTime, t]);

    const actualSeries = useMemo(() => series.map(entry => {
        return {
            title: formatTitle(entry),
            start: entry.start,
            end: entry.end,
            color: entry.color
        }
    }), [series, formatTitle]);

    const buttonText = {
        today: t('plugin.generic.today').toLowerCase().replaceAll("'", ''),
        month: t('html.label.time.month').toLowerCase(),
        week: t('html.label.time.week').toLowerCase(),
        day: t('html.label.time.day').toLowerCase(),
    };

    return (
        <div id={'server-calendar'}>
            <FullCalendar
                locale={localeService.getIntlFriendlyLocale()}
                plugins={[interactionPlugin, dayGridPlugin]}
                timeZone="UTC"
                themeSystem='bootstrap'
                eventColor='#2196F3'
                // dayMaxEventRows={4}
                firstDay={firstDay}
                initialView='dayGridMonth'
                navLinks={true}
                height={height || 800}
                contentHeight={height || 800}
                headerToolbar={{
                    left: 'title',
                    center: '',
                    right: 'dayGridMonth dayGridWeek dayGridDay today prev next'
                }}
                buttonText={buttonText}
                editable={false}
                selectable={Boolean(onSelect)}
                select={onSelect}
                unselectAuto={true}
                events={(_fetchInfo, successCallback) => successCallback(actualSeries)}
            />
        </div>
    )
}

export default ServerCalendar