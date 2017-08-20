function tpsChart(id, tpsSeries, playersOnlineSeries) {
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
				text: 'Players'
			},
			height: '30%'
		}, {
			lineWidth: 2,
			labels: {
				align: 'right',
				x: -3
			},
			title: {
				text: 'TPS'
			},
			height: '70%',
			top: '30%',
			offset: 0
		}],
		tooltip: {
			split: true
		},
		plotOptions: {
			areaspline: {
				fillOpacity: 0.4
			}
		},
		series: [tpsSeries, playersOnlineSeries]
	});
}