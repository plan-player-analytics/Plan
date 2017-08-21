function worldMap(id, colorMin, colorMax, mapSeries) {
	Highcharts.mapChart(id, {
		chart: {
			animation: true
		},
		colorAxis: {
			min: 1,
			type: 'logarithmic',
			minColor: colorMin,
			maxColor: colorMax
		},
		series: [mapSeries]
	});
}