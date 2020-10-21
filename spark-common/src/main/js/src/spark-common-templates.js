const escapeHtml = function(str) {
    if (!str) {
        return null;
    }
    return str.replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/&/g, '&amp;');
}


const escapeHtmlMany = function(srcObj, extract) {
    const res = {};
    extract.forEach(function(key) {
        res[key] = escapeHtml(srcObj[key]);
    });
    return res;
}


/**
 * @param id Id of the HTML element
 * @param src
 * @param createOptions
 * @param className
 */
const appBootstrapContaineriFrame = function(options) {
    const { id, src, className } = escapeHtmlMany(options, ['id', 'src', 'className']);
    const width = escapeHtml(options.createOptions.width);
    return `
        <iframe id="${id}-iframe" class="${className} spark-app-iframe" src="${src}" width="${width}"
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
const appBootstrapContainerDialog2WithiFrame = function(options) {
    const { id, title, src, className } = escapeHtmlMany(options, ['id', 'title', 'src', 'className']);
    const createOptions = options.createOptions;
    const width = escapeHtml(createOptions.width);
    const height = escapeHtml(createOptions.height);
    const showSubmitButton = !!createOptions.showSubmitButton;
    const submitLabel = escapeHtml(createOptions.label.submit);
    const closeLabel = escapeHtml(createOptions.label.close);
    // when calling other templates, use unescaped values
    const idRaw = options.id;
    const srcRaw = options.src;
    const classNameRaw = options.className;
    return `
        <section role="dialog" id="${id}" class="${className} aui-layer aui-dialog2" style="width:${width};"
                aria-hidden="true">
            <header class="aui-dialog2-header">
                <h2 class="aui-dialog2-header-main">${title}</h2>
            </header>
            <div class="aui-dialog2-content spark-app-content"
                    style="padding: 0; width:${width}; height: ${height}; overflow: hidden;">
                ${appBootstrapContaineriFrame({ id: idRaw, src: srcRaw, createOptions, className: classNameRaw })}
            </div>
            <footer class="aui-dialog2-footer">
                <div class="aui-dialog2-footer-actions">
                    ${showSubmitButton ?
        `<button id="submitDialogButton${id}" class="aui-button aui-button-primary">
                            ${submitLabel}
                        </button>`
        : ''}
                    <button id="closeDialogButton${id}" class="aui-button aui-button-link">${closeLabel}</button>
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
const errorDialog2 = function(options) {
    const { id, title, className } = escapeHtmlMany(options, ['id', 'title', 'className']);
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
 * @param id Id of the HTML element
 * @param src URL of the content to load to the iframe
 * @param createOptions extra options to customize the dialog
 * @param className
 */
const appFullscreenContainerIframe = function(options) {
    const { id, src, className } = escapeHtmlMany(options, ['id', 'src', 'className']);
    const addChrome = !!options.createOptions.addChrome;
    return `
        <div id="${id}" 
            class="spark-fullscreen-wrapper ${className} ${addChrome ? "spark-fullscreen-dialog" : ""}"
        >
            ${addChrome ?
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
            <div class="spark-fullscreen-scroll-wrapper ${addChrome ? "spark-fullscreen-haschrome" : ""}">
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
const inlineDialogTrigger = function(options) {
    const { targetId, text } = escapeHtmlMany(options, ['targetId', 'text']);
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
const inlineDialogAppContainer = function(options) {
    const { id, src, className } = escapeHtmlMany(options, ['id', 'src', 'className']);
    const alignment = escapeHtml(options.createOptions.alignment);
    const width = escapeHtml(options.createOptions.width);
    return `
        <aui-inline-dialog id="${id}" class="spark-inline-wrapper ${className}"
            alignment="${alignment}">
                <div style="width:${width}" id="${id + '-iframe-container'}">
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
const bootstrappedIframe = function(options) {
    const { id, src, className } = escapeHtmlMany(options, ['id', 'src', 'className']);
    return `
        <iframe id="${id}" class="${className} spark-iframe" src="${src}" scrolling="no">
        </iframe>
    `;
};

export default {
    appBootstrapContaineriFrame,
    appBootstrapContainerDialog2WithiFrame,
    errorDialog2,
    appFullscreenContainerIframe,
    inlineDialogTrigger,
    inlineDialogAppContainer,
    bootstrappedIframe
};
