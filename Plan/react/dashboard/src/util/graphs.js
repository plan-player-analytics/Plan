export const linegraphButtons = [{
    type: 'hour',
    count: 12,
    text: '12h'
}, {
    type: 'hour',
    count: 24,
    text: '24h'
}, {
    type: 'day',
    count: 7,
    text: '7d'
}, {
    type: 'month',
    count: 1,
    text: '30d'
}, {
    type: 'all',
    text: 'All'
}];

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