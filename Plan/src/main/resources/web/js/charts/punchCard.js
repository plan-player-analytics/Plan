function punchCard(id, punchcardSeries) {
    Highcharts.chart(id, {
        chart: {
            defaultSeriesType: 'scatter'
        },
        title: {text: ''},
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                hour: '%I %P',
                day: '%H %P'
            },
            tickInterval: 3600000
        },
        time: {
            timezoneOffset: 0
        },
        yAxis: {
            title: {
                text: "Day of the Week"
            },
            reversed: true,
            categories: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
        },
        tooltip: {
            pointFormat: 'Activity: {point.z}'
        },
        series: [punchcardSeries]
    });
}