/*
* Usage:
*
* // Html-string
* const render = async () => {
*   // Any code that needs to run before the element is added to DOM
*   return '<p>Hello world</p>';
* }
* const unmount = async () => {
*   // Any code that needs to run when the element is removed from DOM
* };
* global.pageExtensionApi.registerElement('beforeElement', 'server-overview-card', render, unmount);
*
* // HtmlElement
* global.pageExtensionApi.registerElement('afterElement', 'server-overview-card', () => {
*   const para = document.createElement("p");
*   para.innerText = "Hello world";
*   return para;
* }, unmount);
*/
class PageExtensionApi {
    beforeElementRenders = [];
    afterElementRenders = [];

    registerElement(position, className, renderCallback, unmountCallback) {
        const renderers = position === 'beforeElement' ? this.beforeElementRenders : this.afterElementRenders;
        renderers.push({className, renderCallback, unmountCallback});
    }

    onRender(className, position) {
        const renderers = position === 'beforeElement' ? this.beforeElementRenders : this.afterElementRenders;
        return Promise.all(renderers
            .filter(renderer => renderer.className === className)
            .filter(renderer => renderer.renderCallback)
            .map(async renderer => {
                try {
                    return await renderer.renderCallback()
                } catch (e) {
                    console.warn("Error when rendering " + renderer + ": " + e);
                    return null;
                }
            })
            .filter(renderedElement => renderedElement));
    }

    onUnmount(className, position) {
        const renderers = position === 'beforeElement' ? this.beforeElementRenders : this.afterElementRenders;
        return renderers
            .filter(renderer => renderer.className === className)
            .forEach(renderer => {
                try {
                    return renderer.unmountCallback()
                } catch (e) {
                    console.warn("Error when unmounting " + renderer + ": " + e);
                    return null;
                }
            });
    }
}

const pageExtensionApi = new PageExtensionApi();