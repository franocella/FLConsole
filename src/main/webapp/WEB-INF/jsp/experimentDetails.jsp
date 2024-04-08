<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="false" %>

<jsp:useBean id="experiment" scope="request" type="it.unipi.mdwt.flconsole.model.Experiment" />
<jsp:useBean id="isAuthor" scope="request" type="java.lang.Boolean"/>
<jsp:useBean id="expConfig" scope="request" type="it.unipi.mdwt.flconsole.model.ExpConfig" />

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

            <div class="experiment">
                <div class="container-fluid" style="margin: 0;padding: 0">
                    <h1 class="text-center mb-5">${experiment.name}</h1>

                    <div id="ExpInfoTableDiv" class="container">
                            <div class="input-group">
                                <span class="input-group-text"
                                    style="font-weight: bold; font-size: large; width: 240px;">Configuration
                                    Name:</span>
                                <input type="text" disabled aria-label="Configuration Name" class="form-control"
                                    value="${expConfig.name}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">Algorithm:</span>
                                <input type="text" disabled aria-label="Algorithm" class="form-control"
                                       value="${expConfig.algorithm}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">Strategy:</span>
                                <input type="text" disabled aria-label="Strategy" class="form-control"
                                       value="${expConfig.clientSelectionStrategy}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">Number of Clients:</span>
                                <input type="text" disabled aria-label="Number of Clients" class="form-control"
                                       value="${expConfig.minNumberClients}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">Stop Condition:</span>
                                <input type="text" disabled aria-label="Stop Condition" class="form-control"
                                       value="${expConfig.stopCondition}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                      style="font-weight: bold; font-size: large; width: 240px;">Threshold:</span>
                                <input type="text" disabled aria-label="Threshold" class="form-control"
                                       value="${expConfig.stopConditionThreshold}">
                            </div>

                            <c:if test="${not empty expConfig.parameters}">
                                <c:set var="map" value="${expConfig.parameters}" />
                                <c:forEach items="${map}" var="entry">
                                    <div class="input-group">
                                    <span class="input-group-text"
                                          style="font-weight: bold; font-size: large; width: 240px;">${entry.key}:</span>
                                        <input type="text" disabled aria-label="${entry.key}" class="form-control"
                                               value="${entry.value}">
                                    </div>
                                </c:forEach>
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
                                <input type="text" disabled aria-label="Finished At" class="form-control"
                                    value="${experiment.status}">
                            </div>
                            <c:if test="${experiment.status.toString() == 'NOT_STARTED'}">
                                <c:if test="${isAuthor}">
                                    <button id="startTaskBtn" class="btn btn-primary mt-4 float-end" onclick="startTask()">Start
                                        Experiment</button>
                                </c:if>
                            </c:if>
                        </div></div>

                    <h1 class="text-center my-5">Metrics</h1>
                    <div id="MetricsTableDiv" class="container">
                    <table class="table text-center">
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
                    <div id="MetricsTab" class="container">
                    <!-- Tabs for modelMetrics and hostMetrics -->
                        <ul class="nav nav-tabs" id="metricsTabs" role="tablist">
                            <li class="nav-item" role="presentation">
                                <button class="nav-link active" id="modelMetrics-tab" data-bs-toggle="tab" data-bs-target="#modelMetrics" type="button" role="tab" aria-controls="modelMetrics" aria-selected="true">Model Metrics</button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link" id="hostMetrics-tab" data-bs-toggle="tab" data-bs-target="#hostMetrics" type="button" role="tab" aria-controls="hostMetrics" aria-selected="false">Host Metrics</button>
                            </li>
                        </ul>
                    </div>


                <div class="tab-content container" id="metricsTabContent">
                    <div class="tab-pane fade show active" id="modelMetrics" role="tabpanel" aria-labelledby="modelMetrics-tab">
                        <!-- Buttons for each modelMetric -->
                        <div id="modelMetricsButtons">
                            <!-- Buttons will be added dynamically here -->
                        </div>
                        <!-- Chart container for modelMetrics -->
                        <div id="modelMetricsCharts" class="chart-container">
                            <!-- Charts will be added dynamically here -->
                        </div>
                    </div>
                    <div class="tab-pane fade" id="hostMetrics" role="tabpanel" aria-labelledby="hostMetrics-tab">
                        <!-- Buttons for each hostMetric -->
                        <div id="hostMetricsButtons">
                            <!-- Buttons will be added dynamically here -->
                        </div>
                        <!-- Chart container for hostMetrics -->
                        <div id="hostMetricsCharts" class="chart-container">
                            <!-- Charts will be added dynamically here -->
                        </div>
                    </div>
                </div>

            </div>






        <script>
                let jsonDataArray = null;
                <c:if test="${metrics != null}">
                    jsonDataArray = ${metrics};
                    console.log(jsonDataArray);
                </c:if>

                // Call generateCharts function once the DOM is loaded
                document.addEventListener("DOMContentLoaded", () => {
                    if (jsonDataArray !=null && jsonDataArray.length > 0)
                        generateCharts();
                });

                let status = "${experiment.status.toString()}";
                const id = "${experiment.id}";
                const conf = ${expConfig.toJson()};

                if (status === 'RUNNING') {
                    const socketUrl = 'http://localhost:8080/ws';
                    const socket = new SockJS(socketUrl);
                    const stompClient = Stomp.over(socket);

                    stompClient.connect({}, () => {
                        console.log("Connected to WebSocket");

                        stompClient.subscribe("/experiment/metrics", (message) => {
                            const progressUpdate = JSON.parse(message.body);
                            updateData(progressUpdate);
                        });
                    }, (error) => {
                        console.error("WebSocket connection error:", error);
                    });
                }

                function updateData(data) {
                    const labels = Object.keys(data);
                    const dataValues = Object.values(data);
                    updateChart(labels, dataValues);
                }


                function startTask() {

                    if (status === 'RUNNING') {
                        return;
                    }

                    const socketUrl = 'http://localhost:8080/ws';
                    const socket = new SockJS(socketUrl);
                    const stompClient = Stomp.over(socket);

                    stompClient.connect({}, () => {
                        console.log("Connected to WebSocket");

                        stompClient.subscribe("/experiment/" + id + "/metrics", (message) => {
                            const progressUpdate = JSON.parse(message.body);
                            updateData(progressUpdate);
                            if (message.type === 'END_EXPERIMENT') {
                                stompClient.disconnect();
                                displayErrorModal("Experiment finished", "The experiment has finished running");
                            }
                        });
                    }, (error) => {
                        console.error("WebSocket connection error:", error);
                    });

                    console.log("Starting experiment with configuration:", conf);
                    // Send a request to start the experiment
                    // If the request is successful, update the status and remove the button
                    $.ajax({
                        type: "POST",
                        url: "/admin/start-exp",
                        data: {
                            config: JSON.stringify(conf),
                            expId: id
                        },
                        success: function() {
                            status = 'RUNNING';
                            $('#startTaskBtn').remove();
                            openErrorModal("Success", "Experiment started successfully");
                        },
                        error: function(error) {
                            openErrorModal("Error", error.responseText);
                        }
                    });
                }

                // Function to generate charts for modelMetrics and hostMetrics
                function generateCharts() {
                    const groupedData = {};
                    jsonDataArray.forEach(function(data) {
                        const roundNumber = data.round;
                        if (!groupedData[roundNumber]) {
                            groupedData[roundNumber] = [];
                        }
                        groupedData[roundNumber].push(data);
                    });

                    const metricsTypes = ["modelMetrics", "hostMetrics"];

                    // Render charts for each metric by default
                    Object.keys(groupedData[1][0].modelMetrics).forEach(function(metric) {
                        renderChart(metric, "modelMetrics", groupedData);
                    });

                    Object.keys(groupedData[1][0].hostMetrics).forEach(function(metric) {
                        renderChart(metric, "hostMetrics", groupedData);
                    });

                    // Update the table with new data
                    updateTable(groupedData);
                }

                // Render chart for the selected metric and metricsType
                function renderChart(metric, metricsType, groupedData) {
                    const metricsChartsContainer = metricsType === "modelMetrics" ? document.getElementById("modelMetricsCharts") : document.getElementById("hostMetricsCharts");
                    const data = [];
                    const labels = [];
                    Object.values(groupedData).forEach(function(roundData) {
                        roundData.forEach(function(item) {
                            data.push(item[metricsType][metric]);
                            labels.push("Round " + item.round);
                        });
                    });

                    // Remove existing chart if any
                    const existingChart = document.getElementById(metricsType + "-" + metric + "-chart");
                    if (existingChart) {
                        existingChart.remove();
                    }

                    // Create canvas for the chart
                    const canvas = document.createElement("canvas");
                    canvas.id = metricsType + "-" + metric + "-chart";
                    metricsChartsContainer.appendChild(canvas);

                    // Configure the chart
                    new Chart(canvas, {
                        type: "line",
                        data: {
                            labels: labels,
                            datasets: [{
                                label: metric,
                                data: data,
                                borderColor: getRandomColor(),
                                fill: false
                            }]
                        },
                        options: {
                            scales: {
                                y: {
                                    beginAtZero: true
                                }
                            }
                        }
                    });
                }

                // Function to add new data received as a message
                function addNewData(newData) {
                    jsonDataArray.push(JSON.stringify(newData));
                    generateCharts();
                }

                // Function to generate random colors
                function getRandomColor() {
                    const letters = "0123456789ABCDEF";
                    let color = "#";
                    for (let i = 0; i < 6; i++) {
                        color += letters[Math.floor(Math.random() * 16)];
                    }
                    return color;
                }

                // Function to update the table with new data
                function updateTable(groupedData) {
                    const jsonDataBody = document.getElementById("jsonDataBody");
                    jsonDataBody.innerHTML = ""; // Clear existing data

                    Object.values(groupedData).forEach(function(roundData) {
                        roundData.forEach(function(data) {
                            const row = document.createElement("tr");
                            row.innerHTML = "<td>" + data.round + "</td><td>" + createList(data.hostMetrics) + "</td><td>" + createList(data.modelMetrics) + "</td>";
                            jsonDataBody.appendChild(row);
                        });
                    });
                }

                // Function to create an HTML list from a JSON object
                function createList(obj) {
                    const list = document.createElement("ul");
                    list.classList.add("custom-list-style");
                    for (const key in obj) {
                        if (Object.hasOwnProperty.call(obj, key)) {
                            const listItem = document.createElement("li");
                            listItem.innerHTML = "<b>" + key + "</b>: " + obj[key];
                            list.appendChild(listItem);
                        }
                    }
                    return list.outerHTML;
                }

            </script>
        <!-- Your existing script tags for jQuery and Bootstrap -->
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                crossorigin="anonymous"></script>
    </body>
</html>