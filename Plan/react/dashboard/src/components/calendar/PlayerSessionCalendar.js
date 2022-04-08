import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'

const PlayerSessionCalendar = ({series, firstDay}) => {
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
            events={(_fetchInfo, successCallback) => successCallback(series)}
        />
    )
}

export default PlayerSessionCalendar