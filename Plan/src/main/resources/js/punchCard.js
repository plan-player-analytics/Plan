function punchCard(id, punchcardSeries) {
	Highcharts.chart(id, {
		chart: {
			defaultSeriesType: 'scatter'
		},
		xAxis: {
			type: 'datetime',
			dateTimeLabelFormats: {
				hour: '%I %P'
			},
			tickInterval: 3600000
		},
		yAxis: {
			categories: ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
		},
		tooltip: {
			pointFormat: 'Activity: {point.z}'
		},
		series: [punchcardSeries]
	});
}