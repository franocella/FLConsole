<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Admin Dashboard</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />

</head>

<body style="background-color: #f8f8fe;">

    <!-- Header section with navbar -->
    <%@ include file="components/header.txt" %>


    <!-- Overlay -->
    <div id="overlay" class="overlay"></div>
    <div id="overlay-ov" class="overlay-ov"></div>

    <!-- Error modal -->
    <div id="error-modal" class="myAlert-sm" style="z-index: 9999"></div>

    <!-- Config details modal -->
    <div id="config-details-modal" class="myAlert" style="z-index: 9997"></div>

    <!-- New config modal  -->
    <div id="config-modal" class="myAlert">
        <div class="myAlertBody" style="padding-left:100px; padding-right:100px;">
            <h3 style="margin-bottom: 25px;">New FL configuration</h3>
            <form id="Form" action="" method="post" class="align-items-center">
                <input type="text" class="form-control me-2 my-2" name="config-name-modal" id="config-name-modal"
                    required placeholder="Configuration Name" />

                <select id="AlgorithmModal" class="form-select me-2">
                    <option selected>Algorithm</option>
                    <option value="fcmeans">Fcmeans</option>
                </select>

                <select id="codeLanguage" class="form-select me-2 my-2">
                    <option selected>Code Language</option>
                    <option value="java">Java</option>
                    <option value="python">Python</option>
                </select>

                <select id="ClientStrategyModal" class="form-select me-2 my-2">
                    <option selected>Client Strategy</option>
                    <option value="probability">Probability</option>
                    <option value="ranking">Ranking</option>
                    <option value="threshold">Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="ClientSelectionRatio" min="0" max="1" step="0.00001"
                       id="ClientSelectionRatio" required placeholder="Client Selection Ratio" />

                <input type="number" class="form-control me-2 my-2" name="MinNumberOfClients" step="1"
                    id="MinNumberOfClients" required placeholder="Minimum Number of Clients" />

                <select id="StopConditionModal" class="form-select me-2 my-2">
                    <option selected>Stop Condition</option>
                    <option value="custom">Custom</option>
                    <option value="metric_under_threshold">Metric Under Threshold</option>
                    <option value="metric_over_threshold">Metric Over Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="0.00001" min="0"
                    max="1" id="StopThreshold" required placeholder="Stop Condition Threshold" />

                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="1" min="1"
                       max="30" id="MaxNumRounds" required placeholder="Maximum Number of Rounds" />

                <table id="parametersTable" class="table mt-3 text-center my-2">
                    <thead>
                        <tr>
                            <th>Parameter Name</th>
                            <th>Parameter Value</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div class="text-end my-3">
                    <a onclick="removeParameterInputField()" id="remove-parameter" class="btn btn-outline-danger btn-sm" style="display: none">Delete Row</a>
                    <a onclick="addParameterInputField()" id="add-parameter" class="btn btn-outline-primary btn-sm me-2">Add Row</a>
                </div>
                <div class="text-end mt-5">
                    <a class="btn btn-primary me-2" onclick="submitConfigForm()">Save</a>
                    <a onclick="closeModal()" class="btn btn-danger">Cancel</a>
                </div>
            </form>
        </div>
    </div>

    <!-- New experiment modal  -->
    <div id="exp-modal" class="myAlert-sm">
        <div class="myAlertBody">
            <h3 style="margin-bottom: 25px;">New Experiment</h3>
            <div class="align-items-center">
                <input type="text" class="form-control me-2 my-2" id="config-name-exp-modal" required
                    placeholder="Configuration Name" />

                <select id="FL_config_value" class="form-select me-2 my-2">
                    <option selected>FL Configuration</option>
                    <c:forEach items="${allConfigurations}" var="config">
                        <option id="${config.id}" value='${config.toJson()}'>${config.name}</option>
                    </c:forEach>
                </select>

                <div class="text-end my-3">
                    <a class="btn btn-primary me-2" onclick="submitExpForm()">Save</a>
                    <a onclick="closeModal()" class="btn btn-danger">Cancel</a>
                </div>
            </div>

        </div>
    </div>

    <!-- Container -->
    <div class="container" style="margin-top: 50px;">
        <ul class="nav nav-underline">
            <li class="nav-item">
                <a class="nav-link active" href="#tab1Content">FL Configurations</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#tab2Content">My FL Experiments</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" onclick="getAllExperiments()" href="#tab3Content">All FL Experiments</a>
            </li>
        </ul>

        <!-- Add the "tab-content" class to your tab content divs -->

        <!-- TAB 1 -->
        <div id="tab1Content" class="container tab-content" style="display: block;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" name="config-name" id="ExpConfigName" required
                        placeholder="Configuration name" />

                    <select class="form-select me-2" id="Algorithm">
                        <option value="" selected>Algorithm</option>
                        <option value="fcmeans">Fcmeans</option>
                    </select>

                    <select class="form-select me-2" id="ClientStrategy">
                        <option value="" selected>Client strategy</option>
                        <option value="probability">Probability</option>
                        <option value="ranking">Ranking</option>
                        <option value="threshold">Threshold</option>
                    </select>

                    <select class="form-select me-2" id="StopCondition">
                        <option value="" selected>Stop condition</option>
                        <option value="custom">Custom</option>
                        <option value="metric_under_threshold">Metric Under Threshold</option>
                        <option value="metric_over_threshold">Metric Over Threshold</option>
                    </select>

                    <input type="hidden" id="configPage" value="0">

                    <a onclick="displayConfigModal()" class="btn btn-primary">New</a>
                </div>

                <table id="ConfigTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Config Name</th>
                            <th>Algorithm</th>
                            <th>Creation Date</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${configurations.content}" var="config">
                        <tr>
                            <td class='align-middle'>${config.id}</td>
                            <td class='align-middle'>${config.name}</td>
                            <td class='align-middle'>${config.algorithm}</td>
                            <td class='align-middle'>${config.creationDate}</td>
                            <td class='align-middle'><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="displayConfigDetailsModal('${config.id}')"></td>
                            <td class='align-middle'><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig('${config.id}')" height="20px" width="20px"></figure></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- TAB 2 -->
        <div id="tab2Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="execution-name" required
                        placeholder="Experiment name" />
                    <input type="text" class="form-control me-2" id="config-name" required
                        placeholder="Configuration name" />
                    <input type="hidden" id="expPage" value="0">

                    <a onclick="displayExpModal()" class="btn btn-primary">New</a>
                </div>


                <table id="ExpTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Experiment Name</th>
                            <th>Config Name</th>
                            <th>Creation Date</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${experiments.content}" var="exp">
                        <tr>
                            <td class='align-middle'>${exp.id}</td>
                            <td class='align-middle'>${exp.name}</td>
                            <td class='align-middle'>${exp.configName}</td>
                            <td class='align-middle'>${exp.creationDate}</td>
                            <td class='align-middle'><a href="/experiment-${exp.id}"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                            <td class='align-middle'><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteExp('${exp.id}')" height="20px" width="20px"></figure></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <%--TAB 3--%>
        <div id="tab3Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="all-execution-name" required
                           placeholder="Experiment name" />
                    <input type="text" class="form-control me-2" id="all-config-name" required
                           placeholder="Configuration name" />
                    <input type="hidden" id="allExpPage" value="0">

                </div>


                <table id="all-ExpTable" class="table mt-3 text-center"
                       style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Experiment Name</th>
                            <th>Config Name</th>
                            <th>Creation Date</th>
                            <th></th>
                        </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${allExperiments.content}" var="exp">
                        <tr>
                            <td>${exp.id}</td>
                            <td>${exp.name}</td>
                            <td>${exp.expConfig.name}</td>
                            <td>${exp.creationDate}</td>
                            <td><a href="/experiment-${exp.id}"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Pagination buttons -->
        <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
            <div class="d-flex gap-2">
                <!-- Left arrow to decrease the page -->
                <button id="prevPageButton" class="btn btn-primary" onclick="handlePage('prev')">
                    &lt; Previous
                </button>
                <!-- Right arrow to increase the page -->
                <button id="nextPageButton" class="btn btn-primary" onclick="handlePage('next')">
                    Next &gt;
                </button>
            </div>
        </div>
    </div>

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
        crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js"></script>

    <script>
        let totalConfigPages = ${configurations.totalPages};
        let totalExpPages = ${experiments.totalPages};
        let totalAllExpPages = ${allExperiments.totalPages};

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
                displayErrorModal("Missing Fields", missingFields);
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
                    closeModal();
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
            console.log("Deleting config with id:", id);

            $.post('/admin/deleteConfig-' + id, function (response) {
                console.log('Server response:', response);
                $("#" + id).remove();
            }).fail(function (error) {
                console.error('Error deleting config:', error);
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
                console.log("Server response:", jsonData);

                formData["id"] = jsonData.id;
                formData["creationDate"] = jsonData.creationTime;

                console.log("New config:", formData);
                getMyExperiments();

                closeModal();
            }).fail(function (error) {
                console.error("Error:", error);
            });
        }

        function deleteExp(id) {
            console.log("Deleting experiment with id:", id);

            $.delete('/admin/deleteExp-' + id, function (response) {
                console.log('Server response:', response);
            }).fail(function (error) {
                console.error('Error deleting experiment:', error);
            });

            getMyExperiments();
        }

        // Function to handle pagination
        function handlePage(direction) {
            let currentPage, totalPages, getPageFunction;
            const activeTabText = $('.nav-link.active').text().trim();
            switch (activeTabText) {
                case "FL Configurations":
                    currentPage = $("#configPage");
                    totalPages = totalConfigPages;

                    console.log("Configurations totalPages "+ totalPages);
                    console.log("Configurations currentPage "+ currentPage.val());

                    getPageFunction = getMyConfigurations;
                    break;
                case "My FL Experiments":
                    currentPage = $("#expPage");
                    totalPages = totalExpPages;
                    getPageFunction = getMyExperiments;
                    break;
                case "All FL Experiments":
                    currentPage = $("#allExpPage");
                    totalPages = totalAllExpPages;
                    getPageFunction = getAllExperiments;
                    break;
            }

            console.log('totalPages:', totalPages);
            if (direction === 'next' && currentPage.val() < totalPages - 1) {
                currentPage.val(parseInt(currentPage.val()) + 1);
            } else if (direction === 'prev' && currentPage.val() > 0) {
                currentPage.val(parseInt(currentPage.val()) - 1);
            }

            getPageFunction(currentPage.val());
        }

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
            });
        }

        function updateExpTable(response, tableId) {
            const tbody = $('#' + tableId + ' tbody');
            tbody.empty();
            const configurations = response.content;
            $.each(configurations, function (_, item) {
                const row = $('<tr>').append(
                    '<td class="align-middle">' + item.id + '</td>' +
                    '<td class="align-middle">' + item.name + '</td>' +
                    '<td class="align-middle">' + (tableId === 'tab2Content' ? item.configName : item.expConfig.name) + '</td>' +
                    '<td class="align-middle">' + item.creationDate + '</td>' +
                    '<td class="align-middle"><a href="/experiment-' + item.id + '"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>' +
                    (tableId === 'tab2Content' ?
                        '<td class="align-middle"><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteExp(\'' + item.id + '\')" height="20px" width="20px"></figure></td>' :
                        '')
                );
                tbody.append(row);
            });
        }

        // Function to update configuration table
        function updateConfigTable(response) {
            // Clear previous search results
            const tbody = $('#ConfigTable tbody');
            tbody.empty();

            // Extract the list from the response
            const configurations = response.content;

            // Insert rows based on the response
            $.each(configurations, function (index, item) {
                const row = $('<tr>').append(
                    '<td class="align-middle">' + item.id + '</td>' +
                    '<td class="align-middle">' + item.name + '</td>' +
                    '<td class="align-middle">' + item.algorithm + '</td>' +
                    '<td class="align-middle">' + item.creationDate + '</td>' +
                    '<td class="align-middle"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="displayConfigDetailsModal(\'' + item.id + '\')"></td>' +
                    '<td class="align-middle"><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + item.id + '\')" height="20px" width="20px"></figure></td>'
                );

                tbody.append(row);
            });
        }

        $(document).ready(function () {
            // Event listener for tab clicks
            $('.nav-link').on('click', function (e) {
                e.preventDefault();

                // Remove the "active" class from all links
                $('.nav-link').removeClass('active');

                // Add the "active" class to the clicked link
                $(this).addClass('active');

                // Hide all tab contents
                $('.tab-content').hide();

                $('#configPage').val(0);
                $('#expPage').val(0);
                $('#allExpPage').val(0);

                // Show the corresponding tab content
                const targetTab = $(this).attr('href');
                $(targetTab).show();
            });

            $('#execution-name, #config-name').on('input', function () {
                getMyExperiments();
            });

            $('#ExpConfigName, #ClientStrategy, #StopCondition, #Algorithm').on('input', function () {
                getMyConfigurations();
            });

            $('#all-execution-name, #all-config-name').on('input', function () {
                getExperiments();
            });
        });

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
            modalBody.append($("<h3>").text("Configuration"));

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
    </script>
</body>
</html>
