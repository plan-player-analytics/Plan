// Stored by tab {'tab-id': ['address', 'address']}
const currentlyRefreshing = {};
let refreshBarrierMs = 0;

function refreshingJsonRequest(address, callback, tabID, skipOldData) {
    const timestamp = Date.now();
    const addressWithTimestamp = address.includes('?')
        ? `${address}&timestamp=${timestamp}`
        : `${address}?timestamp=${timestamp}`

    const refreshElement = document.querySelector(`#${tabID} .refresh-element`);
    refreshElement.querySelector('i').addEventListener('click', () => {
        if (currentlyRefreshing[tabID].includes(address)) {
            return;
        }
        refreshElement.querySelector('.refresh-notice').innerHTML = '<i class="fa fa-fw fa-cog fa-spin"></i> Updating..';
        refreshingJsonRequest(address, callback, tabID, true);
    });

    let timeout = 1000;

    if (!currentlyRefreshing[tabID]) currentlyRefreshing[tabID] = [];
    currentlyRefreshing[tabID].push(address);

    function makeTheRequest(skipOldData) {
        jsonRequest(addressWithTimestamp, (json, error) => {
            if (error) {
                currentlyRefreshing[tabID].splice(currentlyRefreshing[tabID].indexOf(address), 1);
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
            if (lastUpdated + refreshBarrierMs < timestamp) {
                setTimeout(() => makeTheRequest(true), timeout);
                timeout = timeout >= 12000 ? timeout : timeout * 2;
                if (!skipOldData) callback(json, error);
            } else {
                currentlyRefreshing[tabID].splice(currentlyRefreshing[tabID].indexOf(address), 1);
                if (!currentlyRefreshing[tabID].length) {
                    refreshElement.querySelector('.refresh-notice').innerHTML = "";
                }
                callback(json, error);
            }
        })
    }

    makeTheRequest(skipOldData);
}

/**
 * Make a GET XMLHttpRequest for JSON data.
 * @param address Address to request from
 * @param callback function with (json, error) parameters to call after the request.
 */
function jsonRequest(address, callback) {
    setTimeout(function () {
        const xhr = newConfiguredXHR(callback);

        xhr.open("GET", address, true);
        xhr.send();
    }, 0);
}

/**
 * Make a POST XMLHttpRequest for JSON data.
 * @param address Address to request from
 * @param postBody POST body (form).
 * @param callback function with (json, error) parameters to call after the request.
 */
function jsonPostRequest(address, postBody, callback) {
    setTimeout(function () {
        const xhr = newConfiguredXHR(callback, address);

        xhr.open("POST", address, true);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhr.send(postBody);
    }, 0);
}

/**
 * Create new XMLHttpRequest configured for methods such as jsonRequest
 * @param callback function with (json, error) parameters to call after the request.
 */
function newConfiguredXHR(callback, address) {
    const xhr = new XMLHttpRequest();

    xhr.withCredentials = true;
    xhr.onreadystatechange = function () {
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
                callback(null, e.message)
            }
        }
    };
    xhr.timeout = 45000;
    xhr.ontimeout = function () {
        callback(null, "Timed out after 45 seconds.")
    };

    return xhr;
}

function awaitUntil(predicateFunction) {
    return new Promise((resolve => {
        const handlerFunction = () => {
            if (predicateFunction.apply()) {
                resolve();
            } else {
                setTimeout(handlerFunction, 10)
            }
        };
        handlerFunction();
    }))
}