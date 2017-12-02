function activityStackChart(id, categories, activitySeries) {
    Highcharts.chart(id, {
        chart: {
            type: 'area'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: categories,
            tickmarkPlacement: 'on',
            title: {
                enabled: false
            }
        },
        yAxis: {
            title: {
                text: 'Players'
            },
            labels: {
                formatter: function () {
                    return this.value;
                }
            }
        },
        tooltip: {
            split: true,
            valueSuffix: ' Players'
        },
        plotOptions: {
            area: {
                stacking: 'normal',
                // lineColor: '#666666',
                lineWidth: 1
                // ,
                // marker: {
                //     lineWidth: 1,
                //     lineColor: '#666666'
                // }
            }
        },
        series: activitySeries
    });
}