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

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js"></script>
    <script src="${pageContext.request.contextPath}/JS/main.js"></script>
    <script src="${pageContext.request.contextPath}/JS/adminDashboard.js"></script>
    <script src="${pageContext.request.contextPath}/JS/modals.js"></script>
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
            <button class="btn btn-primary" id="close-error-modal" onclick="closeModal('error')">Close</button>
        </div>
    </div>

    <!-- Container -->
    <div id="tab3Content" class="container tab-content" style="margin-top: 50px;">
        <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
            <div class="d-flex align-items-center">
                <input type="text" class="form-control me-2" id="all-execution-name" required
                       placeholder="Experiment name" />
                <input type="text" class="form-control me-2" id="all-config-name" required
                       placeholder="Configuration name" />
                <input type="hidden" id="allExpPage" value="0">
            </div>
            <table id="all-ExpTable" class="table mt-3 text-center" style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
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
                        <td>${experimentsDate[exp.id]}</td>
                        <td><a href="/FLConsole/experiment-${exp.id}"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- Pagination buttons -->
        <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
            <div class="d-flex gap-2">
                <!-- Left arrow to decrease the page -->
                <button id="prevPageButton" class="btn btn-primary" onclick="handleUserPage('prev')">
                    &lt; Previous
                </button>
                <!-- Right arrow to increase the page -->
                <button id="nextPageButton" class="btn btn-primary" onclick="handleUserPage('next')">
                    Next &gt;
                </button>
            </div>
        </div>
    </div>

    <script>
        // Variables for pagination of experiments
        let totalAllExpPages = ${experiments.totalPages};
        const contextPath = '${pageContext.request.contextPath}';

        // Function to handle the page change
        function handleUserPage(direction) {
            let currentPage = $('#allExpPage');
            if (direction === 'next' && currentPage.val() < totalAllExpPages - 1) {
                currentPage.val(parseInt(currentPage.val()) + 1);
                getAllExperiments(currentPage.val());


            } else if (direction === 'prev' && currentPage.val() > 0) {
                currentPage.val(parseInt(currentPage.val()) - 1);
                getAllExperiments(currentPage.val());
            }
        }

        $(function () {
            $('#all-execution-name, #all-config-name').on('input', function () {getAllExperiments();});
        });
    </script>
</body>

</html>
