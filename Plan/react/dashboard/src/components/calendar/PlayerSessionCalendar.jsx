import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import {useTranslation} from "react-i18next";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat.js";
import {formatDate, useDatePreferences} from "../text/FormattedDate.jsx";
import {useTimePreferences} from "../text/FormattedTime.jsx";

const PlayerSessionCalendar = ({series, firstDay}) => {
    const {t} = useTranslation();

    const timePreferences = useTimePreferences();
    const datePreferences = useDatePreferences();

    const formatDateEasy = date => {
        return formatDate(date, datePreferences.offset, datePreferences.pattern, false, datePreferences.recentDaysPattern, t);
    }

    const formatTitle = entry => {
        switch (entry.title) {
            case 'html.label.session':
                return formatTimeAmount(timePreferences, entry.value) + ' ' + t(entry.title);
            case 'html.label.playtime':
                return t(entry.title) + ": " + formatTimeAmount(timePreferences, entry.value)
            case 'html.label.registered':
                return t(entry.title) + ": " + formatDateEasy(entry.value)
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

    return (
        <FullCalendar
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
            events={(_fetchInfo, successCallback) => successCallback(actualSeries)}
        />
    )
}

export default PlayerSessionCalendar