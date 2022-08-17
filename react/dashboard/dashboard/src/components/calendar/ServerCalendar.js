import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'

const ServerCalendar = ({series, firstDay}) => {
    return (
        <FullCalendar
            plugins={[dayGridPlugin]}
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
            events={(_fetchInfo, successCallback) => successCallback(series)}
        />
    )
}

export default ServerCalendar