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

    const missingFieldsMap = {};
    missingFields.forEach(field => {
        missingFieldsMap[field] = "Missing";
    });

    if (missingFields.length > 0) {
        openModal("Missing Fields", 'error', missingFieldsMap);
        return;
    }

    const parameters = {};
    $("#Form table tbody tr").each(function(index, row) {
        parameters[$(row).find("td:eq(0)").text()] = $(row).find("td:eq(1)").text();
    });
    formData.parameters = parameters;

    $.ajax({
        type: "POST",
        url: "/FLConsole/admin/newConfig",
        contentType: "application/json",
        data: JSON.stringify(formData),
        success: () => {
            getMyConfigurations();
            addNewConfigToDropDown(formData);
            closeModal('config');
        },
        error: function(error) {
            console.error("Error:", error);
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
    $.post('/FLConsole/admin/deleteConfig-' + id, () => $("#" + id).remove())
        .fail(error => console.error('Error deleting config:', error));
    getMyConfigurations();
}

function submitExpForm() {
    const flConfig = JSON.parse($("#FL_config_value").val());
    const formData = {
        "name": $("#config-name-exp-modal").val().trim(),
        "expConfig": {
            "id": flConfig.id,
            "name": flConfig.name,
            "algorithm": flConfig.algorithm
        }
    };

    $.ajax({
        url: '/FLConsole/admin/newExp',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: () => {
            getMyExperiments();
            closeModal('exp');
        },
        error: error => console.error("Error:", error)
    });
}

function deleteExp(id) {
    $.post('/FLConsole/admin/deleteExp-' + id)
        .done(() => getMyExperiments())
        .fail(error => console.error('Error deleting experiment:', error));
}

function getMyConfigurations(page = 0) {
    const configName = $('#ExpConfigName').val();
    const clientStrategy = $('#ClientStrategy').val();
    const stopCondition = $('#StopCondition').val();
    const algorithm = $('#Algorithm').val();

    getData('/FLConsole/admin/getConfigurations', {
        name: configName,
        clientStrategy: clientStrategy,
        stopCondition: stopCondition,
        algorithm: algorithm,
        page: page
    }, $('#configPage'), updateConfigTable);
}

// Function to retrieve experiments of the current page
function getMyExperiments(page = 0) {
    const executionName = $('#execution-name').val();
    const configName = $('#config-name').val();

    getData('/FLConsole/admin/getExperiments', {
        configName: configName,
        executionName: executionName,
        page: page
    }, $('#expPage'), updateExpTable, 'tab2Content');
}

// Function to retrieve configurations or experiments of the current page via an AJAX call
function getData(url, data, pageElement, updateTableFunction, tableId = null) {
    if (pageElement.val() === 0) {
        pageElement.val(0);
    }

    $.get(url, data, function (response) {
        pageElement.val(response.number);
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
    });
}

function updateExpTable(response, tableId) {
    const tbody = $('#' + tableId + ' tbody').empty();
    const configurations = response.content;
    $.each(configurations, function (_, item) {
        const row = $('<tr>').append(
            '<td class="align-middle">' + item.id + '</td>' +
            '<td class="align-middle">' + item.name + '</td>' +
            '<td class="align-middle">' + (tableId === 'tab2Content' ? item.configName : item.expConfig.name) + '</td>' +
            '<td class="align-middle">' + formatDateString(item.creationDate) + '</td>' +
            '<td class="align-middle"><a href="/FLConsole/experiment-' + item.id + '"><img src="' + contextPath + '/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>' +
            (tableId === 'tab2Content' ?
                '<td class="align-middle"><figure class="m-0"><img src="' + contextPath + '/Images/icon_delete.svg" alt="Delete" onclick="deleteExp(\'' + item.id + '\')" height="20px" width="20px"></figure></td>' :
                '')
        );
        tbody.append(row);
    });
}

// Function to update configuration table
function updateConfigTable(response) {
    const tbody = $('#ConfigTable tbody').empty();
    const configurations = response.content;
    $.each(configurations, function (index, item) {
        const row = $('<tr>').append(
            '<td class="align-middle">' + item.id + '</td>' +
            '<td class="align-middle">' + item.name + '</td>' +
            '<td class="align-middle">' + item.algorithm + '</td>' +
            '<td class="align-middle">' + formatDateString(item.creationDate) + '</td>' +
            '<td class="align-middle"><img src="' + contextPath + '/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="openModal(\'\', \'config-details\', \'' + item.id + '\')"></td>' +
            '<td class="align-middle"><figure class="m-0"><img src="' + contextPath + '/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + item.id + '\')" height="20px" width="20px"></figure></td>'
        );
        tbody.append(row);
    });
}