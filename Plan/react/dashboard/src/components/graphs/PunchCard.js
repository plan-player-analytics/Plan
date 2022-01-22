import React, {useEffect} from "react";
import Highcharts from 'highcharts';

// TODO Graphs context for night mode switch

const PunchCard = ({series}) => {
    useEffect(() => {
        const punchCard = {
            name: 'Relative Activity',
            color: '#222',
            data: series
        };
        Highcharts.chart('punchcard', {
            chart: {
                defaultSeriesType: 'scatter'
            },
            title: {text: ''},
            xAxis: {
                type: 'datetime',
                dateTimeLabelFormats: {
                    // https://www.php.net/manual/en/function.strftime.php
                    hour: '%I %P',
                    day: '%I %P'
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
            series: [punchCard]
        })
    }, [series])

    return (
        <div className="chart-area" id="punchcard">
            <span className="loader"/>
        </div>
    )
}

export default PunchCard