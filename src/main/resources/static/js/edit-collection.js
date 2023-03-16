let loading_ui_edit_collection = {};

$(document).ready(function () {

    let updateModal = $('#updateModal');
    updateModal.modal({show: false});

    let deleteItemModal = $('#deleteItemModal');
    deleteItemModal.modal({show: false});

    let replaceImageModal = $('#replaceImageModal');
    replaceImageModal.modal({show: false});

    $('#dataTable').DataTable({
        paging: true,
        searching: true
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

    // Setup HTML editor
    CKEDITOR.replace('description.full', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 500,
        removeButtons: 'About',
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet( '/css/cudl.css' );
            }
        }
    });

    CKEDITOR.replace('credit.prose', {
        filebrowserImageBrowseUrl: '/editor/browse/images',
        height: 300,
        removeButtons: 'About',
        on: {
            instanceReady: function() {
                this.document.appendStyleSheet( '/css/cudl.css' );
            }
        }
    });

    CKEDITOR.config.allowedContent=true;
});



