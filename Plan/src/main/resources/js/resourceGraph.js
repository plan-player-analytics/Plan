function resourceChart(id, cpuSeries, ramSeries) {
	Highcharts.stockChart(id, {
		rangeSelector: {
			selected: 1,
			buttons: [{
				type: 'hour',
				count: 12,
				text: '12h'
			},{
				type: 'hour',
				count: 24,
				text: '24h'
			},{
				type: 'day',
				count: 7,
				text: '7d'
			},{
				type: 'month',
				count: 1,
				text: '30d'
			},{
				type: 'all',
				text: 'All'
			}]
		},
		yAxis: [{
			labels: {
				align: 'right',
				x: -3
			},
			title: {
				text: 'CPU / %'
			},
			height: '55%',
			lineWidth: 2
		}, {
			labels: {
				align: 'right',
				x: -3
			},
			title: {
				text: 'RAM / MB'
			},
			top: '55%',
			height: '40%',
			offset: 0,
			lineWidth: 2
		}],
		tooltip: {
			split: true
		},
		plotOptions: {
			areaspline: {
				fillOpacity: 0.4
			}
		},
		series: [cpuSeries, ramSeries]
	});
}