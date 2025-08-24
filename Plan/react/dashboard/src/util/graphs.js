import {localeService} from "../service/localeService.js";

export const translateLinegraphButtons = (t) => {
    const formatter = new Intl.DurationFormat(localeService.getIntlFriendlyLocale(), {
        style: 'narrow',
    });
    const format = ({hours, days}) => formatter.format({
        days: days > 0 ? Math.floor(days) : undefined,
        hours: hours > 0 ? Math.floor(hours) : undefined
    });

    return [{
        type: 'hour',
        count: 12,
        text: format({hours: 12})
    }, {
        type: 'hour',
        count: 24,
        text: format({hours: 24})
    }, {
        type: 'day',
        count: 7,
        text: format({days: 7})
    }, {
        type: 'month',
        count: 1,
        text: format({days: 30})
    }, {
        type: 'all',
        text: t('html.label.all')
    }];
}
export const tooltip = {
    twoDecimals: {valueDecimals: 2},
    zeroDecimals: {valueDecimals: 0}
}

export const mapPerformanceDataToSeries = performanceData => {
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
};

export const yAxisConfigurations = {
    PLAYERS_ONLINE: {
        labels: {
            formatter: function () {
                return this.value + ' P';
            }
        },
        softMin: 0,
        softMax: 2
    },
    TPS: {
        opposite: true,
        labels: {
            formatter: function () {
                return this.value + ' TPS';
            }
        },
        softMin: 0,
        softMax: 20
    },
    CPU: {
        opposite: true,
        labels: {
            formatter: function () {
                return this.value + '%';
            }
        },
        softMin: 0,
        softMax: 100
    },
    RAM_OR_DISK: {
        labels: {
            formatter: function () {
                return this.value + ' MB';
            }
        },
        softMin: 0
    },
    ENTITIES: {
        opposite: true,
        labels: {
            formatter: function () {
                return this.value + ' E';
            }
        },
        softMin: 0,
        softMax: 2
    },
    CHUNKS: {
        labels: {
            formatter: function () {
                return this.value + ' C';
            }
        },
        softMin: 0
    }
}