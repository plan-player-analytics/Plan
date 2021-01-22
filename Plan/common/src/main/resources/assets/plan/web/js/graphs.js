var linegraphButtons = [{
    type: 'hour',
    count: 12,
    text: '12h'
}, {
    type: 'hour',
    count: 24,
    text: '24h'
}, {
    type: 'day',
    count: 7,
    text: '7d'
}, {
    type: 'month',
    count: 1,
    text: '30d'
}, {
    type: 'all',
    text: 'All'
}];

var graphs = [];
window.calendars = {};

function activityPie(id, activitySeries) {
    graphs.push(Highcharts.chart(id, {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {text: ''},
        tooltip: {
            pointFormat: '{series.name}: <b>{point.y}</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        series: [activitySeries]
    }));
}

function diskChart(id, series) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 2,
            buttons: linegraphButtons
        },
        yAxis: {
            labels: {
                formatter: function () {
                    return this.value + ' MB';
                }
            },
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        legend: {
            enabled: true
        },
        series: series
    }));
}

function horizontalBarChart(id, categories, series, text) {
    graphs.push(Highcharts.chart(id, {
        chart: {
            type: 'bar'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: categories,
            title: {
                text: null
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: text,
                align: 'high'
            },
            labels: {
                overflow: 'justify'
            }
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true
                }
            }
        },
        credits: {
            enabled: true
        },
        series: series
    }));
}

function lineChart(id, series) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 2,
            buttons: linegraphButtons
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        legend: {
            enabled: true
        },
        series: series
    }));
}

function dayByDay(id, series) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 2,
            buttons: linegraphButtons
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        legend: {
            enabled: true
        },
        time: {timezoneOffset: 0},
        series: series
    }));
}

function onlineActivityCalendar(id, event_data, firstDay) {
    window.calendars.online_activity = new FullCalendar.Calendar(document.querySelector(id), {
        timeZone: "UTC",
        themeSystem: 'bootstrap',
        eventColor: '#2196F3',
        firstDay: firstDay,
        initialView: 'dayGridMonth',

        eventDidMount: function (info) {
            $(info.el).popover({
                content: info.event.title,
                trigger: 'hover',
                placement: 'top',
                container: 'body'
            });
        },

        events: function (fetchInfo, successCallback, failureCallback) {
            successCallback(event_data)
        },

        height: 800,
        contentHeight: 795,
        headerToolbar: {
            left: 'title',
            center: '',
            right: 'today prev,next'
        }
    });

    window.calendars.online_activity.render();
}

function performanceChart(id, playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 2,
            buttons: linegraphButtons
        },
        title: {text: ''},
        yAxis: [{
            labels: {
                formatter: function () {
                    return this.value + ' P';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' TPS';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + '%';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' MB';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' E';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' C';
                }
            }
        }],
        legend: {
            enabled: true
        },
        series: [playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries]
    }));
}

function playersChart(id, playersOnlineSeries, sel) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: sel,
            buttons: linegraphButtons
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        series: [playersOnlineSeries]
    }));
}

function playersChartNoNav(id, playersOnlineSeries) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 3,
            buttons: linegraphButtons
        },
        navigator: {
            enabled: false
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        series: [playersOnlineSeries]
    }));
}

function punchCard(id, punchcardSeries) {
    graphs.push(Highcharts.chart(id, {
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
        series: [punchcardSeries]
    }));
}

function resourceChart(id, cpuSeries, ramSeries, playersOnlineSeries) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 1,
            buttons: linegraphButtons
        },
        tooltip: {
            split: true
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        yAxis: [{
            labels: {
                formatter: function () {
                    return this.value + ' Players';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + '%';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' MB';
                }
            }
        }],
        legend: {
            enabled: true
        },
        series: [cpuSeries, ramSeries, playersOnlineSeries]
    }));
}

function serverPie(id, serverSeries) {
    graphs.push(Highcharts.chart(id, {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {text: ''},
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        tooltip: {
            formatter: function () {
                return '<b>' + this.point.name + ':</b> ' + formatTimeAmount(this.y) + ' (' + this.percentage.toFixed(2) + '%)';
            }
        },
        series: [serverSeries]
    }));
}

function formatTimeAmount(ms) {
    var out = "";

    var seconds = Math.floor(ms / 1000);

    var dd = Math.floor(seconds / 86400);
    seconds -= (dd * 86400);
    var dh = Math.floor(seconds / 3600);
    seconds -= (dh * 3600);
    var dm = Math.floor(seconds / 60);
    seconds -= (dm * 60);
    seconds = Math.floor(seconds);
    if (dd !== 0) {
        out += dd.toString() + "d ";
    }
    if (dh !== 0) {
        out += dh.toString() + "h ";
    }
    if (dm !== 0) {
        out += dm.toString() + "m ";
    }
    out += seconds.toString() + "s ";

    return out;
}

function sessionCalendar(id, event_data, firstDay) {
    window.calendars.sessions = new FullCalendar.Calendar(document.querySelector(id), {
        timeZone: "UTC",
        themeSystem: 'bootstrap',
        eventColor: '#009688',
        dayMaxEventRows: 4,
        firstDay: firstDay,
        initialView: 'dayGridMonth',

        eventDidMount: function (info) {
            $(info.el).popover({
                content: info.event.title,
                trigger: 'hover',
                placement: 'top',
                container: 'body'
            });
        },

        events: function (fetchInfo, successCallback, failureCallback) {
            successCallback(event_data)
        },

        navLinks: true,
        height: 450,
        contentHeight: 445,
        headerToolbar: {
            left: 'title',
            center: '',
            right: 'dayGridMonth dayGridWeek dayGridDay today prev,next'
        }
    });

    setTimeout(function () {
        window.calendars.sessions.render();
    }, 0);
}

function stackChart(id, categories, series, label) {
    graphs.push(Highcharts.chart(id, {
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
                text: label
            },
            labels: {
                formatter: function () {
                    return this.value;
                }
            },
            softMax: 2,
            softMin: 0
        },
        tooltip: {
            split: true,
            valueSuffix: ' ' + label
        },
        plotOptions: {
            area: {
                stacking: 'normal',
                lineWidth: 1
            }
        },
        series: series
    }));
}

function tpsChart(id, tpsSeries, playersOnlineSeries) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 1,
            buttons: linegraphButtons
        },
        tooltip: {
            split: true
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        yAxis: [{
            labels: {
                formatter: function () {
                    return this.value + ' Players';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' TPS';
                }
            }
        }],
        legend: {
            enabled: true
        },
        series: [tpsSeries, playersOnlineSeries]
    }));
}

function worldChart(id, entitySeries, chunkSeries, playersOnlineSeries) {
    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 1,
            buttons: linegraphButtons
        },
        tooltip: {
            split: true
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        yAxis: [{
            labels: {
                formatter: function () {
                    return this.value + ' Players';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' Entities';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' Chunks';
                }
            }
        }],
        legend: {
            enabled: true
        },
        series: [entitySeries, chunkSeries, playersOnlineSeries]
    }));
}

function worldMap(id, colorMin, colorMax, mapSeries) {
    graphs.push(Highcharts.mapChart(id, {
        chart: {
            animation: true
        },
        title: {text: ''},

        mapNavigation: {
            enabled: true,
            enableDoubleClickZoomTo: true
        },

        colorAxis: {
            min: 1,
            type: 'logarithmic',
            minColor: colorMin,
            maxColor: colorMax
        },
        series: [mapSeries]
    }));
}

function worldPie(id, worldSeries, gmSeries) {
    var defaultTitle = '';
    var defaultSubtitle = 'Click to expand';
    var chart = Highcharts.chart(id, {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie',
            events: {
                drilldown: function (e) {
                    chart.setTitle({text: '' + e.point.name}, {text: ''});
                },
                drillup: function (e) {
                    chart.setTitle({text: defaultTitle}, {text: defaultSubtitle});
                }
            }
        },
        title: {text: defaultTitle},
        subtitle: {
            text: defaultSubtitle
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        tooltip: {
            formatter: function () {
                return '<b>' + this.point.name + ':</b> ' + formatTimeAmount(this.y) + ' (' + this.percentage.toFixed(2) + '%)';
            }
        },
        series: [worldSeries],
        drilldown: {
            series: gmSeries.map(function (d) {
                return {name: d.name, id: d.id, colors: gmPieColors, data: d.data}
            })
        }
    });
    graphs.push(chart);
}

function updateGraphs() {
    for (let graph of graphs) {
        graph.update(Highcharts.theme);
    }
}