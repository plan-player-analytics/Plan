self.onmessage = (e) => {
    const performanceData = e.data;
    const playersOnline = [];
    const tps = [];
    const cpu = [];
    const ram = [];
    const entities = [];
    const chunks = [];
    const disk = [];
    const msptAverage = [];
    const mspt95thPercentile = [];

    let i = 0;
    const length = performanceData.length;

    for (i; i < length; i++) {
        const entry = performanceData[i];
        const date = entry[0];
        playersOnline[i] = [date, entry[1]];
        tps[i] = [date, entry[2]];
        cpu[i] = [date, entry[3]];
        ram[i] = [date, entry[4]];
        entities[i] = [date, entry[5]];
        chunks[i] = [date, entry[6]];
        disk[i] = [date, entry[7]];
        msptAverage[i] = [date, entry[8]];
        mspt95thPercentile[i] = [date, entry[9]];
    }
    const processedData = {playersOnline, tps, cpu, ram, entities, chunks, disk, msptAverage, mspt95thPercentile};
    self.postMessage(processedData);
};