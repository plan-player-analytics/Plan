function sessionCalendar(id, events, firstDay) {
    $(id).fullCalendar({
        eventColor: '#009688',
        eventLimit: 4,
        firstDay: firstDay,

        eventRender: function (eventObj, $el) {
            $el.popover({
                content: eventObj.title,
                trigger: 'hover',
                placement: 'top',
                container: 'body'
            });
        },

        events: events,

        navLinks: true,
        height: 'parent',
        header: {
            left: 'title',
            center: '',
            right: 'month agendaWeek agendaDay today prev,next'
        }
    });

    setTimeout(function () {
        $(id).fullCalendar('render')
    }, 1000);
}