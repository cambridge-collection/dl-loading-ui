let loading_ui_edit_collection = {};

$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    let deleteItemModal = $('#deleteItemModal');
    deleteItemModal.modal({show: false});

    let replaceImageModal = $('#replaceImageModal');
    replaceImageModal.modal({show: false});

    $('#dataTable').DataTable({
        paging: false,
        searching: false,
        info: false,
        lengthChange: false
    });

    $('#collectioneditform-submit').on('click', function () {
        updateModal.modal('show');
        $('#collectioneditform').submit();
    });
    $('#collectioneditform-reset').on('click', function () {
        $('#collectioneditform')[0].reset();
    });
    $('#addItemFile').on('change', function () {
        updateModal.modal('show');
        $('#addItemFileForm').submit();
    });
    $('#replaceImageModalButton').on('click', function () {
        replaceImageModal.modal('show');
        return false;
    });

    function updateReleaseStatusColour() {
        var select = $('#releaseStatus');
        select.toggleClass('release-status-released', select.val() === 'released');
        select.toggleClass('release-status-draft', select.val() !== 'released');
    }

    $('#releaseStatus').on('change', updateReleaseStatusColour);
    updateReleaseStatusColour();

    function renumberPositions() {
        var table = $('#dataTable');
        var currentPage = parseInt(table.data('current-page') || 0, 10);
        var pageSize = parseInt(table.data('page-size') || 0, 10);
        table.find('tbody tr').each(function (index) {
            var position = (currentPage * pageSize) + index + 1;
            $(this).find('.item-position').text(position);
        });
    }

    function updateReorderButtons() {
        var table = $('#dataTable');
        var rows = table.find('tbody tr');
        rows.find('.move-up').prop('disabled', false);
        rows.find('.move-down').prop('disabled', false);
        rows.first().find('.move-up').prop('disabled', true);
        rows.last().find('.move-down').prop('disabled', true);
    }

    loading_ui_edit_collection.moveItem = function (button, direction) {
        var $button = $(button);
        var row = $button.closest('tr');
        var tbody = row.closest('tbody');
        var target = direction === 'up' ? row.prev('tr') : row.next('tr');

        if (target.length === 0) {
            return false;
        }

        // Reorder rows in the DOM
        if (direction === 'up') {
            row.insertBefore(target);
        } else {
            row.insertAfter(target);
        }

        // Update item order hidden field for items on this page
        var orderInput = $('#collectionItemIds');
        if (orderInput.length) {
            var current = orderInput.val() || '';
            if (current.length > 0) {
                var order = current.split(',');

                // Map from id -> index in global order
                var indexById = {};
                for (var i = 0; i < order.length; i++) {
                    indexById[order[i]] = i;
                }

                // IDs in this page in new DOM order
                var pageIds = [];
                tbody.find('tr').each(function () {
                    pageIds.push($(this).data('item-id'));
                });

                // Corresponding indices in global order, sorted so we keep the same positions
                var indices = pageIds.map(function (id) {
                    return indexById[id];
                }).sort(function (a, b) { return a - b; });

                for (var j = 0; j < indices.length; j++) {
                    order[indices[j]] = pageIds[j];
                }

                orderInput.val(order.join(','));
            }
        }

        renumberPositions();
        updateReorderButtons();

        // Show one-time hint that Update is needed to save
        var hint = $('#reorder-hint');
        if (hint.length && hint.hasClass('d-none')) {
            hint.removeClass('d-none');
        }
        return false;
    };

    $('#dataTable').on('click', '.move-up', function () {
        return loading_ui_edit_collection.moveItem(this, 'up');
    });

    $('#dataTable').on('click', '.move-down', function () {
        return loading_ui_edit_collection.moveItem(this, 'down');
    });

    loading_ui_edit_collection.showDeleteModal = function (button) {
        deleteItemModal.modal('show');
        let form = button.form;
        $('#confirmDeleteItemButton').on('click', function () {
            deleteItemModal.modal('hide');
            updateModal.modal('show');
            form.submit();
        });
        return false;
    };

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
    CKEDITOR.replace('description.full', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 500,
        removeButtons: 'About',
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

    CKEDITOR.replace('credit.prose', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 300,
        removeButtons: 'About',
        extraPlugins: (CKEDITOR.config.extraPlugins ? CKEDITOR.config.extraPlugins + ',mediaembed' : 'mediaembed'),
        contentsCss: ['/css/cudl.css', '/js/mediaEmbed/mediaEmbed.min.css'],
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet('/css/cudl.css');
                injectMediaEmbed(this);
            },
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

    // Initialise reorder UI
    renumberPositions();
    updateReorderButtons();
});
