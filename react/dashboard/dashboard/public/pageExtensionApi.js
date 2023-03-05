/*
* Usage:
* - Look for any element with 'extendable' class name and use its id.
*
* // Html-string
* const render = async (context) => {
*   // Any code that needs to run before the element is added to DOM
*   return '<p>Hello world</p>';
* }
* const unmount = async () => {
*   // Any code that needs to run when the element is removed from DOM
* };
* // 'card-body-server-as-numbers' is the ID of the element
* global.pageExtensionApi.registerElement('beforeElement', 'card-body-server-as-numbers', render, unmount);
*
* // HtmlElement
* global.pageExtensionApi.registerElement('afterElement', 'card-body-server-as-numbers', () => {
*   const para = document.createElement("p");
*   para.innerText = "Hello world";
*   return para;
* }, unmount);
*/
class PageExtensionApi {
    beforeElementRenders = [];
    afterElementRenders = [];

    registerElement(position, id, renderCallback, unmountCallback) {
        const renderers = position === 'beforeElement' ? this.beforeElementRenders : this.afterElementRenders;
        renderers.push({id, renderCallback, unmountCallback});
    }

    onRender(id, position, context) {
        const renderers = position === 'beforeElement' ? this.beforeElementRenders : this.afterElementRenders;
        return Promise.all(renderers
            .filter(renderer => renderer.id === id)
            .filter(renderer => renderer.renderCallback)
            .map(async renderer => {
                try {
                    const rendered = await renderer.renderCallback(context);
                    if (rendered instanceof HTMLElement) {
                        return rendered.outerHTML;
                    } else {
                        return rendered;
                    }
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

// Global
pageExtensionApi = new PageExtensionApi();