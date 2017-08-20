function gmPie(id, gmSeries, gmtotal, gmColors) {
	Highcharts.chart(id, {
		chart: {
			plotBackgroundColor: null, plotBorderWidth: null, plotShadow: false,
			type: 'pie'
		},
		subtitle: {text: gmtotal},
		tooltip: {
			pointFormat: '{series.name}: <b>{point.percentage:.2f}%</b>'
		},
		plotOptions: {
			pie: {
				allowPointSelect: true,
				cursor: 'pointer',
				dataLabels: {
					enabled: false
				},
				colors: gmColors,
				showInLegend: true
			}
		},
		series: [gmSeries]
	});
}