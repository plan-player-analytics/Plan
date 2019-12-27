function loadPingTable(json, error) {
    pingTable = $("#geolocations").find("#data_ping_table").find("tbody");

    if (error) {
        pingTable.append('<tr><td>Error: ' + error + '</td><td>-</td><td>-</td><td>-</td></tr>');
        return;
    }

    var countries = json;

    if (!countries.length) {
        pingTable.append('<tr><td>No Data</td><td>-</td><td>-</td><td>-</td></tr>');
        return;
    }

    var tableHtml = '';

    for (var i = 0; i < countries.length; i++) {
        var country = countries[i];
        tableHtml += createPingTableRow(country);
    }

    pingTable.append(tableHtml);
}

function createPingTableRow(entry) {
    return '<tr><td>' + entry.country +
        '</td><td>' + entry.avg_ping +
        '</td><td>' + entry.min_ping +
        '</td><td>' + entry.max_ping +
        '</td></tr>'
}