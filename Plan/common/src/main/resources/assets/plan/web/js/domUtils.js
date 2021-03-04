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