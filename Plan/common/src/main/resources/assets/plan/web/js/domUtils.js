function insertElementBefore(elementSelector, createElementFunction) {
    const placeBefore = document.querySelector(elementSelector);
    insertElementBeforeElement(placeBefore, createElementFunction);
}

function insertElementBeforeElement(placeBefore, createElementFunction) {
    const element = createElementFunction();
    placeBefore.insertAdjacentElement('beforebegin', element);
}

function insertElementAfter(elementSelector, createElementFunction) {
    const placeBefore = document.querySelector(elementSelector);
    insertElementAfterElement(placeBefore, createElementFunction)
}

function insertElementAfterElement(placeBefore, createElementFunction) {
    const element = createElementFunction();
    placeBefore.insertAdjacentElement('afterend', element);
}

/**
 * Shows an error using the page loader.
 * @param {string} [message] The message to be shown.
 */
function loaderError(message) {
    let loader = document.querySelector(".page-loader");
    let loaderText = document.querySelector('.loader-text');

    if (loader.style.display === "none") {
        loader.style.display = "block";
    }

    if (message !== undefined) {
        loaderText.innerText = message;
    } else {
        loaderText.innerText = "Error occurred, see the Developer Console (Ctrl+Shift+I) for details."
    }
}
