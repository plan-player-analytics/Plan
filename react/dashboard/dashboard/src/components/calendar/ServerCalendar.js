import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faHandPointer} from "@fortawesome/free-regular-svg-icons";

const ServerCalendar = ({series, firstDay, onSelect}) => {
    const {t} = useTranslation();

    const explainerStyle = {
        position: "absolute",
        top: "0.5rem",
        right: "1rem"
    };

    return (
        <div id={'server-calendar'}>
            <p style={explainerStyle}><FontAwesomeIcon icon={faHandPointer}/> {t('html.text.clickAndDrag')}</p>
            <FullCalendar
                plugins={[interactionPlugin, dayGridPlugin]}
                timeZone="UTC"
                themeSystem='bootstrap'
                eventColor='#2196F3'
                // dayMaxEventRows={4}
                firstDay={firstDay}
                initialView='dayGridMonth'
                navLinks={true}
                height={800}
                contentHeight={800}
                headerToolbar={{
                    left: 'title',
                    center: '',
                    right: 'dayGridMonth dayGridWeek dayGridDay today prev next'
                }}
                editable={Boolean(onSelect)}
                selectable={Boolean(onSelect)}
                select={onSelect}
                unselectAuto={true}
                events={(_fetchInfo, successCallback) => successCallback(series)}
            />
        </div>
    )
}

export default ServerCalendar