$(document).ready(function () {
    let fileSelectModal = $('#fileSelectModal');
    let previewWindow = $('#previewWindow');

    fileSelectModal.modal({show: false});
    previewWindow.hide();

    $('#filetree_container').fileTree({
        root: gitSourceDataPath,
        script: '/edit/filetree/list'

    }, function (file) {

        displayFileOptions(file);

    });

    function displayPreview() {

        let file = $('#previewFilePath').val();
        let filename = file.split('/').pop();

        $.post("/edit/filetree/get", {filepath: file}, function (data) {
            if (filename.toLowerCase().endsWith(".json") || filename.toLowerCase().endsWith(".json5")) {
                displayJSON(data);
            } else if (filename.toLowerCase().endsWith(".xml")) {
                displayXML(data);
            }

        });
        $('#selectedFileHeader').text(filename);
        fileSelectModal.modal('hide');
        previewWindow.show();
        return false;
    }

    function displayFileOptions(file) {
        let filename = file.split('/').pop();

        $('#fileSelectFileName').text(filename);
        let submitButton = $('#previewSubmit');
        submitButton.hide();
        $('#confirmDownloadFile input[name="filepath"]').val(file);
        $('#confirmRenameFile input[name="filepath"]').val(file);
        $('#confirmDeleteFile input[name="filepath"]').val(file);
        $('#previewFilePath').val(file);

        // Enable preview option for .json files.
        if (filename.toString().toLowerCase().endsWith(".json") ||
            filename.toString().toLowerCase().endsWith(".json5") ||
            filename.toString().toLowerCase().endsWith(".xml")
        ) {
            $('#confirmPreviewFile').on('submit', function () {
                return displayPreview();
            });
            submitButton.show();
        }

        fileSelectModal.modal('show');
    }

    function displayJSON(json) {
        if (typeof json == "object") {
            json = JSON5.stringify(json, null, 2);
        }
        let preview = $('#previewFile');
        preview.text(json);
        preview.removeAttr("class");
        preview.attr("class", "language-json");
        window.Prism.highlightAll();

    }

    function displayXML(xml) {
        if (typeof xml == "object") {
            let serializer = new XMLSerializer();
            xml = serializer.serializeToString(xml);
        }
        let preview = $('#previewFile');
        preview.text(xml);
        preview.removeAttr("class");
        preview.attr("class", "language-xml");
        window.Prism.highlightAll();
    }

});
