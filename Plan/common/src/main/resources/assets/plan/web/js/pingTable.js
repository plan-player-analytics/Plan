function loadPingTable(json, error) {
    const pingTable = document.querySelector('#geolocations #data_ping_table tbody');

    if (error) {
        pingTable.innerHTML = `<tr><td>Error: ${error}</td><td>-</td><td>-</td><td>-</td></tr>`;
        return;
    }

    const countries = json.table;

    if (!countries.length) {
        pingTable.innerHTML = '<tr><td>No Data</td><td>-</td><td>-</td><td>-</td></tr>';
        return;
    }

    pingTable.innerHTML = countries.map(createPingTableRow).join('');
}

function createPingTableRow(entry) {
    return `<tr>
            <td>${entry.country}</td>
            <td>${entry.avg_ping}</td>
            <td>${entry.min_ping}</td>
            <td>${entry.max_ping}</td>
        </tr>`
}