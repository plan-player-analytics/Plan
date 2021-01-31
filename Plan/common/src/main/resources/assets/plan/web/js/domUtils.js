function insertElementBefore(elementSelector, createElementFunction) {
    const placeBefore = document.querySelector(elementSelector);
    const element = createElementFunction();
    placeBefore.insertAdjacentElement('beforebegin', element);
}