function activityPie(id, activitySeries, activityTotal, activityColors) {
	Highcharts.chart(id, {
		chart: {
			plotBackgroundColor: null,
			plotBorderWidth: null,
			plotShadow: false,
			type: 'pie'
		},
		subtitle: {text: 'Total Players:' + activityTotal},
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
				colors: activityColors,
				showInLegend: true
			}
		},
		series: [activitySeries]
	});
}