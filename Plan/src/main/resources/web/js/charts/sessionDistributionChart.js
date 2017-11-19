function sessionDistributionChart(id, sessionLengthSeries) {
	Highcharts.chart(id, {
		chart: {
			type: 'column'
		},
		xAxis: {
			type: 'category',
			labels: {
				rotation: -45
			}
		},
		yAxis: {
			min: 0,
			title: {
				text: 'Sessions'
			}
		},
		legend: {
			enabled: false
		},
		plotOptions: {
			series: {
				groupPadding: 0
			},
			pointPadding: 0
		},
		series: [sessionLengthSeries]
	});
}