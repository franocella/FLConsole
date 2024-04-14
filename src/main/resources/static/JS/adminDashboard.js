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

    } else if (modalId === "exp-modal") {
        // Fields for exp-modal
        $("#config-name-exp-modal").val("");
        $("#FL_config_value").val("FL Configuration");
    }

    // Reset values in the parameters table
    $("#parametersTable tbody").empty();
}

function displayConfigDetailsModal(configId) {
    $("#overlay").show();
    $("body").css("overflow-y", "hidden");

    let modal = $("#config-details-modal").show().empty();
    let modalBody = $("<div>").addClass("myAlertBody").css({"padding-left": "100px", "padding-right": "100px"});
    modalBody.append($("<h3>").text("Configuration").css("margin-bottom", "30px"));

    let table = $("<table>").addClass("table").attr("id", "config-table-details");
    let tableBody = $("<tbody>");

    $.get('/admin/getConfigDetails', {id: configId}, function(response) {
        console.log("Server response:", response);

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

        let closeButton = $("<a>").addClass("btn btn-danger").text("Close").click(closeConfigDetailModal);
        let closeDiv = $("<div>").addClass("text-end mt-5").append(closeButton);
        modalBody.append(closeDiv);
        modal.append(modalBody);
    });
}

function createRow(name, value) {
    const displayName = getDisplayName(name);
    return $("<tr>").html("<td>" + displayName + "</td><td>" + value + "</td>")[0];
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

// Function to close the modal
function closeConfigDetailModal() {
    $("#overlay").hide();
    $("#config-details-modal").hide();
    $("body").css("overflow-y", "auto");
}

function closeErrorModal() {
    $("#overlay-ov").hide();
    $("#error-modal").hide();
    $("#close-error-modal").hide();
}

function displayConfigModal() {
    $("#overlay").show();
    $("#config-modal").show();

    $("body").css("overflow-y", "hidden");
}

function displayExpModal() {
    $("#overlay").show();
    $("#exp-modal").show();

    $("body").css("overflow-y", "hidden");
}

function closeModal() {
    $("#config-modal").hide();
    $("#exp-modal").hide();
    $("#overlay").hide();

    $("body").css("overflow-y", "auto");

    resetModalFields("config-modal");
    resetModalFields("exp-modal");
}

function displayErrorModal(title, params) {
    const modalElement = $("#error-modal");

    modalElement.show();
    $("#overlay-ov").show();
    $("body").css("overflow-y", "hidden");

    // Remove existing content
    modalElement.empty();

    // Create the myAlertBody div
    const alertBodyDiv = $("<div>").addClass("myAlertBody").css("z-index", "9999");

    // Create the h3 element for the error title
    const titleElement = $("<h3>").attr("id", "Err-Title").text(title);

    // Create the p element for the error message
    const messageElement = $("<p>").addClass("mt-3").attr("id", "Err-Message");

    // Construct the HTML content for Err-Message using the JSON parameters
    let errorMessage = "<ul>";
    params.forEach(function (param) {
        errorMessage += "<li>" + param + ": " + "Missing" + "</li>";
    });
    errorMessage += "</ul>";
    messageElement.html(errorMessage);

    // Create the close button
    const closeButton = $("<button>").addClass("btn btn-primary").attr("id", "close-error-modal").text("Close").click(closeErrorModal);

    // Append elements to the myAlertBody div
    alertBodyDiv.append(titleElement, messageElement, closeButton);

    // Append the myAlertBody div to the modal
    modalElement.append(alertBodyDiv);
}


// Function to retrieve configurations or experiments of the current page via an AJAX call
function getData(url, data, pageElement, totalPagesElement, getPageFunction, updateTableFunction, tableId = null) {
    if (pageElement.val() === 0) {
        pageElement.val(0);
    }

    $.get(url, data, function (response) {
        pageElement.val(response.number);
        // Update global variables
        if (pageElement.attr('id') === 'allExpPage') {
            totalAllExpPages = response.totalPages;
        } else if (pageElement.attr('id') === 'expPage') {
            totalExpPages = response.totalPages;
        } else if (pageElement.attr('id') === 'configPage') {
            totalConfigPages = response.totalPages;
        }
        if (tableId) {
            updateTableFunction(response, tableId);
        } else {
            updateTableFunction(response);
        }
    }).fail(function (error) {
        console.error("Error getting data:", error);
        openModal('Error', 'error', 'Error getting data');
    });
}
function submitConfigForm() {
    const formData = {
        name: $("#config-name-modal").val().trim(),
        clientSelectionStrategy: $("#ClientStrategyModal").val(),
        minNumberClients: $("#MinNumberOfClients").val(),
        algorithm: $("#AlgorithmModal").val(),
        stopCondition: $("#StopConditionModal").val(),
        stopConditionThreshold: $("#StopThreshold").val().trim(),
        maxNumberOfRounds: $("#MaxNumRounds").val(),
        codeLanguage: $("#codeLanguage").val(),
        clientSelectionRatio: Number($("#ClientSelectionRatio").val())
    };

    const missingFields = ["name", "clientSelectionStrategy", "minNumberClients", "algorithm",
        "stopCondition", "stopConditionThreshold", "maxNumberOfRounds", "codeLanguage", "clientSelectionRatio"]
        .filter(field => !formData[field] || formData[field] === "Client Strategy" || formData[field] === "Algorithm"
            || formData[field] === "Stop Condition" || formData[field] === "Code Language")
        .map(field => getDisplayName(field));

    if (missingFields.length > 0) {
        openModal('Missing Fields', 'error', missingFields);
        return;
    }

    const parameters = {};
    $("#Form table tbody tr").each(function(index, row) {
        parameters[$(row).find("td:eq(0)").text()] = $(row).find("td:eq(1)").text();
    });
    formData.parameters = parameters;

    $.ajax({
        type: "POST",
        url: "/admin/newConfig",
        contentType: "application/json",
        data: JSON.stringify(formData),
        success: function(response) {
            const jsonData = JSON.parse(response);
            formData.id = jsonData.id;
            formData.creationDate = jsonData.creationTime;
            getMyConfigurations();
            addNewConfigToDropDown(formData);
        },
        error: function(error) {
            console.error("Error:", error);
            openModal('Error', 'error', 'Error creating configuration');
        }
    });
}

function addNewConfigToDropDown(formData) {
    const { id, name, algorithm } = formData;
    $("#FL_config_value").append($("<option>", { value: JSON.stringify({ id, name, algorithm }), text: name })[0]);
}

function addParameterInputField() {
    const table = $("#parametersTable tbody");
    const newRowCount = table.find("tr").length + 1;
    const newRow = $("<tr>").appendTo(table);
    $("<td>", { contentEditable: true, text: "Parameter " + newRowCount }).appendTo(newRow);
    $("<td>", { contentEditable: true, text: "Value " + newRowCount }).appendTo(newRow);
    $("#remove-parameter").show();
}

function removeParameterInputField() {
    const table = $("#parametersTable tbody");
    const rowCount = table.find("tr").length;
    if (rowCount > 0) {
        table.find("tr:last").remove();
    }
    if (rowCount === 1) {
        $("#remove-parameter").hide();
    }
}

function deleteConfig(id) {
    console.log("Deleting config with id:", id);

    $.post('/admin/deleteConfig-' + id, function (response) {
        console.log('Server response:', response);
        $("#" + id).remove();
    }).fail(function (error) {
        console.error('Error deleting config:', error);
        openModal('Error', 'error', 'Error deleting configuration')
    });

    getMyConfigurations();
}

function formatDateString(dateString) {
    if (!dateString) return "";
    return moment(dateString).format('ddd MMM DD HH:mm:ss ZZ YYYY');
}

function submitExpForm() {
    const expName = $("#config-name-exp-modal").val().trim();
    const flConfig = JSON.parse($("#FL_config_value").val());
    const formData = {
        "name": expName,
        "expConfig": {
            "id": flConfig.id,
            "name": flConfig.name,
            "algorithm": flConfig.algorithm
        }
    };

    $.post('/admin/newExp', JSON.stringify(formData), function (response) {
        const jsonData = JSON.parse(response);

        formData["id"] = jsonData.id;
        formData["creationDate"] = jsonData.creationTime;

        getMyExperiments();

    }).fail(function (error) {
        console.error("Error:", error);
        openModal('Error', 'error', 'Error creating experiment');
    });
}

function deleteExp(id) {
    console.log("Deleting experiment with id:", id);

    $.delete('/admin/deleteExp-' + id, function (response) {
        console.log('Server response:', response);
    }).fail(function (error) {
        console.error('Error deleting experiment:', error);
        openModal('Error', 'error', 'Error deleting experiment')
    });

    getMyExperiments();
}

// Function to handle pagination


// Function to retrieve configurations of the current page
function getMyConfigurations(page = 0) {
    const configName = $('#ExpConfigName').val();
    const clientStrategy = $('#ClientStrategy').val();
    const stopCondition = $('#StopCondition').val();
    const algorithm = $('#Algorithm').val();

    getData('/admin/getConfigurations', {
        name: configName,
        clientStrategy: clientStrategy,
        stopCondition: stopCondition,
        algorithm: algorithm,
        page: page
    }, $('#configPage'), totalConfigPages, getMyConfigurations, updateConfigTable);
}

// Function to retrieve experiments of the current page
function getMyExperiments(page = 0) {
    const executionName = $('#execution-name').val();
    const configName = $('#config-name').val();

    getData('/admin/getExperiments', {
        configName: configName,
        executionName: executionName,
        page: page
    }, $('#expPage'), totalExpPages, getMyExperiments, updateExpTable, 'tab2Content');
}

// Function to retrieve all experiments of the current page
function getAllExperiments(page = 0) {
    const executionName = $('#all-execution-name').val();
    const configName = $('#all-config-name').val();

    getData('/getExperiments', {
        configName: configName,
        expName: executionName,
        page: page
    }, $('#allExpPage'), totalAllExpPages, getAllExperiments, updateExpTable, 'tab3Content');
}