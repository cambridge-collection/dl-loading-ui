CKEDITOR.dialog.add('mediaembedDialog', function (editor) {
    function getVal(dialog, id) { return dialog.getValueOf('info', id); }
    function setVal(dialog, id, v) { dialog.setValueOf('info', id, v || ''); }

    var FIELD_IDS = ['itemid','playlistid','permalink','playlabel','class'];
    var SERVICE_CONFIG = {
        youtube:    { show: ['itemid','playlistid','class'], requireAtLeastOneOf: ['itemid','playlistid'] },
        vimeo:      { show: ['itemid','playlistid','class'], requireAtLeastOneOf: ['itemid'] },
        soundcloud: { show: ['itemid','permalink','class'], requireAtLeastOneOf: ['itemid'] }
    };
    function getConfig(service) {
        return SERVICE_CONFIG[service] || { show: FIELD_IDS.slice(), requireAtLeastOneOf: ['itemid','playlistid'] };
    }
    function updateVisibility(dialog) {
        try {
            var service = getVal(dialog, 'service') || 'youtube';
            var cfg = getConfig(service);
            var touchedRows = [];

            function findAncestorTag(node, tag) {
                var n = node;
                tag = tag.toLowerCase();
                while (n && n.getName && n.getName()) {
                    if (n.getName().toLowerCase() === tag) return n;
                    n = n.getParent && n.getParent();
                }
                return null;
            }

            FIELD_IDS.forEach(function(id) {
                var ui = dialog.getContentElement('info', id);
                if (!ui || !ui.getElement) return;
                var elem = ui.getElement();
                var td = findAncestorTag(elem, 'td');
                var tr = findAncestorTag(elem, 'tr');
                var visible = cfg.show.indexOf(id) !== -1;

                if (td) td.setStyle('display', visible ? '' : 'none');
                else if (tr) tr.setStyle('display', visible ? '' : 'none');

                if (!visible) { ui.setValue(''); }

                if (tr) touchedRows.push(tr);
            });

            // After toggling cells, hide a row only if all its cells are hidden
            touchedRows.forEach(function(tr) {
                try {
                    var tds = tr.find('td');
                    var anyVisible = false;
                    for (var i = 0; i < tds.count(); i++) {
                        var td = tds.getItem(i);
                        if (td.getStyle('display') !== 'none') { anyVisible = true; break; }
                    }
                    tr.setStyle('display', anyVisible ? '' : 'none');
                } catch (_) { /* no-op */ }
            });
        } catch (e) { /* no-op */ }
    }

    return {
        title: 'Insert/Edit Media Embed',
        minWidth: 400,
        minHeight: 50,
        contents: [
            {
                id: 'info',
                label: 'Media',
                elements: [
                    {
                        type: 'select', id: 'service', label: 'Service', required: true,
                        items: [ ['YouTube','youtube'], ['Vimeo','vimeo'], ['SoundCloud','soundcloud'] ],
                        setup: function (widget) { this.setValue(widget.data.service || 'youtube'); },
                        commit: function (widget) { widget.setData('service', this.getValue()); },
                        onChange: function() { updateVisibility(this.getDialog()); }
                    },
                    {
                        type: 'text', id: 'title', label: 'Title', required: true,
                        validate: CKEDITOR.dialog.validate.notEmpty('Title is required.'),
                        setup: function (widget) { this.setValue(widget.data.title || ''); },
                        commit: function (widget) { widget.setData('title', this.getValue()); }
                    },
                    {
                        type: 'hbox', widths: ['50%','50%'], children: [
                            { type: 'text', id: 'itemid', label: 'Item ID',
                              setup: function (widget) { this.setValue(widget.data.itemid || ''); },
                              commit: function (widget) { widget.setData('itemid', this.getValue()); }
                            },
                            { type: 'text', id: 'playlistid', label: 'Playlist ID',
                              setup: function (widget) { this.setValue(widget.data.playlistid || ''); },
                              commit: function (widget) { widget.setData('playlistid', this.getValue()); }
                            }
                        ]
                    },
                    { type: 'text', id: 'permalink', label: 'Permalink',
                      setup: function (widget) { this.setValue(widget.data.permalink || ''); },
                      commit: function (widget) { widget.setData('permalink', this.getValue()); }
                    },
                    { type: 'text', id: 'playlabel', label: 'Play Button Label',
                      setup: function (widget) { this.setValue(widget.data.playlabel || ''); },
                      commit: function (widget) { widget.setData('playlabel', this.getValue()); }
                    },
                    { type: 'text', id: 'class', label: 'CSS Class',
                      setup: function (widget) { this.setValue(widget.data.class || ''); },
                      commit: function (widget) { widget.setData('class', this.getValue()); }
                    }
                ]
            }
        ],
        onLoad: function() {
            updateVisibility(this);
        },
        onShow: function () {
            var ed = this._.editor;
            var widget = ed.widgets && ed.widgets.focused;
            this.widget = widget || null;

            if (this.widget) {
                this.setupContent(this.widget);
                this._mediaElement = null;
                return;
            }

            // Determine target without inserting anything yet to avoid duplicates
            var target = ed._mediaembedTarget || null;
            if (!target) {
                var sel = ed.getSelection();
                var start = sel && sel.getStartElement && sel.getStartElement();
                target = start && start.getAscendant && start.getAscendant('media-embed', true);
            }
            this._mediaElement = target || null;

            if (this._mediaElement) {
                var el = this._mediaElement;
                setVal(this, 'service', el.getAttribute('service') || 'youtube');
                setVal(this, 'title', el.getAttribute('title') || '');
                setVal(this, 'itemid', el.getAttribute('itemid') || '');
                setVal(this, 'playlistid', el.getAttribute('playlistid') || '');
                setVal(this, 'permalink', el.getAttribute('permalink') || '');
                setVal(this, 'playlabel', el.getAttribute('playlabel') || '');
                setVal(this, 'class', el.getAttribute('class') || '');
            } else {
                // Default values for new element
                setVal(this, 'service', 'youtube');
                setVal(this, 'title', '');
                setVal(this, 'itemid', '');
                setVal(this, 'playlistid', '');
                setVal(this, 'permalink', '');
                setVal(this, 'playlabel', '');
                setVal(this, 'class', '');
            }
            updateVisibility(this);
        },
        onOk: function () {
            // Cross-field validation: service+title required; service-specific rules
            var title = getVal(this, 'title');
            var service = getVal(this, 'service');
            var itemid = getVal(this, 'itemid');
            var playlistid = getVal(this, 'playlistid');

            if (!service) {
                alert('Service is required.');
                return false;
            }
            if (!title || !title.trim()) {
                alert('Title is required.');
                return false;
            }
            var cfg = getConfig(service);
            if (cfg && cfg.requireAtLeastOneOf) {
                var hasOne = cfg.requireAtLeastOneOf.some(function(fid){
                    var v = getVal(this, fid);
                    return v && (''+v).trim().length > 0;
                }, this);
                if (!hasOne) {
                    alert('Please provide at least one of: ' + cfg.requireAtLeastOneOf.join(', '));
                    return false;
                }
            }

            if (this.widget) {
                this.commitContent(this.widget);
                var elw = this.widget.element; if (elw) elw.setAttribute('contenteditable', 'false');
            } else {
                var el = this._mediaElement || new CKEDITOR.dom.element('media-embed');
                function setOrRemove(name, val) {
                    if (val && ('' + val).length > 0) el.setAttribute(name, val); else el.removeAttribute(name);
                }
                setOrRemove('service', service);
                setOrRemove('title', title);
                setOrRemove('itemid', itemid);
                setOrRemove('playlistid', playlistid);
                setOrRemove('permalink', getVal(this, 'permalink'));
                setOrRemove('playlabel', getVal(this, 'playlabel'));
                setOrRemove('class', getVal(this, 'class'));
                el.setAttribute('contenteditable', 'false');
                if (!this._mediaElement) {
                    this._.editor.insertElement(el);
                }
            }
            this._.editor._mediaembedTarget = null;
        }
    };
});
