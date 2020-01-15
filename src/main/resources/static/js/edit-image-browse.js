$(document).ready(function () {

    // Requires context set in page

    let addFileModal = $('#addFile');
    addFileModal.modal({show: false});
    let deleteInfo = $('#deleteInfo');
    let deleteFileModal = $('#deleteFile');
    deleteFileModal.modal({show: false});

    let fileToDelete;

    $('.fancybox').fancybox();

    $('.add-image-button').on('click', () => {
        addFileModal.modal('show');
        return false;
    });

    $('.thumbnail a.btn-select').on('click', e => {
        selectFile(getFileInfo(e.currentTarget));
        return false;
    });

    $('.thumbnail a.delete-button').on('click', e => {
        openDeleteConfirmation(getFileInfo(e.currentTarget));
        return false;
    });

    deleteFileModal.find('.btn-close').on('click', e => {
        fileToDelete = undefined;
        deleteFileModal.modal('hide');
        return false;
    });

    deleteFileModal.find('.btn-delete').on('click', e => {
        deleteSelectedFile();
        return false;
    });

    addFileModal.find('.btn-close').on('click', e => {
        addFileModal.modal('hide');
    });

    function getFileInfo(el) {
        let data = {};
        data.name = $(el).parents('.thumbnail').data('name');
        data.type = $(el).parents('.thumbnail').data('type');
        data.url = $(el).parents('.thumbnail').data('url');
        return data;
    }

    function selectFile(file) {

        // If this is called by CKEditor return value to that window.
        if (context.ckEditor !== 'None') {
            // update image in CKEditor (HTML Editing window)
            window.opener.CKEDITOR.tools.callFunction(
                context.ckEditorFunctionId, file.url);
            window.close();
        } else {
            // update Image for collection and close parent modal
            window.parent.$('#thumbnailImageInput').val(file.url.replace('/edit/source/', ''));
            window.parent.$('#thumbnailImage').attr('src', file.url);
            window.parent.$('#replaceImageModal').modal('hide');
        }
    }

    function openDeleteConfirmation(file) {
        if (file.type === 'DIRECTORY') {
            deleteInfo.html('<p>Only empty folders can be deleted.</p>');
        } else {
            deleteInfo.html('<p>Please make VERY sure this image is not used in any web pages before deleting it.</p>');
        }

        fileToDelete = file;
        deleteFileModal.modal('show');
    }

    function deleteSelectedFile() {
        if (!fileToDelete)
            throw new Error('No file selected');

        let file = fileToDelete;
        let data = {
            filePath: pathJoin([context.currentDir, fileToDelete.name])
        };

        let jqxhr = $.post('/editor/delete/image', data)
            .done(() => location.reload()) // reload page.
            .fail(() => alert('Unable to delete ' + file.name))
            .always(() => {
                deleteFileModal.modal('hide');
                window.location.reload();
            });
    }

    function pathJoin(parts, sep) {
        let separator = sep || '/';
        let replace = new RegExp(separator + '{1,}', 'g');
        return parts.join(separator).replace(replace, separator);
    }


    function validateAddForm() {

        let upload = document.forms.addFileForm.upload.value;

        upload = upload.replace("C:\\fakepath\\", ""); // Chrome and Safari prepend this, so remove it.

        if (!(/^.*\.(jpg|jpeg|png|gif|bmp)$/i).test(upload)) {
            alert("Select an image file with the file extension .jpg .jpeg .bmp .png or .gif");
            return false;
        }

        if (!(/^[-_A-Za-z0-9]+\.(jpg|jpeg|png|gif|bmp)$/i).test(upload)) {
            alert("The image file name must only contain the characters A-Z or 0-9 or - or _ without spaces.");
            return false;
        }

        let dir = document.forms.addFileForm.directory.value;
        if (!/^[-_/A-Za-z0-9]*$/.test(dir)) {
            alert("Folder name must only contain the characters A-Z or 0-9 or / or - or _ without spaces.");
            return false;
        }

        return true;
    }

    $("#addFileForm").submit(function () {
        let url = `/editor/add/image?CKEditor=${encodeURIComponent(context.ckEditor)}&CKEditorFuncNum=${encodeURIComponent(context.ckEditorFunctionId)}&langCode=${encodeURIComponent(context.language)}`;

        if (validateAddForm()) {

            $.ajax({
                type: "POST",
                url: url,
                data: new FormData(this),
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    window.location.reload(); //reload page.
                }
            });
        }
        return false;
    });
});
