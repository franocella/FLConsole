<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>


<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>User dashboard</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
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

    <!-- Container -->
    <div class="container" style="margin-top: 50px;">
        <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
            <div class="d-flex align-items-center">
                <input type="text" class="form-control me-2" id="execution-name" required placeholder="Execution name"/>
                <input type="text" class="form-control me-2" id="config-name" required placeholder="Configuration name"/>
            </div>


            <table id="table" class="table mt-3 text-center" style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                <thead>
                <tr>
                    <th>Id</th>
                    <th>Execution name</th>
                    <th>Config Name</th>
                    <th>Creation date</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <!-- Examples -->
                <c:forEach items="${experiments.content}" var="exp">
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
                <button class="btn btn-primary" onclick="">
                    &lt; Previous
                </button>
                <!-- Right arrow to increase the page -->
                <button class="btn btn-primary" onclick="">
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
        // Variables for pagination of experiments
        let currentExpPage = 0;
        let totalExpPages = 1;
        totalExpPages = ${experiments.totalPages};

        $(document).ready(function () {
            // Event listener for the search button
            $('#execution-name, #config-name').on('input', function () {
                getExperiments();
            });
        });

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

        function updateExpTable(response) {
            $('#table tbody').empty();

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
                $('#table tbody').append(row);
            });
        }

        // Function to retrieve the next page of experiments
        function nextExpPage() {
            if (currentExpPage < totalExpPages - 1) {
                currentExpPage++;
                getExperiments(currentExpPage);
            }
        }

        // Function to retrieve the previous page of experiments
        function prevExpPage() {
            if (currentExpPage > 0) {
                currentExpPage--;
                getExperiments(currentExpPage);
            }
        }

        // Function to retrieve experiments of the current page via an AJAX call
        function getExperiments(page = 0) {

            const executionName = $('#execution-name').val();
            const configName = $('#config-name').val();
            console.log(executionName, configName);
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
                    currentExpPage = response.number;
                    totalExpPages = response.totalPages;
                    updateExpTable(response);
                },
                error: function (xhr) {
                    console.error(xhr.responseText);
                }
            });
        }
    </script>
</body>

</html>
