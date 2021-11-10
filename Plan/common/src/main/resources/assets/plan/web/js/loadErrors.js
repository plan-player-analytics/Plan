const errorLogs = document.getElementById('error-logs');

function renderErrorLog(i, errorLog) {
    return createErrorAccordionTitle(i, errorLog) + createErrorAccordionBody(i, errorLog);
}

function createErrorAccordionTitle(i, errorLog) {
    let style = 'bg-amber-outline';
    const tagLine = errorLog.contents[0];
    return `<tr id="error_h_${i}" aria-controls="error_t_${i}" aria-expanded="false" 
                class="clickable collapsed ${style}" data-bs-target="#error_t_${i}" data-bs-toggle="collapse">
                <td>${errorLog.fileName}</td>
                <td>${tagLine}</td>
            </tr>`
}

function createErrorAccordionBody(i, errorLog) {
    return `<tr class="collapse" data-bs-parent="#tableAccordion" id="error_t_${i}">
                <td colspan="2">
                    <pre class="pre-scrollable" style="overflow-x: scroll">${errorLog.contents.join('\n')}</pre>
                </td>
            </tr>`;
}

jsonRequest("./v1/errors", (json, error) => {
    if (error) {
        return errorLogs.innerText = `Failed to load /v1/errors: ${error}`;
    }

    let html = ``;
    for (let i = 0; i < json.length; i++) {
        html += renderErrorLog(i, json[i]);
    }

    errorLogs.innerHTML = html;
})