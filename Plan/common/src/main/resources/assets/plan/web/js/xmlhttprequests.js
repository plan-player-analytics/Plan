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