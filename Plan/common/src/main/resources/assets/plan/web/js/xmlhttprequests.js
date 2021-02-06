function refreshingJsonRequest(address, callback, tabID) {
    const timestamp = Date.now();
    const addressWithTimestamp = address.includes('?')
        ? `${address}&timestamp=${timestamp}`
        : `${address}?timestamp=${timestamp}`

    const refreshElement = document.querySelector(`#${tabID} .refresh-element`);
    refreshElement.querySelector('i').addEventListener('click', () => {
        refreshElement.querySelector('.refresh-notice').innerHTML = '<i class="fa fa-fw fa-cog fa-spin"></i> Updating..';
        refreshingJsonRequest(address, callback, tabID);
    });

    let timeout = 1000;

    function makeTheRequest() {
        jsonRequest(addressWithTimestamp, (json, error) => {
            if (error) {
                if (error.status === 400 && error.error.includes('Attempt to get data from the future!')) {
                    console.error(error.error); // System time not in sync with UTC
                    refreshElement.innerHTML = "System times out of sync with UTC";
                    return jsonRequest(address, callback);
                }
                refreshElement.querySelector('.refresh-notice').innerHTML = "";
                return callback(json, error);
            }

            refreshElement.querySelector('.refresh-time').innerText = json.timestamp_f;

            const lastUpdated = json.timestamp;
            // TODO Work out the kinks with the refresh barrier time
            if (lastUpdated < timestamp) {
                setTimeout(makeTheRequest, timeout);
                timeout = timeout >= 12000 ? timeout : timeout * 2;
            } else {
                refreshElement.querySelector('.refresh-notice').innerHTML = "";
            }
            callback(json, error);
        })
    }

    makeTheRequest();
}

/**
 * Make an XMLHttpRequest for JSON data.
 * @param address Address to request from
 * @param callback function with (json, error) parameters to call after the request.
 */
function jsonRequest(address, callback) {
    setTimeout(function () {
        var xhttp = new XMLHttpRequest();
        xhttp.withCredentials = true;
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                try {
                    if (this.status === 200 || (this.status === 0 && this.responseText)) {
                        var json = JSON.parse(this.responseText);
                        setTimeout(function () {
                            callback(json, null)
                        }, 0);
                    } else if (this.status === 404 || this.status === 403 || this.status === 500) {
                        callback(null, "HTTP " + this.status + " (See " + address + ")")
                    } else if (this.status === 400) {
                        const json = JSON.parse(this.responseText);
                        callback(json, json.error)
                    } else if (this.status === 0) {
                        callback(null, "Request did not reach the server. (Server offline / Adblocker?)")
                    }
                } catch (e) {
                    callback(null, e.message + " (See " + address + ")")
                }
            }
        };
        xhttp.timeout = 45000;
        xhttp.ontimeout = function () {
            callback(null, "Timed out after 45 seconds. (" + address + ")")
        };
        xhttp.open("GET", address, true);
        xhttp.send();
    }, 0);
}