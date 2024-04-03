<jsp:useBean id="experiment" scope="request" type="it.unipi.mdwt.flconsole.model.Experiment" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="false" %>

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
                    <div class="row align-items-center">
                        <div class="col" style="height: 460px">
                            <div>
                                <canvas id="myChart"></canvas>
                            </div>
                        </div>
                        <div class="col"
                            style="height: 460px; padding-top: 40px; padding-left: 20px; padding-right: 20px">

                            <div class="input-group">
                                <span class="input-group-text"
                                    style="font-weight: bold; font-size: large; width: 240px;">Configuration
                                    Name:</span>
                                <input type="text" disabled aria-label="Configuration Name" class="form-control"
                                    value="${experiment.expConfig.name}">
                            </div>

                            <div class="input-group">
                                <span class="input-group-text"
                                    style="font-weight: bold; font-size: large; width: 240px;">Algorithm:</span>
                                <input type="text" disabled aria-label="Algorithm" class="form-control"
                                    value="${experiment.expConfig.algorithm}">
                            </div>


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

                            <c:if test="${experiment.status == 'pending'}">
                                <button id="startTaskBtn" class="btn btn-primary mt-4 float-end" onclick="startTask()">Start
                                    Experiment</button>
                            </c:if>
                        </div>

                    </div>


                    <div id="data-container">

                    </div>


                </div>
            </div>

            <script>
                let status = "${experiment.status}";

                <c:if test="${experiment.status == 'running'}">
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
                </c:if>

                function updateData(data) {
                    const labels = Object.keys(data);
                    const dataValues = Object.values(data);
                    updateChart(labels, dataValues);
                }


                function startTask() {

                    if (status === 'running') {
                        return;
                    }

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

                    // Send a request to start the experiment
                    // If the request is successful, update the status and remove the button
                    $.ajax({
                        type: "GET",
                        url: "/admin/start-exp",
                        success: function() {
                            status = 'running';
                            $('#startTaskBtn').remove();
                            openErrorModal("Success", "Experiment started successfully");
                        },
                        error: function(error) {
                            openErrorModal("Error", error.responseText);
                        }
                    });
                }

                // Initial empty data
                const emptyData = {
                    labels: [],
                    datasets: [],
                };

                // Chart.js configuration with empty data
                const config = {
                    type: 'bar',
                    data: emptyData,
                    options: {
                        scales: {
                            y: {
                                beginAtZero: true,
                            },
                        },
                    },
                };

                // Create the chart with empty data
                const myChart = new Chart(
                    document.getElementById('myChart'),
                    config
                );

                function updateChart(labels, dataValues) {
                    // Create a new dataset for each parameter
                    const newDataset = {
                        label: "Progress:" + (myChart.data.datasets.length + 1),
                        backgroundColor: 'rgba(52, 107, 171, 100)',
                        borderColor: 'rgba(52, 107, 171, 100)',
                        borderWidth: 1,
                        data: dataValues,
                    };

                    // Update chart data with the new dataset
                    myChart.data.labels = labels;
                    myChart.data.datasets.push(newDataset);

                    myChart.update();
                }
            </script>

        <!-- Your existing script tags for jQuery and Bootstrap -->
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
                crossorigin="anonymous"></script>
    </body>
</html>