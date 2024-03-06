<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>


<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Homepage</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="CSS/main.css" />

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
        <div class="myAlertBody">
            <h3 style="margin-bottom: 25px;">New FL configuration</h3>
            <form id="Form" action="" method="post" class="align-items-center">
                <input type="text" class="form-control me-2 my-2" name="config-name-modal" id="config-name-modal" required placeholder="Configuration name"/>
            
                <select id="ClientStrategyModal" class="form-select me-2 my-2">
                    <option selected>Client strategy</option>
                    <option value="1">One</option>
                    <option value="2">Two</option>
                    <option value="3">Three</option>
                </select>
            
                <input type="number" class="form-control me-2 my-2" name="NumberOfClients" step="1" id="NumberOfClients" required placeholder="Number of clients"/>

                <select id="AlgorithmModal" class="form-select me-2">
                    <option selected>Algorithm</option>
                    <option value="1">Algorithm one</option>
                    <option value="2">Algorithm two</option>
                    <option value="3">Algorithm three</option>
                </select>

                <select id="StopConditionModal" class="form-select me-2 my-2">
                    <option selected>Stop condition</option>
                    <option value="1">One</option>
                    <option value="2">Two</option>
                    <option value="3">Three</option>
                </select>
                
                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="0.00001" min="0" max="1" id="StopThreshold" required placeholder="Stop condition threshold"/>
    
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
                    <a onclick="addRow()" class="btn btn-outline-primary btn-sm me-2">Add Row</a>
                    <a onclick="deleteRow()" class="btn btn-outline-danger btn-sm">Delete Row</a>
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
                <input type="text" class="form-control me-2 my-2" id="config-name-exp-modal" required placeholder="Configuration name"/>
            
                <select id="FL_config_value" class="form-select me-2 my-2">
                    <option selected>FL configuration</option>
                    <option value="1">One</option>
                    <option value="2">Two</option>
                    <option value="3">Three</option>
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
                    <input type="text" class="form-control me-2" name="config-name" id="ExpConfigName" required placeholder="Configuration name"/>
                
                    <select class="form-select me-2">
                        <option selected>Client strategy</option>
                        <option value="1">One</option>
                        <option value="2">Two</option>
                        <option value="3">Three</option>
                    </select>

                    <select class="form-select me-2">
                        <option selected>Stop condition</option>
                        <option value="1">One</option>
                        <option value="2">Two</option>
                        <option value="3">Three</option>
                    </select>
                
                    <button class="btn btn-primary me-2">Search</button>
                    <a onclick="displayConfigModal()" class="btn btn-primary">New</a>
                </div>
                
                
                <table id="ConfigTable" class="table mt-3 text-center" style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Config Name</th>
                            <th>Algorithm</th>
                            <th>Client Selection Strategy</th>
                            <th>Num. Clients</th>
                            <th>Stop Condition</th>
                            <th>Created At</th>
                            <th>Updated At</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- TAB 2 -->
        <div id="tab2Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="execution-name" required placeholder="Execution name"/>
                    <input type="text" class="form-control me-2"id="config-name" required placeholder="Configuration name"/>

                    <button class="btn btn-primary me-2">Search</button>
                    <a onclick="displayExpModal()" class="btn btn-primary">New</a>
                </div>
                
                
                <table class="table mt-3 text-center" style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Execution name</th>
                            <th>Config Name</th>
                            <th>Open</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Examples -->
                        <tr>
                            <td>1</td>
                            <td>Experiment A</td>
                            <td>Configuration 1</td>
                            <td><a href="#"><img src="Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                        </tr>
                        <tr>
                            <td>2</td>
                            <td>Experiment B</td>
                            <td>Configuration 2</td>
                            <td><a href="#"><img src="Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>




    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
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
                var targetTab = $(this).attr('href');
                $(targetTab).show();
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

            // Check if mandatory parameters are provided
            if (name === "" || strategy === "Client strategy" || numClients === "" || algorithm === "Algorithm" || stopCondition === "Stop condition" || threshold === "") {
                // Display an error modal with the names of missing mandatory parameters
                displayErrorModal("Parameters", {
                    "Name": name === "" ? "Missing" : name,
                    "Client Strategy": strategy === "Client strategy" ? "Missing" : strategy,
                    "Number of Clients": numClients === "" ? "Missing" : numClients,
                    "Algorithm": algorithm === "Algorithm" ? "Missing" : algorithm,
                    "Stop Condition": stopCondition === "Stop condition" ? "Missing" : stopCondition,
                    "Threshold": threshold === "" ? "Missing" : threshold
                });
            } else {
                // If all mandatory parameters are provided, proceed with creating the formData object
                const formData = {
                    "name": name,
                    "strategy": strategy,
                    "numClients": numClients,
                    "algorithm": algorithm,
                    "stopCondition": stopCondition,
                    "threshold": threshold
                };

                // Take the parameters from the table and add them to the formData object
                const parameters = {};  // Initialize parameters as an empty object

                $("#Form table tbody tr").each(function(index, row) {
                    const parameterName = $(row).find("td:eq(0)").text();
                    const parameterValue = $(row).find("td:eq(1)").text();
                    parameters[parameterName] = parameterValue;
                });

                // Only add parameters field to formData if there are parameters
                if (Object.keys(parameters).length > 0) {
                    formData["parameters"] = parameters;
                }

                console.log("formData object:", formData);

                $.ajax({
                    type: "POST",
                    url: "/newConfig",
                    contentType: "application/json",
                    data: JSON.stringify(formData),
                    success: function(response) {

                        const jsonData = JSON.parse(response);

                        formData["id"] = jsonData.id;
                        formData["creationDate"] = jsonData.creationTime;
                        formData["lastUpdate"] = jsonData.lastUpdate;

                        console.log("New config:", formData);
                        addNewConfigToList(formData);

                        closeModal();
                    },
                    error: function(error) {
                        console.error("Error:", error);
                    }
                });
            }
        }

    function addNewConfigToList(formData) {
        const table = $("#ConfigTable");

        const newRow = $("<tr>");

        newRow.append("<td>" + formData.id + "</td>");
        newRow.append("<td>" + formData.name + "</td>");
        newRow.append("<td>" + formData.algorithm + "</td>");
        newRow.append("<td>" + formData.strategy + "</td>");
        newRow.append("<td>" + formData.numClients + "</td>");
        newRow.append("<td>" + formData.stopCondition + "</td>");
        newRow.append("<td>" + formData.creationDate + "</td>");
        newRow.append("<td>" + formData.lastUpdate + "</td>");

        newRow.append('<td class="align-middle"><figure class="m-0"><img src="Images/icon_delete.svg" alt="Delete" onclick="deleteConfig(\'' + formData.id + '\')" height="20px" width="20px"></figure></td>');

        table.append(newRow);
    }

    function deleteConfig(id) {
        console.log("Deleting config with id:", id);

        $.ajax({
            url: '/deleteConfig-' + id,
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


    function submitExpForm(){
        const formData = {
            "experimentName": $("#config-name-exp-modal").val(),
            "flConfig": $("#FL_config_value").val(),
        };
        console.log(JSON.stringify(formData));
        closeModal();
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
        }
        


        function addRow() {
            const table = document.getElementById("parametersTable");
            if (table.tBodies[0].rows.length < 5) {
                const newRow = table.tBodies[0].insertRow(table.tBodies[0].rows.length);
                const cell1 = newRow.insertCell(0);
                const cell2 = newRow.insertCell(1);
                cell1.contentEditable = true;
                cell2.contentEditable = true;
                cell1.textContent = "New Parameter";
                cell2.textContent = "New Value";
        
            }   
        }

        function deleteRow() {
            const table = document.getElementById("parametersTable");
            if (table.tBodies[0].rows.length > 1) {
                table.tBodies[0].deleteRow(table.tBodies[0].rows.length - 1);
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

    </script>
</body>

</html>
