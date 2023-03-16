
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

    // Setup HTML editor
    CKEDITOR.replace('html', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 500,
        removeButtons: 'About',
        allowedContent: true,
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet( '/css/cudl.css' );
            }
        }
    });

    CKEDITOR.config.allowedContent=true;
    CKEDITOR.dtd.$removeEmpty.span = 0;
});



