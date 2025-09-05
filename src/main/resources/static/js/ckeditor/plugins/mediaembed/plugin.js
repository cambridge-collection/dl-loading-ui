CKEDITOR.plugins.add('mediaembed', {
    requires: 'dialog,contextmenu',
    icons: 'mediaembed',
    init: function (editor) {
        CKEDITOR.dialog.add('mediaembedDialog', this.path + 'dialogs/mediaembed.js');

        if (editor.widgets) {
            editor.widgets.add('mediaembed', {
                allowedContent: 'media-embed[service,title,itemid,playlistid,permalink,playlabel,class]',
                requiredContent: 'media-embed[service,title]',
                upcast: function (el) { return el.name === 'media-embed'; },
                dialog: 'mediaembedDialog',
                init: function () {
                    var attrs = ['service','title','itemid','playlistid','permalink','playlabel','class'];
                    for (var i = 0; i < attrs.length; i++) {
                        var a = attrs[i];
                        var v = this.element.getAttribute(a);
                        if (v) this.setData(a, v);
                    }
                    this.element.setAttribute('contenteditable', 'false');
                },
                data: function () {
                    var attrs = ['service','title','itemid','playlistid','permalink','playlabel','class'];
                    for (var i = 0; i < attrs.length; i++) {
                        var a = attrs[i];
                        var v = this.data[a];
                        if (v && ('' + v).length > 0) {
                            this.element.setAttribute(a, v);
                        } else {
                            this.element.removeAttribute(a);
                        }
                    }
                    this.element.setAttribute('contenteditable', 'false');
                }
            });
        }

        editor.addCommand('mediaembed', {
            allowedContent: 'media-embed[service,title,itemid,playlistid,permalink,playlabel,class]',
            requiredContent: 'media-embed[service,title]',
            exec: function (editor) {
                var selection = editor.getSelection();
                var element = selection && selection.getStartElement && selection.getStartElement();
                var mediaEl = element && element.getAscendant && element.getAscendant('media-embed', true);
                // Stash target element (if any) so the dialog can pick it up without inserting duplicates
                editor._mediaembedTarget = mediaEl || null;
                editor.openDialog('mediaembedDialog');
            }
        });

        editor.ui.addButton('MediaEmbed', {
            label: 'Media Embed',
            command: 'mediaembed',
            toolbar: 'insert',
            icon: 'mediaembed'
        });

        if (editor.contextMenu) {
            editor.addMenuGroup('mediaembedGroup');
            editor.addMenuItem('mediaembedEdit', {
                label: 'Edit Media Embed',
                command: 'mediaembed',
                group: 'mediaembedGroup',
                icon: this.path + 'icons/mediaembed.svg'
            });
            editor.addMenuItem('mediaembedRemove', {
                label: 'Remove Media Embed',
                group: 'mediaembedGroup',
                icon: this.path + 'icons/mediaembed.svg',
                onClick: function () {
                    var sel = editor.getSelection();
                    var start = sel && sel.getStartElement && sel.getStartElement();
                    var el = start && start.getAscendant && start.getAscendant('media-embed', true);
                    if (el) el.remove();
                }
            });
            editor.contextMenu.addListener(function (element) {
                var el = element && element.getAscendant && element.getAscendant('media-embed', true);
                if (el) {
                    return { mediaembedEdit: CKEDITOR.TRISTATE_OFF, mediaembedRemove: CKEDITOR.TRISTATE_OFF };
                }
                return null;
            });
        }

        editor.on('doubleclick', function(evt) {
            var el = evt.data && evt.data.element && evt.data.element.getAscendant && evt.data.element.getAscendant('media-embed', true);
            if (el) {
                editor.execCommand('mediaembed');
                evt.cancel();
            }
        });

        function lockMediaEmbeds() {
            try {
                var list = editor.document && editor.document.$ && editor.document.$.querySelectorAll('media-embed');
                if (!list) return;
                list.forEach(function (el) { el.setAttribute('contenteditable', 'false'); });
            } catch (_) { /* no-op */ }
        }

        editor.on('contentDom', function () {
            lockMediaEmbeds();
            var ed = editor.editable();
            ed.attachListener(ed, 'keyup', lockMediaEmbeds);
            ed.attachListener(ed, 'click', function (evt) {
                lockMediaEmbeds();
                try {
                    var target = evt.data.getTarget();
                    var el = target && target.getAscendant && target.getAscendant('media-embed', true);
                    if (el) {
                        editor.getSelection().selectElement(el);
                    }
                } catch (_) { /* no-op */ }
            });
            ed.attachListener(ed, 'paste', lockMediaEmbeds);
            ed.attachListener(ed, 'drop', lockMediaEmbeds);

            ed.attachListener(ed, 'keydown', function (evt) {
                var key = evt.data.getKey && evt.data.getKey();
                if (key !== 8 && key !== 46) return; // backspace/delete

                var sel = editor.getSelection();
                var start = sel && sel.getStartElement && sel.getStartElement();
                var el = (sel && sel.getSelectedElement && sel.getSelectedElement()) ||
                         (start && start.getAscendant && start.getAscendant('media-embed', true));
                if (!el) return;

                el.remove();
                evt.data.preventDefault();
            });
        });
    }
});
