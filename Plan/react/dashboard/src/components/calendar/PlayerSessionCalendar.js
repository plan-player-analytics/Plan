import React from "react";
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'

const PlayerSessionCalendar = ({series, firstDay}) => (
    <FullCalendar
        plugins={[dayGridPlugin]}
        timeZone="UTC"
        themeSystem='bootstrap' // TODO Fix styling and size of the calendar
        eventColor='#009688'
        dayMaxEventRows={4}
        firstDay={firstDay}
        initialView='dayGridMonth'
        navLinks={true}
        height={510}
        contentHeight={510}
        headerToolbar={{
            left: 'title',
            center: '',
            right: 'dayGridMonth dayGridWeek dayGridDay today prev next'
        }}
        events={(fetchInfo, successCallback, failCallback) => successCallback(series)}
        eventDidMount={(info) => {
            // TODO popover
            /*{
                content: info.event.title,
                trigger: 'hover',
                placement: 'top',
                container: 'body'
            }*/
        }}
    >
        <span className="loader"/>
    </FullCalendar>
)

export default PlayerSessionCalendar