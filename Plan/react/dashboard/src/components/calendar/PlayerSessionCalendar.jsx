import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import {useTranslation} from "react-i18next";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat.js";
import {formatDateWithPreferences, useDatePreferences} from "../text/FormattedDate.jsx";
import {useTimePreferences} from "../text/FormattedTime.jsx";
import {localeService} from "../../service/localeService.js";

const PlayerSessionCalendar = ({series, firstDay}) => {
    const {t} = useTranslation();

    const timePreferences = useTimePreferences();
    const datePreferences = useDatePreferences();

    const formatTitle = entry => {
        switch (entry.title) {
            case 'html.label.session':
                return formatTimeAmount(timePreferences, entry.value) + ' ' + t(entry.title);
            case 'html.label.playtime':
                return t(entry.title) + ": " + formatTimeAmount(timePreferences, entry.value)
            case 'html.label.registered':
                return t(entry.title) + ": " + formatDateWithPreferences(datePreferences, entry.value)
            default:
                return t(entry.title) + ": " + entry.value;
        }
    }

    const actualSeries = series.map(entry => {
        return {
            title: formatTitle(entry),
            start: entry.start,
            end: entry.end,
            color: entry.color
        }
    });

    const buttonText = {
        today: t('plugin.generic.today').toLowerCase().replaceAll("'", ''),
        month: t('html.label.time.month').toLowerCase(),
        week: t('html.label.time.week').toLowerCase(),
        day: t('html.label.time.day').toLowerCase(),
    };

    return (
        <FullCalendar
            locale={localeService.getIntlFriendlyLocale()}
            plugins={[dayGridPlugin]}
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
            buttonText={buttonText}
            events={(_fetchInfo, successCallback) => successCallback(actualSeries)}
        />
    )
}

export default PlayerSessionCalendar