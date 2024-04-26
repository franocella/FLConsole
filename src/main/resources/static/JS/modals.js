function resetModalFields(modalId) {
    // Reset the values of the fields in the modal
    if (modalId === "config-modal") {
        // Fields for config-modal
        $("#config-name-modal").val("");
        $("#AlgorithmModal").val("Algorithm");
        $("#codeLanguage").val("Code Language");
        $("#ClientStrategyModal").val("Client Strategy");
        $("#ClientSelectionRatio").val("");
        $("#MinNumberOfClients").val("");
        $("#StopConditionModal").val("Stop Condition");
        $("#StopThreshold").val("");
        $("#MaxNumRounds").val("");
        $("#parametersTable tbody").empty();
        $("#remove-parameter").hide();

    } else if (modalId === "exp-modal") {
        // Fields for exp-modal
        $("#config-name-exp-modal").val("");
        $("#FL_config_value").val("FL Configuration");
    }
}

function openModal(title, type, params) {
    const overlay = $("#overlay");
    const body = $("body");

    overlay.show();
    body.css("overflow-y", "hidden");

    switch (type) {
        case 'error':
            displayErrorModal(title, params);
            break;
        case 'config':
            showFormModal(type);
            break;
        case 'exp':
            showFormModal(type);
            break;
        case 'config-details':
            displayConfigDetailsModal(params);
            break;
        default:
            console.error('Unknown modal type:', type);
    }
}

function displayConfigDetailsModal(configId) {
    $("#overlay").show();
    $("body").css("overflow-y", "hidden");

    let modal = $("#config-details-modal").show().empty();
    let modalBody = $("<div>").addClass("myAlertBody").css({"padding-left": "100px", "padding-right": "100px"});
    modalBody.append($("<h3>").text("Configuration"));

    let table = $("<table>").addClass("table").attr("id", "config-table-details");
    let tableBody = $("<tbody>");

    $.get('/FLConsole/admin/getConfigDetails', {id: configId}, function(response) {
        $.each(response, function(key, value) {
            if (key === "parameters") {
                $.each(value, function(paramKey, paramValue) {
                    tableBody.append(createRow(paramKey, paramValue));
                });
            } else {
                tableBody.append(createRow(key, value));
            }
        });
    }).fail(function(error) {
        console.error("Error getting config details:", error);
    }).always(function() {
        table.append(tableBody);
        modalBody.append(table);

        let closeButton = $("<a>").addClass("btn btn-danger").text("Close")
            .on('click', () => closeModal("config-details"));

        let closeDiv = $("<div>").addClass("text-end mt-5").append(closeButton);
        modalBody.append(closeDiv);
        modal.append(modalBody);
    });
}

function createRow(name, value) {
    const displayName = getDisplayName(name);
    if (name === "creationDate") {
        return $("<tr>").html("<td>" + displayName + "</td><td>" + formatDateString(value) + "</td>")[0];
    } else {
        return $("<tr>").html("<td>" + displayName + "</td><td>" + value + "</td>")[0];
    }
}

function getDisplayName(key) {
    const displayNames = {
        "id": "ID",
        "name": "Name",
        "algorithm": "Algorithm",
        "codeLanguage": "Code Language",
        "clientSelectionStrategy": "Client Selection Strategy",
        "clientSelectionRatio": "Client Selection Ratio",
        "minNumberClients": "Min Number of Clients",
        "stopCondition": "Stop Condition",
        "stopConditionThreshold": "Stop Condition Threshold",
        "maxNumberOfRounds": "Max Number of Rounds"
    };

    return displayNames[key] || key;
}

function showFormModal(type) {
    $('#' + type + '-modal').show();
    $("#overlay").show();
    $("body").css("overflow-y", "hidden");
}

function displayErrorModal(title, params) {
    const modalElement = $("#error-modal").empty().show();
    $("#overlay-ov").show();
    $("body").css("overflow-y", "hidden");
    const closeButton = $("<button>").addClass("btn btn-danger").attr("id", "close-error-modal").text("Close")
        .click(() => closeModal("error"));

    // append a li with key and value for each parameter
    if (typeof params === "object") {
        const errorMessage = $("<ul>");
        for (const key in params) {
            errorMessage.append($("<li>").text(key + ": " + params[key]));
        }

        modalElement.append(
            $("<div>").addClass("myAlertBody").css("z-index", "9999").append(
                $("<h3>").attr("id", "Err-Title").text(title),
                $("<p>").addClass("mt-3").attr("id", "Err-Message").append(errorMessage)
            )
        );
    // append a simple message
    } else if (typeof params === "string") {
        modalElement.append(
            $("<div>").addClass("myAlertBody").css("z-index", "9999").append(
                $("<h3>").attr("id", "Err-Title").text(title),
                $("<p>").addClass("mt-3").attr("id", "Err-Message").append(params)
            )
        );
    }

    modalElement.find(".myAlertBody").append(closeButton);
}

function closeModal(type) {
    $('#' + type + '-modal').hide();
    type === 'error' ? $("#overlay-ov").hide() : $("#overlay").hide();
    $('body').css('overflow-y', 'auto');
    if (type === 'config' || type === 'exp') {
        resetModalFields(type + "-modal");
    }
}