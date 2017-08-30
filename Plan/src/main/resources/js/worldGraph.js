function worldChart(id, entitySeries, chunkSeries) {
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
				text: 'Chunks'
			},
			height: '50%',
			lineWidth: 2
		}, {
			labels: {
				align: 'right',
				x: -3
			},
			title: {
				text: 'Entities'
			},
			top: '50%',
			height: '40%',
			offset: 0,
			lineWidth: 2
		}],
		tooltip: {
			split: true
		},
		title: {text: ''},
		plotOptions: {
			areaspline: {
				fillOpacity: 0.4
			}
		},
		series: [entitySeries, chunkSeries]
	});
}