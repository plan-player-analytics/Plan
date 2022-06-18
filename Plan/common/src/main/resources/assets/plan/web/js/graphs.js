const linegraphButtons = [{
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

const graphs = [];
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

function mapToDataSeries(performanceData) {
    const playersOnline = [];
    const tps = [];
    const cpu = [];
    const ram = [];
    const entities = [];
    const chunks = [];
    const disk = [];

    return new Promise((resolve => {
        let i = 0;
        const length = performanceData.length;

        function processNextThousand() {
            const to = Math.min(i + 1000, length);
            for (i; i < to; i++) {
                const entry = performanceData[i];
                const date = entry[0];
                playersOnline[i] = [date, entry[1]];
                tps[i] = [date, entry[2]];
                cpu[i] = [date, entry[3]];
                ram[i] = [date, entry[4]];
                entities[i] = [date, entry[5]];
                chunks[i] = [date, entry[6]];
                disk[i] = [date, entry[7]];
            }
            if (i >= length) {
                resolve({playersOnline, tps, cpu, ram, entities, chunks, disk})
            } else {
                setTimeout(processNextThousand, 10);
            }
        }

        processNextThousand();
    }))
}

function performanceChart(id, playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries) {
    const chart = Highcharts.stockChart(id, {
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
    });

    function toggleLabels() {
        if (!chart || !chart.yAxis || !chart.yAxis.length) return;
        const newWidth = $(window).width();
        chart.yAxis[0].update({labels: {enabled: newWidth >= 900}});
        chart.yAxis[1].update({labels: {enabled: newWidth >= 900}});
        chart.yAxis[2].update({labels: {enabled: newWidth >= 1000}});
        chart.yAxis[3].update({labels: {enabled: newWidth >= 1000}});
        chart.yAxis[4].update({labels: {enabled: newWidth >= 1400}});
        chart.yAxis[5].update({labels: {enabled: newWidth >= 1400}});
    }

    $(window).resize(toggleLabels);
    toggleLabels();

    graphs.push(chart);
}

function playersChart(id, playersOnlineSeries, sel) {
    function groupByIntervalStartingFrom(startDate, interval) {
        let previousGroupStart = startDate;
        const groupByInterval = [[]];

        for (let point of playersOnlineSeries.data) {
            const date = point[0];
            if (date < startDate) {
                continue;
            }

            if (previousGroupStart + interval < date) {
                previousGroupStart = date;
                groupByInterval.push([]);
            }

            const currentGroup = groupByInterval[groupByInterval.length - 1];
            currentGroup.push(point);
        }
        return groupByInterval;
    }

    function averageGroupPoints(groupByInterval, minDate) {
        const averages = [];
        for (let group of groupByInterval) {
            let totalDate = 0;
            let total = 0;
            let count = group.length;
            for (let point of group) {
                totalDate += (point[0] - minDate); // Remove the minDate from dates to calculate a smaller total
                total += point[1];
            }

            if (count !== 0) {
                const middleDate = Math.trunc((totalDate / count) + minDate);
                const average = Math.trunc(total / count);
                averages.push([middleDate, average]);
            }
        }
        return averages;
    }

    function getAveragePlayersSeries(minDate, twentyPointInterval) {
        const groupByInterval = groupByIntervalStartingFrom(minDate, twentyPointInterval);

        return {
            name: s.name.averagePlayersOnline,
            type: s.type.spline,
            tooltip: s.tooltip.zeroDecimals,
            data: averageGroupPoints(groupByInterval, minDate),
            color: "#02458d",
            yAxis: 0
        };
    }

    function updateAveragePlayers(event) {
        const minDate = event.min;
        const maxDate = event.max;
        const twentyPointInterval = (maxDate - minDate) / 20;

        const averagePlayersSeries = getAveragePlayersSeries(minDate, twentyPointInterval);

        const playersOnlineGraph = graphs.find(graph => graph && graph.renderTo && graph.renderTo.id === id);
        playersOnlineGraph.series[1].update(averagePlayersSeries);
    }

    const emptyAveragePlayersSeries = {
        name: s.name.averagePlayersOnline,
        type: s.type.spline,
        tooltip: s.tooltip.zeroDecimals,
        data: [],
        color: "#02458d",
        yAxis: 0
    };

    graphs.push(Highcharts.stockChart(id, {
        rangeSelector: {
            selected: sel,
            buttons: linegraphButtons
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        /* Average online players graph Disabled
        xAxis: {
            events: {
                afterSetExtremes: updateAveragePlayers
            }
        },
        */
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        series: [playersOnlineSeries, /*emptyAveragePlayersSeries*/]
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

function joinAddressPie(id, joinAddresses) {
    if (joinAddresses.data.length < 2) {
        document.getElementById(id).innerHTML = '<div class="card-body"><p></p></div>'
        document.getElementById(id).classList.remove('chart-area');

        // XSS danger appending join addresses directly, using innerText is safe.
        for (let slice of joinAddresses.data) {
            document.querySelector(`#${id} p`).innerText = `${slice.name}: ${slice.y}`;
        }
    } else {
        document.getElementById(id).innerHTML = '';
        document.getElementById(id).classList.add('chart-area');
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
                    return '<b>' + this.point.name + ':</b> ' + this.y + ' (' + this.percentage.toFixed(2) + '%)';
                }
            },
            series: [joinAddresses]
        }));
    }
}

function formatTimeAmount(ms) {
    let out = "";

    let seconds = Math.floor(ms / 1000);

    const dd = Math.floor(seconds / 86400);
    seconds -= (dd * 86400);
    const dh = Math.floor(seconds / 3600);
    seconds -= (dh * 3600);
    const dm = Math.floor(seconds / 60);
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
    document.querySelector(id + " .loader").remove();
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
            },
            ordinal: false
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
    const defaultTitle = '';
    const defaultSubtitle = 'Click to expand';
    const chart = Highcharts.chart(id, {
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
    // HighCharts nukes the scrollbar variable from the given parameter
    // If the graph doesn't support srollbars (bar, pie and map charts for example)
    // This workaround stores a copy of the scrollbar so that it can be set
    const scrollbar = {...Highcharts.theme.scrollbar};

    function updateGraph(graph, index, array) {
        // Empty objects can be left in the array if existing graph is re-rendered
        if (Object.keys(graph).length === 0) {
            array.splice(index, 1);
            return;
        }

        // scrollbar workaround
        if (!Highcharts.theme["scrollbar"]) Highcharts.theme["scrollbar"] = {...scrollbar};

        graph.update(Highcharts.theme);
    }

    graphs.forEach(updateGraph);
}
