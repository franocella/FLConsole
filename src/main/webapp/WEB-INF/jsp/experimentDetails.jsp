<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Experiment details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
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
        <h1 class="text-center mb-5" >Experiment A</h1>
        <div class="row align-items-center">
            <div class="col" style="height: 460px">
                <div>
                    <canvas id="myChart"></canvas>
                </div>
            </div>
            <div class="col" style="height: 460px; padding-top: 40px; padding-left: 20px; padding-right: 20px">

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Configuration Name:</span>
                        <input type="text" aria-label="Configuration Name" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Algorithm:</span>
                        <input type="text" aria-label="Algorithm" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Client Selection Strategy:</span>
                        <input type="text" aria-label="Client Selection Strategy" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Number of Clients:</span>
                        <input type="text" aria-label="Number of Clients" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Stop Condition:</span>
                        <input type="text" aria-label="Stop Condition" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Created At:</span>
                        <input type="text" aria-label="Created At" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Updated At:</span>
                        <input type="text" aria-label="Updated At" class="form-control">
                    </div>

                    <div class="input-group">
                        <span class="input-group-text" style="font-weight: bold; font-size: large; width: 240px;">Finished At:</span>
                        <input type="text" aria-label="Finished At" class="form-control">
                    </div>

                    <button class="btn btn-primary mt-4 float-end" onclick="startExperiment()"> Start Experiment</button>
                </div>

            </div>



</div>
</div>

<script>
    const socketUrl = 'http://localhost:8080/ws';
    const socket = new SockJS(socketUrl);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log("Connected to WebSocket");

        stompClient.subscribe("/experiment/progress", (message) => {
            const progressUpdate = JSON.parse(message.body);
            updateData(progressUpdate.RandomValue);
        });
    }, (error) => {
        console.error("WebSocket connection error:", error);
    });

    function updateData(data) {
        const dataContainer = document.getElementById('data-container');

        const listItem = document.createElement('li');
        listItem.textContent = 'Random Value: ' + data;

        dataContainer.appendChild(listItem);
    }

    function startTask() {
        fetch('/start-task', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        });
    }

    // Initial empty data
    const emptyData = {
        labels: [],
        datasets: [{
            label: 'Sample Dataset',
            backgroundColor: 'rgba(52, 107, 171, 100)',
            borderColor: 'rgba(52, 107, 171, 100)',
            borderWidth: 1,
            data: [],
        }]
    };

    // Chart.js configuration with empty data
    const config = {
        type: 'line',
        data: emptyData,
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        },
    };

    // Create the chart with empty data
    const myChart = new Chart(
        document.getElementById('myChart'),
        config
    );

    // Function to start the experiment (placeholder)
    function startExperiment() {
        // Sample data to be updated when the experiment starts
        const labels = ['January', 'February', 'March', 'April', 'May', 'June'];
        // Update chart data with actual data
        myChart.data = {
            labels: labels,
            datasets: [{
                label: 'Sample Dataset',
                backgroundColor: 'rgba(52, 107, 171, 100)',
                borderColor: 'rgba(52, 107, 171, 100)',
                borderWidth: 1,
                data: [65, 59, 80, 81, 56, 55],
            }]
        };
        myChart.update();
    }
</script>

<!-- Your existing script tags for jQuery and Bootstrap -->
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</body>

</html>
