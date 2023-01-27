
$(document).ready(function () {

    // Setup HTML editor
    CKEDITOR.replace('html', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 500,
        removeButtons: 'About',
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet( '/css/cudl.css' );
            }
        }
    });

    CKEDITOR.config.allowedContent=true;
});



