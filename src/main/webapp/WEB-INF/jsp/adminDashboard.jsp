<%@ page import="it.unipi.mdwt.flconsole.utils.Constants" %>
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

                <input type="number" class="form-control me-2 my-2" name="MinNumberOfClients" step="1"
                    id="MinNumberOfClients" required placeholder="Minimum Number of clients" />

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
                    </tbody>
                </table>
                <div class="text-end my-3">
                    <a onclick="removeParameterInputField()" id="remove-parameter" class="btn btn-outline-danger btn-sm">Delete Row</a>
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
            <h3 style="margin-bottom: 25px;">New experiment</h3>
            <div class="align-items-center">
                <input type="text" class="form-control me-2 my-2" id="config-name-exp-modal" required
                    placeholder="Configuration name" />

                <select id="FL_config_value" class="form-select me-2 my-2">
                    <option selected>FL configuration</option>
                    <c:forEach items="${allConfigurations}" var="config">
                        <option value='${config.toJson()}'>${config.name}</option>
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
                <a class="nav-link" onclick="getExperiments()" href="#tab3Content">All FL Experiments</a>
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
                        <option value="kmeans">Kmeans</option>
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
                            <td class='align-middle'><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="openConfigDetails('${config.id}')"></td>
                            <td class='align-middle'><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig('${config.id}')" height="20px" width="20px"></figure></td>
                        </tr>
                    </c:forEach>
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
                    <c:forEach items="${experiments}" var="exp">
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

            <!-- Pagination buttons -->
            <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
                <div class="d-flex gap-2">
                    <!-- Left arrow to decrease the page -->
                    <button class="btn btn-primary" onclick="prevAllExpPage()">
                        &lt; Previous
                    </button>
                    <!-- Right arrow to increase the page -->
                    <button class="btn btn-primary" onclick="nextAllExpPage()">
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
        // Variables for pagination of experiments
        let totalConfigPages;
        <c:if test="${not empty configurations.totalPages}">
            totalConfigPages = ${configurations.totalPages};
        </c:if>

        // Variables for pagination of experiments
        let totalExpPages;
        <c:if test="${not empty totalExpPages}">
            totalExpPages = ${totalExpPages};
        </c:if>

        // Variables for pagination of experiments
        let totalAllExpPages;
        <c:if test="${not empty allExperiments}">
            totalAllExpPages = ${allExperiments.getTotalPages()};
        </c:if>

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

            $('#execution-name, #config-name').on('input', function () {
                getMyExperiments();
            });

            $('#ExpConfigName, #ClientStrategy, #StopCondition, #Algorithm').on('input', function () {
                getConfigurations();
            });

            $('#all-execution-name, #all-config-name').on('input', function () {
                getExperiments();
            });
        });

        function submitConfigForm() {
            // Get values from input fields
            const name = $("#config-name-modal").val().trim();
            const strategy = $("#ClientStrategyModal").val();
            const numClients = $("#MinNumberOfClients").val();
            const algorithm = $("#AlgorithmModal").val();
            const stopCondition = $("#StopConditionModal").val();
            const threshold = $("#StopThreshold").val().trim();
            const maxNumRounds = $("#MaxNumRounds").val();
            const codeLanguage = $("#codeLanguage").val();
            const clientSelectionRatio = $("#ClientSelectionRatio").val();

            // Check if mandatory parameters are provided
            if (name === "" || strategy === "Client strategy" || numClients === "" || algorithm === "Algorithm" || stopCondition === "Stop condition" || threshold === "" || maxNumRounds === "" || codeLanguage === "Code Language" || clientSelectionRatio === "") {
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
                    "minNumberOfClients": numClients,
                    "algorithm": algorithm,
                    "stopCondition": stopCondition,
                    "threshold": threshold,
                    "maxNumRounds": maxNumRounds,
                    "codeLanguage": codeLanguage,
                    "clientSelectionRatio": Number(clientSelectionRatio)
                };

                // Take the parameters from the table and add them to the formData object
                const parameters = {};  // Initialize parameters as an empty object

                $("#Form table tbody tr").each(function (index, row) {
                    const parameterName = $(row).find("td:eq(0)").text();
                    parameters[parameterName] = $(row).find("td:eq(1)").text();
                });

                console.log("Parameters:", parameters);
                // Only add parameters field to formData if there are parameters
                if (parameters !== {}) {
                    formData["parameters"] = parameters;
                }

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

                        getConfigurations();
                        addNewConfigToDropDown(formData);

                        closeModal();
                    },
                    error: function (error) {
                        console.error("Error:", error);
                    }
                });
            }
        }

        function addNewConfigToDropDown(formData) {
            const id = formData.id;
            const name = formData.name;
            const algorithm = formData.algorithm;

            // Add the option to the dropdown menu
            const selectElement = document.getElementById("FL_config_value");
            const option = document.createElement("option");
            option.value = JSON.stringify({id, name, algorithm});
            option.text = name;
            selectElement.appendChild(option);
        }

        function openConfigDetails(id) {
        }

        function deleteConfig(id) {
            console.log("Deleting config with id:", id);

            $.ajax({
                url: '/admin/deleteConfig-' + id,
                type: 'GET',
                success: function (response) {
                    console.log('Server response:', response);

                },
                error: function (error) {
                    console.error('Error deleting config:', error);
                }
            });

            getConfigurations();
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
                    getMyExperiments();

                    closeModal();
                },
                error: function (error) {
                    console.error("Error:", error);
                }
            });
        }

        function removeParameterInputField() {
            const table = document.getElementById("parametersTable");
            const rowCount = table.tBodies[0].rows.length;
            if (rowCount > 0) {
                table.tBodies[0].deleteRow(rowCount - 1);
            }

            const newRowCount = rowCount - 1;
            // Hide the delete button if there is only one row
            if (newRowCount === 0) {
                const deleteButton = document.getElementById("remove-parameter");
                deleteButton.style.display = "none";
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
            Object.keys(params).forEach(function (param) {
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
                addParameterInputField();
            }
        }

        function addParameterInputField() {
            const table = document.getElementById("parametersTable");
            const rowCount = table.tBodies[0].rows.length;
            let newRowCount;
            const newRow = table.tBodies[0].insertRow(rowCount);
            newRowCount = rowCount + 1;
            const cell1 = newRow.insertCell(0);
            const cell2 = newRow.insertCell(1);
            cell1.contentEditable = true;
            cell2.contentEditable = true;
            cell1.textContent = "Parameter" + newRowCount;
            cell2.textContent = "Value" + newRowCount;

            // Show the delete button if there is at least 1 row
            if (newRowCount > 0) {
                const deleteButton = document.getElementById("remove-parameter");
                deleteButton.style.display = "inline-block";
            }
        }

        function updateExpTable(response) {
            $('#tab2Content tbody').empty();

            // Extract the list from the response
            const configurations = response.content;

            $.each(configurations, function (index, item) {
                const row = $('<tr>').append(
                    "<td class='align-middle'>" + item.id + "</td>" +
                    "<td class='align-middle'>" + item.name + "</td>" +
                    "<td class='align-middle'>" + item.configName + "</td>" +
                    "<td class='align-middle'>" + item.creationDate + "</td>" +
                    "<td class='align-middle'><a href='/experiment-" + item.id + "'><img src='${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg' alt='Open' width='25px' height='25px'></a></td>" +
                    "<td class='align-middle'><figure class='m-0'><img src='${pageContext.request.contextPath}/Images/icon_delete.svg' alt='Delete' onclick='deleteExp(\"" + item.id + "\")' height='20px' width='20px'></figure></td>"

            );
                $('#tab2Content tbody').append(row);
            });
        }

        // Function to update configuration table
        function updateConfigTable(response) {
            // Clear previous search results
            $('#ConfigTable tbody').empty();

            // Extract the list from the response
            const configurations = response.content;

            // Insert rows based on the response
            $.each(configurations, function (index, item) {
                const row = '<tr>' +
                    '<td class="align-middle">' + item.id + '</td>' +
                    '<td class="align-middle">' + item.name + '</td>' +
                    '<td class="align-middle">' + item.algorithm + '</td>' +
                    '<td class="align-middle">' + item.creationDate + '</td>' +
                    '<td class="align-middle"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="openConfigDetails(\'' + item.id + '\')"></td>' +
                    '<td class="align-middle"><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + item.id + '\')" height="20px" width="20px"></figure></td>' +
                    '</tr>';

                $('#ConfigTable tbody').append(row);
            });
        }

        function deleteExp(id) {
            console.log("Deleting experiment with id:", id);

            $.ajax({
                url: '/admin/deleteExp-' + id,
                type: 'GET',
                success: function (response) {
                    console.log('Server response:', response);

                },
                error: function (error) {
                    console.error('Error deleting experiment:', error);
                }
            });

            getMyExperiments();
        }

        // Function to retrieve the next page of configurations
        function nextConfigPage() {
            let currentConfigPage = $('#configPage');
            if (currentConfigPage.val() < totalConfigPages - 1) {
                currentConfigPage.val(parseInt(currentConfigPage.val()) + 1);
                console.log("Current config page:", currentConfigPage.val());
                getConfigurations(currentConfigPage.val());
            }
        }

        // Function to retrieve the previous page of configurations
        function prevConfigPage() {
            let currentConfigPage = $('#configPage');
            if (currentConfigPage.val() > 0) {
                currentConfigPage.val(parseInt(currentConfigPage.val()) - 1);
                console.log("Current config page:", currentConfigPage.val());
                getConfigurations(currentConfigPage.val());
            }
        }

        // Function to retrieve the next page of experiments
        function nextExpPage() {
            let currentExpPage = $('#expPage');
            if (currentExpPage.val() < totalExpPages - 1) {
                currentExpPage.val(parseInt(currentExpPage.val()) + 1);
                console.log("Current exp page:", currentExpPage.val());
                getMyExperiments(currentExpPage.val());
            }
        }

        // Function to retrieve the previous page of experiments
        function prevExpPage() {
            let currentExpPage = $('#expPage');
            if (currentExpPage.val() > 0) {
                currentExpPage.val(parseInt(currentExpPage.val()) - 1);
                console.log("Current exp page:", currentExpPage.val());
                getMyExperiments(currentExpPage.val());
            }
        }

        // Function to retrieve configurations of the current page via an AJAX call
        function getConfigurations(page = 0) {
            const configName = $('#ExpConfigName').val();
            const clientStrategy = $('#ClientStrategy').val();
            const stopCondition = $('#StopCondition').val();
            const algorithm = $('#Algorithm').val();
            if (page === 0) {
                $('#configPage').val(0);
            }

            $.ajax({
                url: '/admin/getConfigurations',
                method: 'GET',
                data: {
                    name: configName,
                    clientStrategy: clientStrategy,
                    stopCondition: stopCondition,
                    algorithm: algorithm,
                    page: page
                },
                success: function (response) {
                    $('#configPage').val(response.number);
                    totalConfigPages = response.totalPages;
                    updateConfigTable(response);
                },
                error: function (xhr, status, error) {
                    console.error(xhr.responseText);
                }
            });
        }

        // Function to retrieve experiments of the current page via an AJAX call
        function getMyExperiments(page = 0) {
            const executionName = $('#execution-name').val();
            const configName = $('#config-name').val();
            if (page === 0) {
                $('#expPage').val(0);
            }

            $.ajax({
                url: '/admin/getExperiments',
                method: 'GET',
                data: {
                    configName: configName,
                    executionName: executionName,
                    page: page
                },
                success: function (response) {
                    $('#expPage').val(response.number);
                    totalExpPages = response.totalPages;
                    updateExpTable(response);
                },
                error: function (xhr, status, error) {
                    console.error(xhr.responseText);
                }
            });
        }

        function updateAllExpTable(response) {
            $('#all-ExpTable tbody').empty();

            // Extract the list from the response
            const configurations = response.content;

            $.each(configurations, function (index, item) {
                const row = $('<tr>').append(
                    "<td class='align-middle'>" + item.id + "</td>" +
                    "<td class='align-middle'>" + item.name + "</td>" +
                    "<td class='align-middle'>" + item.expConfig.name + "</td>" +
                    "<td class='align-middle'>" + item.creationDate + "</td>" +
                    '<td class="align-middle"><a href="/experiment-' + item.id + '"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>'
                );
                $('#all-ExpTable tbody').append(row);
            });
        }

        // Function to retrieve the next page of experiments
        function nextAllExpPage() {
            let currentAllExpPage = $('#allExpPage');
            if (currentAllExpPage.val() < totalAllExpPages - 1) {
                currentAllExpPage.val(parseInt(currentAllExpPage.val()) + 1);
                getExperiments(currentAllExpPage.val());
            }
        }

        // Function to retrieve the previous page of experiments
        function prevAllExpPage() {
            let currentAllExpPage = $('#allExpPage');
            if (currentAllExpPage.val() > 0) {
                currentAllExpPage.val(parseInt(currentAllExpPage.val()) - 1);
                getExperiments(currentAllExpPage.val());
            }
        }

        // Function to retrieve experiments of the current page via an AJAX call
        function getExperiments(page = 0) {
            const executionName = $('#all-execution-name').val();
            const configName = $('#all-config-name').val();
            if (page === 0) {
                $('#allExpPage').val(0);
            }
            $.ajax({
                url: '/getExperiments',
                method: 'POST',
                data: {
                    configName: configName,
                    expName: executionName,
                    page: page
                },
                success: function (response) {
                    console.log(response);
                    $('#allExpPage').val(response.number);
                    totalAllExpPages = response.totalPages;
                    updateAllExpTable(response);
                },
                error: function (xhr) {
                    console.error(xhr.responseText);
                }
            });
        }
    </script>
</body>

</html>
