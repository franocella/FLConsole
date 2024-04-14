<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="false" %>

<jsp:useBean id="experiment" scope="request" type="it.unipi.mdwt.flconsole.model.Experiment" />
<jsp:useBean id="isAuthor" scope="request" type="java.lang.Boolean"/>
<jsp:useBean id="expConfig" scope="request" type="java.util.Optional" />

<!DOCTYPE html>
<html lang="en">

    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Experiment details</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
            crossorigin="anonymous" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/expDetails.css" />

        <!-- Chart.js library -->
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <!-- WebSocket connection and dynamic data display with JavaScript -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.2/sockjs.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    </head>

    <body style="background-color: #f8f8fe;">


        <!-- Header section with navbar -->
        <%@ include file="components/header.txt" %>

        <!-- Overlay -->
        <div id="overlay-ov" class="overlay-ov"></div>

        <!-- Error modal -->
        <div id="error-modal" class="myAlert-sm" style="z-index: 9999"></div>

        <div class="experiment">
            <div class="container-fluid d-flex justify-content-center">
                <div id="ExpInfoTableDiv" class="container">
                    <h1 class="text-center mb-5">${experiment.name}</h1>
                    <c:choose>
                        <c:when test="${experiment.expConfig.deleted}">
                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Configuration
                                Name:</span>
                                <input type="text" disabled aria-label="Configuration Name" class="form-control"
                                       value="${experiment.expConfig.name} (DELETED)">
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Configuration
                                Name:</span>
                                <input type="text" disabled aria-label="Configuration Name" class="form-control"
                                       value="${experiment.expConfig.name}">
                            </div>
                        </c:otherwise>
                    </c:choose>

                        <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Algorithm:</span>
                            <input type="text" disabled aria-label="Algorithm" class="form-control"
                                   value="${experiment.expConfig.algorithm}">
                        </div>

                        <c:if test="${expConfig.present}">

                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Strategy:</span>
                                <input type="text" disabled aria-label="Strategy" class="form-control"
                                       value="${expConfig.get().clientSelectionStrategy}">
                            </div>

                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Number of Clients:</span>
                                <input type="text" disabled aria-label="Number of Clients" class="form-control"
                                       value="${expConfig.get().minNumberClients}">
                            </div>

                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Stop Condition:</span>
                                <input type="text" disabled aria-label="Stop Condition" class="form-control"
                                       value="${expConfig.get().stopCondition}">
                            </div>

                            <div class="input-group">
                            <span class="input-group-text"
                                  style="font-weight: bold; font-size: large; width: 240px;">Threshold:</span>
                                <input type="text" disabled aria-label="Threshold" class="form-control"
                                       value="${expConfig.get().stopConditionThreshold}">
                            </div>

                            <c:if test="${not empty expConfig.get().parameters}">
                                <c:set var="map" value="${expConfig.get().parameters}" />
                                <c:forEach items="${map}" var="entry">
                                    <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">${entry.key}:</span>
                                        <input type="text" disabled aria-label="${entry.key}" class="form-control"
                                               value="${entry.value}">
                                    </div>
                                </c:forEach>
                            </c:if>
                        </c:if>

                        <div class="input-group">
                            <span class="input-group-text"
                                style="font-weight: bold; font-size: large; width: 240px;">Created
                                At:</span>
                            <input type="text" disabled aria-label="Created At" class="form-control"
                                value="${experiment.creationDate}">
                        </div>

                        <div class="input-group">
                            <span class="input-group-text"
                                style="font-weight: bold; font-size: large; width: 240px;">Status:</span>
                            <input type="text" id="statusInput" disabled aria-label="Finished At" class="form-control"
                                value="${experiment.status}">
                        </div>
                        <c:if test="${experiment.status.toString() == 'NOT_STARTED'}">
                            <c:if test="${isAuthor}">
                                <c:if test="${expConfig.present}">
                                    <button id="startTaskBtn" class="btn btn-primary mt-4 float-end" onclick="startTask()">Start
                                        Experiment</button>
                                </c:if>
                            </c:if>
                        </c:if>
                </div>
            </div>
        </div>
        <h1 class="text-center my-5">Metrics</h1>

        <div class="container-fluid d-flex justify-content-center">

            <div id="MetricsTab" class="container">
                <ul class="nav nav-tabs justify-content-center" id="metricsTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="modelMetrics-tab" data-bs-toggle="tab" data-bs-target="#modelMetrics" type="button" role="tab" aria-controls="modelMetrics" aria-selected="true">Model Metrics</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="hostMetrics-tab" data-bs-toggle="tab" data-bs-target="#hostMetrics" type="button" role="tab" aria-controls="hostMetrics" aria-selected="false">Host Metrics</button>
                    </li>
                </ul>

            </div>
        </div>

        <div class="container-fluid d-flex justify-content-center">
            <div class="tab-content container" id="metricsTabContent">
                <div class="tab-pane fade show active" id="modelMetrics" role="tabpanel" aria-labelledby="modelMetrics-tab">

                    <!-- Chart container for modelMetrics -->
                    <div id="modelMetricsCharts" class="chart-container">
                        <!-- Charts will be added dynamically here -->
                    </div>
                </div>
                <div class="tab-pane fade" id="hostMetrics" role="tabpanel" aria-labelledby="hostMetrics-tab">
                    <!-- Chart container for hostMetrics -->
                    <div id="hostMetricsCharts" class="chart-container">
                        <!-- Charts will be added dynamically here -->
                    </div>
                </div>
            </div>
        </div>

        <div id="MetricsTableDiv" class="container-fluid d-flex justify-content-center mb-5">
            <table class="table container text-center mt-5" style="margin-bottom: 150px">
                <thead>
                <tr>
                    <th>Round</th>
                    <th>Host Metrics</th>
                    <th>Model Metrics</th>
                </tr>
                </thead>
                <tbody id="jsonDataBody">
                <!-- Data will be dynamically added here -->
                </tbody>
            </table>
        </div>
        <script src="${pageContext.request.contextPath}/JS/main.js"></script>
        <script src="${pageContext.request.contextPath}/JS/expDetails.js"></script>

        <script>
            let jsonDataArray = null;
            let conf = null;
            let status = "${experiment.status.toString()}";
            const id = "${experiment.id}";

            <c:if test="${metrics != null}">
                jsonDataArray = ${metrics};
            </c:if>
            <c:if test="${expConfig.present}">
                conf = ${expConfig.get().toJson()};
            </c:if>
        </script>

        <!-- Your existing script tags for jQuery and Bootstrap -->
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                crossorigin="anonymous">
        </script>
        <script src="${pageContext.request.contextPath}/JS/modals.js"></script>

    </body>
</html>