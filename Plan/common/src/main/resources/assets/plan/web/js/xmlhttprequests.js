/**
 * Make an XMLHttpRequest for JSON data.
 * @param address Address to request from
 * @param callback function with (json, error) parameters to call after the request.
 */
function jsonRequest(address, callback) {
    setTimeout(function () {
        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                try {
                    if (this.status === 200) {
                        var json = JSON.parse(this.responseText);
                        setTimeout(function () {
                            callback(json, null)
                        }, 0);
                    } else if (this.status === 404 || this.status === 403 || this.status === 500) {
                        callback(null, this.status)
                    }
                } catch (e) {
                    callback(null, e.message)
                }
            }
        };
        xhttp.open("GET", address, true);
        xhttp.send();
    }, 0);
}