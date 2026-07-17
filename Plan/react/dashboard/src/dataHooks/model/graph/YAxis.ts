import Highcharts from 'highcharts/esm/highstock';

export type YAxisById = {
    players: Highcharts.YAxisOptions;
    tps: Highcharts.YAxisOptions;
    milliseconds: Highcharts.YAxisOptions;
    percentage: Highcharts.YAxisOptions;
    chunks: Highcharts.YAxisOptions;
    entities: Highcharts.YAxisOptions;
    megabytes: Highcharts.YAxisOptions;
}