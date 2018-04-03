/**
 * @param id Id of the HTML element
 * @param src
 * @param createOptions
 * @param className
 */
const appBootstrapContaineriFrame = function({id, src, createOptions, className}) {
    return `
        <iframe id="${id}-iframe" class="${className} spark-app-iframe" src="${src}" width="${createOptions.width}"
            height="100%" scrolling="no">
        </iframe>
    `;
};


/**
 * Template to create an error dialog 2
 * @param id
 * @param title
 * @param src
 * @param createOptions
 * @param className
 */
const appBootstrapContainerDialog2WithiFrame = function({id, title, src, createOptions, className}) {
    return `
        <section role="dialog" id="${id}" class="${className} aui-layer aui-dialog2" style="width:${createOptions.width};"
                aria-hidden="true">
            <header class="aui-dialog2-header">
                <h2 class="aui-dialog2-header-main">${title}</h2>
            </header>
            <div class="aui-dialog2-content spark-app-content"
                    style="padding: 0; width:${createOptions.width}; height: ${createOptions.height}; overflow: hidden;">
                ${appBootstrapContaineriFrame({id, src, createOptions, className})}
            </div>
            <footer class="aui-dialog2-footer">
                <div class="aui-dialog2-footer-actions">
                    ${createOptions.showSubmitButton ?
                        `<button id="submitDialogButton${id}" class="aui-button aui-button-primary">
                            ${createOptions.label.submit}
                        </button>`
                    : ''}
                    <button id="closeDialogButton${id}" class="aui-button aui-button-link">${createOptions.label.close}</button>
                </div>
            </footer>
        </section>
    `;
};


/**
 * Template to create an error dialog 2
 *
 * @param id
 * @param title
 * @param className
 */
const errorDialog2 = function({id, title, className}) {
    return `
        <section role="dialog" id="${id}" class="${className} aui-layer aui-dialog2 aui-dialog2-medium" aria-hidden="true">
            <header class="aui-dialog2-header">
                <h2 class="aui-dialog2-header-main">${title}</h2>
            </header>
            <div class="aui-dialog2-content spark-app-content"></div>
            <footer class="aui-dialog2-footer">
                <div class="aui-dialog2-footer-actions">
                    <button id="closeErrorDialogButton" class="aui-button aui-button-link">Close</button>
                </div>
            </footer>
        </section>
    `;
};


/**
 * Template to create an error dialog 1
 *
 * @param title
 * @param className
 */
const errorDialog = function({title, className}) {
    return `
        <div class="dialog-components ${className}">
            <h2 class="dialog-title">${title}</h2>
            <div class="dialog-page-body">
                <div class="dialog-panel-body spark-app-content">
            </div>
            </div>
                <div class="dialog-button-panel">
                <a href="#" class="button-panel-link button-panel-cancel-link" id="closeErrorDialogButton">Close</a>
            </div>
        </div>
    `;
};

/**
 * @param id Id of the HTML element
 * @param src URL of the content to load to the iframe
 * @param createOptions extra options to customize the dialog
 * @param className
 */
const appFullscreenContainerIframe = function({id, src, createOptions, className}) {
    return `
        <div id="${id}" 
            class="spark-fullscreen-wrapper ${className} ${createOptions.addChrome ? "spark-fullscreen-dialog" : "" }"
        >
            ${createOptions.addChrome ?
                `<div class="spark-fullscreen-chrome">
                    <div class="spark-fullscreen-chrome-btnwrap">
                        <button id="${id}-chrome-submit" class="aui-button aui-icon aui-icon-small aui-iconfont-success">
                            "OK"
                        </button>
                        <button id="${id}-chrome-cancel" class="aui-button aui-icon aui-icon-small aui-iconfont-close-dialog">
                            "Cancel"
                        </button>
                    </div>
                </div>
            ` : ''}
            <div class="spark-fullscreen-scroll-wrapper ${createOptions.addChrome ? "spark-fullscreen-haschrome" : ""}">
                <iframe id="${id}-iframe" class="spark-fullscreen-iframe" src="${src}" scrolling="no">
                </iframe>
            </div>
        </div>
    `;
};

/**
 * Template for a link that can be used as a trigger for an inline dialog
 *
 * @param targetId id of the controlled inline dialog
 * @param text optional link text
 */
const inlineDialogTrigger = function({ targetId, text }) {
    return `
        <a data-aui-trigger aria-controls="${targetId}" style="cursor: pointer; color: inherit;">${text}</a>
    `;
};

/**
 * Template for an inline dialog containing an iframe
 *
 * @param id id for the wrapper (and prefix for other ids)
 * @param src URL of the source of the iframe
 * @param createOptions supports setting 'alignment' and (initial) 'width' of the inline dialog
 * @param className
 */
const inlineDialogAppContainer = function({ id, src, createOptions, className }) {
    return `
        <aui-inline-dialog id="${id}" class="spark-inline-wrapper ${className}"
            alignment="${createOptions.alignment}">
                <div style="width:${createOptions.width}" id="${id + '-iframe-container'}">
                    <iframe id="${id}-iframe" class="${className} spark-iframe" src="${src}" scrolling="no">
                    </iframe>
                </div>
        </aui-inline-dialog>
    `;
};

/**
 * Template for creating a SPARK iframe for the most common SPA use case, where the iframe has width '100%' (ie. width of
 * its parent element) and its height will grow as needed to fit the content of the app
 *
 * @param id id for the iframe
 * @param src URL of the source of the iframe
 * @param className
 */
const bootstrappedIframe = function({ id, src, className }) {
    return `
        <iframe id="${id}" class="${className} spark-iframe" src="${src}" scrolling="no">
        </iframe>
    `;
};

export default {
    appBootstrapContaineriFrame,
    appBootstrapContainerDialog2WithiFrame,
    errorDialog2,
    errorDialog,
    appFullscreenContainerIframe,
    inlineDialogTrigger,
    inlineDialogAppContainer,
    bootstrappedIframe
};