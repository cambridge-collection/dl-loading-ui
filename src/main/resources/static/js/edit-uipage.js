
$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    $('#uipageform-submit').on('click', function () {
        updateModal.modal('show');
        $('#uipageform').submit();
    });

    $('#uipageform-reset').on('click', function () {
        $('#uipageform')[0].reset();
    });

    // Ensure preview popup includes custom element assets
    CKEDITOR.config.previewTemplate = '<!DOCTYPE html>' +
        '<html dir="%dir%" lang="%lang%">' +
        '<head>' +
        '<meta charset="utf-8">' +
        '<title>%title%</title>' +
        '<base href="%base%">' +
        '<link rel="stylesheet" href="/css/cudl.css">' +
        '<link rel="stylesheet" href="/js/mediaEmbed/mediaEmbed.min.css">' +
        '</head>' +
        '<body>' +
        '%content%' +
        '<script src="/js/mediaEmbed/mediaEmbed.min.js"></script>' +
        '</body>' +
        '</html>';

    // Reinitialize any existing <media-embed> elements in the iframe
    function refreshMediaEmbeds(editor) {
        try {
            var iframeWin = editor.window && editor.window.$;
            if (!iframeWin || !iframeWin.customElements || !iframeWin.customElements.get('media-embed')) return;
            var nodes = editor.document.$.querySelectorAll('media-embed');
            nodes.forEach(function (el) {
                try {
                    if (typeof el.initialize === 'function') {
                        el.initialize();
                    }
                } catch (_) { /* no-op */ }
            });
        } catch (_) { /* no-op */ }
    }

    // Ensure the media-embed custom element is available inside the CKEditor iframe
    function injectMediaEmbed(editor) {
        try {
            // Always ensure CSS is present in the iframe
            editor.document.appendStyleSheet('/js/mediaEmbed/mediaEmbed.min.css');

            // If the iframe already knows about the element, skip reloading the script
            var iframeWin = editor.window && editor.window.$;
            var hasCustomElements = iframeWin && iframeWin.customElements && typeof iframeWin.customElements.get === 'function';
            var alreadyDefined = hasCustomElements && iframeWin.customElements.get('media-embed');
            if (alreadyDefined) {
                // If already defined, refresh any existing nodes to ensure preview renders
                refreshMediaEmbeds(editor);
                return;
            }

            // Load JS into the editor iframe head
            var head = editor.document.getHead();
            var scriptEl = new CKEDITOR.dom.element('script', editor.document);
            scriptEl.setAttribute('type', 'text/javascript');
            scriptEl.setAttribute('src', '/js/mediaEmbed/mediaEmbed.min.js');
            // After script loads and defines the element, refresh to render previews
            scriptEl.on('load', function () { refreshMediaEmbeds(editor); });
            head.append(scriptEl);
        } catch (e) {
            if (window && window.console) {
                console.warn('Failed to inject mediaEmbed into CKEditor iframe:', e);
            }
        }
    }

    // Register external CKEditor plugin for <media-embed/>
    if (CKEDITOR && CKEDITOR.plugins && CKEDITOR.plugins.addExternal) {
        CKEDITOR.plugins.addExternal('mediaembed', '/js/ckeditor/plugins/mediaembed/', 'plugin.js');
    }

    // Setup HTML editor
    CKEDITOR.replace('html', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 500,
        removeButtons: 'About',
        allowedContent: true,
        extraPlugins: (CKEDITOR.config.extraPlugins ? CKEDITOR.config.extraPlugins + ',mediaembed' : 'mediaembed'),
        contentsCss: ['/css/cudl.css', '/js/mediaEmbed/mediaEmbed.min.css'],
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet('/css/cudl.css');
                injectMediaEmbed(this);
            },
            // CKEditor rebuilds the iframe DOM; re-inject as needed
            contentDom: function() {
                injectMediaEmbed(this);
            },
            dataReady: function() {
                injectMediaEmbed(this);
            },
            mode: function() {
                if (this.mode === 'wysiwyg') {
                    injectMediaEmbed(this);
                }
            }
        }
    });

    CKEDITOR.config.allowedContent=true;
    CKEDITOR.dtd.$removeEmpty.span = 0;
});
