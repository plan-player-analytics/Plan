function onlineActivityCalendar(id, events, firstDay) {
    $(id).fullCalendar({
        eventColor: '#2196F3',
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

        height: 'parent',
        header: {
            left: 'title',
            center: '',
            right: 'month prev,next'
        }
    });

    setTimeout(function () {
        $(id).fullCalendar('render')
    }, 1000);
}