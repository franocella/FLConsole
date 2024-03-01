<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>


<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Login</title>
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
                    <input type="text" class="form-control me-2" name="config-name" id="config-name" required placeholder="Configuration name"/>
                
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
                
                
                <table class="table mt-3 text-center" style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
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
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Examples -->
                        <tr>
                            <td>1</td>
                            <td>Configuration 1</td>
                            <td>Algorithm A</td>
                            <td>Strategy 1</td>
                            <td>10</td>
                            <td>Condition 1</td>
                            <td>2022-01-01 12:00:00</td>
                            <td>2022-01-01 14:30:00</td>
                        </tr>
                        <tr>
                            <td>2</td>
                            <td>Configuration 2</td>
                            <td>Algorithm B</td>
                            <td>Strategy 2</td>
                            <td>20</td>
                            <td>Condition 2</td>
                            <td>2022-01-02 10:30:00</td>
                            <td>2022-01-02 16:45:00</td>
                        </tr>
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
            var formData = {
                "configName": $("#config-name-modal").val(),
                "clientStrategy": $("#ClientStrategyModal").val(),
                "numberOfClients": $("#NumberOfClients").val(),
                "stopCondition": $("#StopConditionModal").val(),
                "stopThreshold": $("#StopThreshold").val(),
                "parameters": []
            };

            $("#Form table tbody tr").each(function(index, row) {
                var parameterName = $(row).find("td:eq(0)").text();
                var parameterValue = $(row).find("td:eq(1)").text();
                formData.parameters.push({"name": parameterName, "value": parameterValue});
            });

            console.log(JSON.stringify(formData));
            closeModal();
        }

        function submitExpForm(){
            var formData = {
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
            var table = document.getElementById("parametersTable");
            if (table.tBodies[0].rows.length < 5) { 
                var newRow = table.tBodies[0].insertRow(table.tBodies[0].rows.length);
                var cell1 = newRow.insertCell(0);
                var cell2 = newRow.insertCell(1);
                cell1.contentEditable = true;
                cell2.contentEditable = true;
                cell1.textContent = "New Parameter";
                cell2.textContent = "New Value";
        
            }   
        }

        function deleteRow() {
            var table = document.getElementById("parametersTable");
            if (table.tBodies[0].rows.length > 1) {
                table.tBodies[0].deleteRow(table.tBodies[0].rows.length - 1);
            }
        }
        

    </script>
</body>

</html>
