<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
    <div id="overlay-ov" class="overlay" style="z-index: 9998"></div>

    <div id="error-modal" class="myAlert-sm" style="z-index: 9999">
        <div class="myAlertBody" style="z-index: 9999">
            <h3 id="Err-Title"></h3>
            <p class="mt-3" id="Err-Message"></p>
            <button class="btn btn-primary" id="close-error-modal" onclick="closeErrorModal()">Close</button>
        </div>
    </div>

    <!-- New config modal  -->
    <div id="config-modal" class="myAlert">
        <div class="myAlertBody" style="padding-left:100px; padding-right:100px;">
            <h3 style="margin-bottom: 25px;">New FL configuration</h3>
            <form id="Form" action="" method="post" class="align-items-center">
                <input type="text" class="form-control me-2 my-2" name="config-name-modal" id="config-name-modal"
                    required placeholder="Configuration name" />

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
                    <option selected>Client strategy</option>
                    <option value="probability">Probability</option>
                    <option value="ranking">Ranking</option>
                    <option value="threshold">Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="ClientSelectionRatio" min="0" max="1" step="0.00001"
                       id="ClientSelectionRatio" required placeholder="Client Selection Ratio" />

                <input type="number" class="form-control me-2 my-2" name="NumberOfClients" step="1"
                    id="NumberOfClients" required placeholder="Minimum Number of clients" />

                <select id="StopConditionModal" class="form-select me-2 my-2">
                    <option selected>Stop condition</option>
                    <option value="custom">Custom</option>
                    <option value="max_number_rounds">Maximum Number of Rounds</option>
                    <option value="metric_under_threshold">Metric Under Threshold</option>
                    <option value="metric_over_threshold">Metric Over Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="0.00001" min="0"
                    max="1" id="StopThreshold" required placeholder="Stop condition threshold" />

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
                        <!-- Row 1 -->
                        <tr>
                            <td contenteditable="true">Parameter 1</td>
                            <td contenteditable="true">Value 1</td>
                        </tr>
                        <!-- Row 2 -->
                        <tr>
                            <td contenteditable="true">Parameter 2</td>
                            <td contenteditable="true">Value 2</td>
                        </tr>
                    </tbody>
                </table>
                <div class="text-end my-3">
                    <a onclick="addRow()" id="add-parameter" class="btn btn-outline-primary btn-sm me-2">Add Row</a>
                    <a onclick="deleteRow()" id="remove-parameter" class="btn btn-outline-danger btn-sm">Delete Row</a>
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
            <h3 style="margin-bottom: 25px;">New experiment</h3>
            <div class="align-items-center">
                <input type="text" class="form-control me-2 my-2" id="config-name-exp-modal" required
                    placeholder="Configuration name" />

                <select id="FL_config_value" class="form-select me-2 my-2">
                    <option selected>FL configuration</option>
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
                <a class="nav-link active" href="#tab1Content">FL configuration</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#tab2Content">FL execution</a>
            </li>
        </ul>

        <!-- Add the "tab-content" class to your tab content divs -->

        <!-- TAB 1 -->
        <div id="tab1Content" class="container tab-content" style="display: block;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" name="config-name" id="ExpConfigName" required
                        placeholder="Configuration name" />

                    <select class="form-select me-2" id="ClientStrategy">
                        <option value="" selected>Client strategy</option>
                        <option value="probability">Probability</option>
                        <option value="ranking">Ranking</option>
                        <option value="threshold">Threshold</option>
                    </select>

                    <select class="form-select me-2" id="StopCondition">
                        <option value="" selected>Stop condition</option>
                        <option value="custom">Custom</option>
                        <option value="max_number_rounds">Maximum Number of Rounds</option>
                        <option value="metric_under_threshold">Metric Under Threshold</option>
                        <option value="metric_over_threshold">Metric Over Threshold</option>
                    </select>



                    <a onclick="displayConfigModal()" class="btn btn-primary">New</a>
                </div>


                <table id="ConfigTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Config Name</th>
                            <th>Algorithm</th>
                            <th>Client Selection Strategy</th>
                            <th>Num. Clients</th>
                            <th>Stop Condition</th>
                            <th>Created At</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
            <!-- Pagination buttons -->
            <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
                <div class="d-flex gap-2">
                    <!-- Left arrow to decrease the page -->
                    <button class="btn btn-primary" onclick="prevConfigPage()">
                        &lt; Previous
                    </button>
                    <!-- Right arrow to increase the page -->
                    <button class="btn btn-primary" onclick="nextConfigPage()">
                        Next &gt;
                    </button>
                </div>
            </div>
        </div>

        <!-- TAB 2 -->
        <div id="tab2Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="execution-name" required
                        placeholder="Execution name" />
                    <input type="text" class="form-control me-2" id="config-name" required
                        placeholder="Configuration name" />

                    <a onclick="displayExpModal()" class="btn btn-primary">New</a>
                </div>


                <table id="ExpTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Execution name</th>
                            <th>Config Name</th>
                            <th>Creation Date</th>
                            <th>Open</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${experiments}" var="exp">
                        <tr>
                            <td class='align-middle'>${exp.id}</td>
                            <td class='align-middle'>${exp.name}</td>
                            <td class='align-middle'>${exp.configName}</td>
                            <td class='align-middle'>${exp.creationDate}</td>
                            <td class='align-middle'><a href="/admin/experiment-${exp.id}"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Pagination buttons -->
            <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
                <div class="d-flex gap-2">
                    <!-- Left arrow to decrease the page -->
                    <button class="btn btn-primary" onclick="prevExpPage()">
                        &lt; Previous
                    </button>
                    <!-- Right arrow to increase the page -->
                    <button class="btn btn-primary" onclick="nextExpPage()">
                        Next &gt;
                    </button>
                </div>
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
        let configurations = ${configurations};
        configurations.forEach(addNewConfigToList);

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

                // Show the corresponding tab content
                const targetTab = $(this).attr('href');
                $(targetTab).show();
            });

            $('#execution-name, #config-name').on('input', function() {
                searchExp();
            });

            $('#ExpConfigName, #ClientStrategy, #StopCondition').on('input', function() {
                searchConfig();
            });
        });

        function submitConfigForm() {
            // Get values from input fields
            const name = $("#config-name-modal").val().trim();
            const strategy = $("#ClientStrategyModal").val();
            const numClients = $("#NumberOfClients").val().trim();
            const algorithm = $("#AlgorithmModal").val();
            const stopCondition = $("#StopConditionModal").val();
            const threshold = $("#StopThreshold").val().trim();
            const maxNumRounds = $("#MaxNumRounds").val();
            const codeLanguage = $("#codeLanguage").val();
            const clientSelectionRatio = $("#ClientSelectionRatio").val();

            // Check if mandatory parameters are provided
            if (name === "" || strategy === "Client strategy" || numClients === "" || algorithm === "Algorithm" || stopCondition === "Stop condition" || threshold === "" || maxNumRounds === "" || codeLanguage === "Code Language" || clientSelectionRatio === "")  {
                // Display an error modal with the names of missing mandatory parameters
                displayErrorModal("Parameters", {
                    "Name": name === "" ? "Missing" : name,
                    "Client Strategy": strategy === "Client strategy" ? "Missing" : strategy,
                    "Number of Clients": numClients === "" ? "Missing" : numClients,
                    "Algorithm": algorithm === "Algorithm" ? "Missing" : algorithm,
                    "Stop Condition": stopCondition === "Stop condition" ? "Missing" : stopCondition,
                    "Threshold": threshold === "" ? "Missing" : threshold,
                    "maxNumRounds": maxNumRounds === "" ? "Missing" : maxNumRounds,
                    "codeLanguage": codeLanguage === "Code Language" ? "Missing" : codeLanguage,
                    "clientSelectionRatio": clientSelectionRatio === "" ? "Missing" : clientSelectionRatio
                });
            } else {
                // If all mandatory parameters are provided, proceed with creating the formData object
                const formData = {
                    "name": name,
                    "strategy": strategy,
                    "numClients": numClients,
                    "algorithm": algorithm,
                    "stopCondition": stopCondition,
                    "threshold": threshold,
                    "maxNumRounds": maxNumRounds,
                    "codeLanguage": codeLanguage,
                    "clientSelectionRatio": clientSelectionRatio
                };

                // Take the parameters from the table and add them to the formData object
                const parameters = {};  // Initialize parameters as an empty object

                $("#Form table tbody tr").each(function (index, row) {
                    const parameterName = $(row).find("td:eq(0)").text();
                    parameters[parameterName] = $(row).find("td:eq(1)").text();
                });

                // Only add parameters field to formData if there are parameters
                if (Object.keys(parameters).rowCount > 0) {
                    formData["parameters"] = parameters;
                }

                console.log("formData object:", formData);

                $.ajax({
                    type: "POST",
                    url: "/admin/newConfig",
                    contentType: "application/json",
                    data: JSON.stringify(formData),
                    success: function (response) {

                        const jsonData = JSON.parse(response);

                        formData["id"] = jsonData.id;
                        formData["creationDate"] = jsonData.creationTime;


                        console.log("New config:", formData);
                        addNewConfigToList(formData);

                        closeModal();
                    },
                    error: function (error) {
                        console.error("Error:", error);
                    }
                });
            }
        }

        function addNewConfigToList(formData) {
            const table = $("#ConfigTable");
            const id = formData.id;
            const name = formData.name;
            const algorithm = formData.algorithm;

            const newRow = '<tr>' +
                '<td class="align-middle">' + id + '</td>' +
                '<td class="align-middle">' + name + '</td>' +
                '<td class="align-middle">' + algorithm + '</td>' +
                '<td class="align-middle">' + formData.strategy + '</td>' +
                '<td class="align-middle">' + formData.numClients + '</td>' +
                '<td class="align-middle">' + formData.stopCondition + '</td>' +
                '<td class="align-middle">' + formData.creationDate + '</td>' +
                '<td class="align-middle"><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + id + '\')" height="20px" width="20px"></figure></td>' +
                '</tr>';

            table.append(newRow);

            // Add the option to the dropdown menu
            const selectElement = document.getElementById("FL_config_value");
            const option = document.createElement("option");
            option.value = JSON.stringify({ id, name, algorithm });
            option.text = name;
            selectElement.appendChild(option);
        }


        function deleteConfig(id) {
            console.log("Deleting config with id:", id);

            $.ajax({
                url: '/admin/deleteConfig-' + id,
                type: 'GET',
                success: function (response) {
                    console.log('Server response:', response);

                    $('#ConfigTable tr:contains(' + id + ')').remove();
                },
                error: function (error) {
                    console.error('Error deleting config:', error);
                }
            });
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

            $.ajax({
                type: "POST",
                url: "/admin/newExp",
                contentType: "application/json",
                data: JSON.stringify(formData),
                success: function (response) {

                    const jsonData = JSON.parse(response);
                    console.log("Server response:", jsonData);

                    formData["id"] = jsonData.id;
                    formData["creationDate"] = jsonData.creationTime;

                    console.log("New config:", formData);
                    addNewExpToList(formData);

                    closeModal();
                },
                error: function (error) {
                    console.error("Error:", error);
                }
            });
        }


        function deleteRow() {
            const table = document.getElementById("parametersTable");
            const rowCount = table.tBodies[0].rows.length;
            if (rowCount > 1) {
                table.tBodies[0].deleteRow(rowCount - 1);
            }

            const newRowCount =  rowCount - 1;
            // Hide the delete button if there is only one row
            if (newRowCount === 1) {
                const deleteButton = document.getElementById("remove-parameter");
                deleteButton.style.display = "none";
            }

            // Show the add button if there are less than 5 rows
            if (newRowCount < 5) {
                const addRowButton = document.getElementById("add-parameter");
                addRowButton.style.display = "inline-block";
            }
        }

        function displayErrorModal(title, params) {
            const overlayElement = $("#overlay-ov");
            overlayElement.css("display", "block");

            $("body").css("overflow-y", "hidden");

            const modalElement = $("#error-modal");
            modalElement.css("display", "block");

            // Set the text of the Err-Title element
            $("#Err-Title").text(title);

            // Construct the HTML content for Err-Message using the JSON parameters
            let errorMessage = "<ul>";
            Object.keys(params).forEach(function(param) {
                errorMessage += "<li>" + param + ": " + params[param] + "</li>";
            });
            errorMessage += "</ul>";

            $("#Err-Message").html(errorMessage);

            // Show the close button
            $("#close-error-modal").css("display", "block");
        }

        function closeErrorModal() {
            const overlayElement = $("#overlay-ov");
            overlayElement.css("display", "none");

            $("body").css("overflow-y", "auto");

            const modalElement = $("#error-modal");
            modalElement.css("display", "none");

            // Hide the close button
            $("#close-error-modal").css("display", "none");
        }

        function addNewExpToList(formData) {
            const table = $("#ExpTable");

            const id = formData.id;
            const executionName = formData.name;
            const configName = formData.expConfig.name;
            const creationDate = formatDateString(formData.creationDate);

            const newRow = "<tr>" +
                "<td class='align-middle'>" + id + "</td>" +
                "<td class='align-middle'>" + executionName + "</td>" +
                "<td class='align-middle'>" + configName + "</td>" +
                "<td class='align-middle'>" + creationDate + "</td>" +
                '<td class="align-middle"><a href="/experiment-' + id + '"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>' +
                "</tr>";

            table.append(newRow);
        }

        function formatDateString(dateString) {
            if (!dateString) return "";
            return moment(dateString).format('ddd MMM DD HH:mm:ss ZZ YYYY');
        }

        function displayConfigModal() {

            const overlayElement = document.getElementById("overlay");
            overlayElement.style.display = "block";

            let body = document.getElementsByTagName("body")[0];
            body.style.overflowY = "hidden";

            const modalElement = document.getElementById("config-modal");
            modalElement.style.display = "block";
        }

        function displayExpModal() {

            const overlayElement = document.getElementById("overlay");
            overlayElement.style.display = "block";

            let body = document.getElementsByTagName("body")[0];
            body.style.overflowY = "hidden";

            const modalElement = document.getElementById("exp-modal");
            modalElement.style.display = "block";
        }

        function closeModal() {
            let body = document.getElementsByTagName("body")[0];
            body.style.overflowY = "scroll";
            document.getElementById("exp-modal").style.display = "none";
            document.getElementById("config-modal").style.display = "none";
            document.getElementById("overlay").style.display = "none";

            resetModalFields("config-modal");
            resetModalFields("exp-modal");
        }

        function resetModalFields(modalId) {
            // Reset the values of the fields in the modal
            const modal = $("#" + modalId);

            if (modalId === "config-modal") {
                // Fields for config-modal
                modal.find("#config-name-modal").val("");
                modal.find("#ClientStrategyModal").val("Client strategy");
                modal.find("#NumberOfClients").val("");
                modal.find("#AlgorithmModal").val("Algorithm");
                modal.find("#StopConditionModal").val("Stop condition");
                modal.find("#StopThreshold").val("");
            } else if (modalId === "exp-modal") {
                // Fields for exp-modal
                modal.find("#config-name-exp-modal").val("");
                modal.find("#FL_config_value").val("FL configuration");
            }

            // Reset values in the parameters table
            const table = modal.find("#parametersTable")[0];
            if (table && table.tBodies[0]) {
                const rowCount = table.tBodies[0].rows.rowCount;

                // Update the first row values
                const firstRow = table.tBodies[0].rows[0];
                firstRow.cells[0].textContent = "Parameter 1";
                firstRow.cells[1].textContent = "Value 1";

                // Remove all rows except the first one
                for (let i = rowCount - 1; i >= 1; i--) {
                    table.tBodies[0].deleteRow(i);
                }

                // Add the second default row
                addRow();
            }
        }

        function addRow() {
            const table = document.getElementById("parametersTable");
            const rowCount = table.tBodies[0].rows.length;
            let newRowCount;
            if (rowCount < 5) {
                const newRow = table.tBodies[0].insertRow(rowCount);
                newRowCount = rowCount + 1;
                const cell1 = newRow.insertCell(0);
                const cell2 = newRow.insertCell(1);
                cell1.contentEditable = true;
                cell2.contentEditable = true;
                cell1.textContent = "Parameter" + newRowCount;
                cell2.textContent = "Value" + newRowCount;
            }

            // Hide the add button if there are 5 rows
            if (newRowCount === 5) {
                const addRowButton = document.getElementById("add-parameter");
                addRowButton.style.display = "none";
            }

            // Show the delete button if there are more than 1 row
            if (newRowCount > 1) {
                const deleteButton = document.getElementById("remove-parameter");
                deleteButton.style.display = "inline-block";
            }
        }

        // Function to perform search by configuration name
        function searchExp() {
            const executionName = $('#execution-name').val();
            const configName = $('#config-name').val();
            $.ajax({
                url: '/admin/getExperiments',
                method: 'GET',
                data: {
                    configName: configName,
                    executionName: executionName,
                    page: 0
                },
                success: function(response) {
                    updateExpTable(response);
                },
                error: function(xhr) {
                    console.error(xhr.responseText);
                }
            });
        }

        function updateExpTable(response) {
            $('#tab2Content tbody').empty();

            // Extract the list from the response
            const configurations = response.content;

            $.each(configurations, function(index, item) {
                const row = $('<tr>').append(
                    "<td class='align-middle'>" + item.id + "</td>" +
                    "<td class='align-middle'>" + item.name + "</td>" +
                    "<td class='align-middle'>" + item.configName + "</td>" +
                    "<td class='align-middle'>" + item.creationDate + "</td>" +
                    '<td class="align-middle"><a href="/experiment-' + item.id + '"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>'
            );
                $('#tab2Content tbody').append(row);
            });
        }


        // Function to perform configuration search
        function searchConfig() {
            // Retrieve values from input fields
            const configName = $('#ExpConfigName').val();
            const clientStrategy = $('#ClientStrategy').val();
            const stopCondition = $('#StopCondition').val();

            // Send asynchronous request
            $.ajax({
                url: '/admin/getConfigurations',
                method: 'GET',
                data: {
                    configName: configName,
                    clientStrategy: clientStrategy,
                    stopCondition: stopCondition,
                    page: 0
                },
                success: function(response) {
                    // Call function to update configuration table
                    console.log(response);
                    updateConfigTable(response);
                },
                error: function(xhr, status, error) {
                    // Handle error
                    console.error(xhr.responseText);
                }
            });
        }

        // Function to update configuration table
        function updateConfigTable(response) {
            // Clear previous search results
            $('#ConfigTable tbody').empty();

            // Extract the list from the response
            const configurations = response.content;

            // Insert rows based on the response
            $.each(configurations, function(index, item) {
                const row = '<tr>' +
                    '<td class="align-middle">' + item.id + '</td>' +
                    '<td class="align-middle">' + item.name + '</td>' +
                    '<td class="align-middle">' + item.algorithm + '</td>' +
                    '<td class="align-middle">' + item.strategy + '</td>' +
                    '<td class="align-middle">' + item.numClients + '</td>' +
                    '<td class="align-middle">' + item.stopCondition + '</td>' +
                    '<td class="align-middle">' + item.creationDate + '</td>' +
                    '<td class="align-middle">' + item.lastUpdate + '</td>' +
                    '<td class="align-middle"><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + item.id + '\')" height="20px" width="20px"></figure></td>' +
                    '</tr>';

                $('#ConfigTable tbody').append(row);
            });

        }

        // Variables for pagination of configurations
        let currentConfigPage = 0;
        let totalConfigPages = ${totalConfigPages};

        // Variables for pagination of experiments
        let currentExpPage = 0;
        let totalExpPages = ${totalExpPages};

        // Function to retrieve the next page of configurations
        function nextConfigPage() {
            if (currentConfigPage < totalConfigPages-1) {
                currentConfigPage++;
                getConfigurations();
            }
        }
        // Function to retrieve the previous page of configurations
        function prevConfigPage() {
            if (currentConfigPage > 0) {
                currentConfigPage--;
                getConfigurations();
            }
        }

        // Function to retrieve the next page of experiments
        function nextExpPage() {
            if (currentExpPage < totalExpPages-1) {
                currentExpPage++;
                getExperiments();
            }
        }

        // Function to retrieve the previous page of experiments
        function prevExpPage() {
            if (currentExpPage > 0) {
                currentExpPage--;
                getExperiments();
            }
        }

        // Function to retrieve configurations of the current page via an AJAX call
        function getConfigurations() {
            const configName = $('#ExpConfigName').val();
            const clientStrategy = $('#ClientStrategy').val();
            const stopCondition = $('#StopCondition').val();
            $.ajax({
                url: '/admin/getConfigurations',
                method: 'GET',
                data: {
                    name: configName,
                    clientStrategy: clientStrategy,
                    stopCondition: stopCondition,
                    page: currentConfigPage
                },
                success: function(response) {
                    currentConfigPage = response.number;
                    totalConfigPages = response.totalPages;
                    updateConfigTable(response);
                },
                error: function(xhr, status, error) {
                    console.error(xhr.responseText);
                }
            });
        }

        // Function to retrieve experiments of the current page via an AJAX call
        function getExperiments() {
            const executionName = $('#execution-name').val();
            const configName = $('#config-name').val();
            $.ajax({
                url: '/admin/getExperiments',
                method: 'GET',
                data: {
                    configName: configName,
                    executionName: executionName,
                    page: currentExpPage
                },
                success: function(response) {
                    currentExpPage = response.number;
                    totalExpPages = response.totalPages;
                    updateExpTable(response);
                },
                error: function(xhr, status, error) {
                    console.error(xhr.responseText);
                }
            });
        }
    </script>
</body>

</html>
