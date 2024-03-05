<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Experiment details</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="CSS/main.css" />
    <link rel="stylesheet" href="CSS/expDetails.css" />

</head>

<body style="background-color: #f8f8fe;">

<!-- Header section with navbar -->
<%@ include file="components/header.txt" %>

<div class="experiment">


    <div class="container-fluid" style="margin: 0;padding: 0">
        <h1 style="padding-left: 10px">Experiment A</h1>
        <div class="row">
            <div class="col">
                <div>
                    <canvas id="myChart"></canvas>
                </div>
            </div>
            <div class="col col-infos">
                <ul class="list-group">
                    <li class="list-group-item title">Configuration Name:</li>
                    <li class="list-group-item title">Algorithm:</li>
                    <li class="list-group-item title">Client Selection Strategy:</li>
                    <li class="list-group-item title">Number of Clients:</li>
                    <li class="list-group-item title">Stop Condition:</li>
                    <li class="list-group-item title">Created At:</li>
                    <li class="list-group-item title">Updated At:</li>
                    <li class="list-group-item title">Finished At:</li>
                </ul>
                <ul class="list-group d-flex flex-column flex-grow-1">
                    <li class="list-group-item">Configuration Name</li>
                    <li class="list-group-item">Algorithm A</li>
                    <li class="list-group-item">Strategy 2</li>
                    <li class="list-group-item">20</li>
                    <li class="list-group-item">Condition 1</li>
                    <li class="list-group-item">2022-01-01 12:00:00</li>
                    <li class="list-group-item">2022-01-01 14:30:00</li>
                    <li class="list-group-item">2022-01-01 14:30:00</li>
                </ul>
            </div>
        </div>
    </div>
    <button class="start-exp mt-2" onclick="startExperiment()"> Start Experiment</button>
</div>

<script>
    // Initial empty data
    var emptyData = {
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
    var config = {
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
    var myChart = new Chart(
        document.getElementById('myChart'),
        config
    );

    // Function to start the experiment (placeholder)
    function startExperiment() {
        // Sample data to be updated when the experiment starts
        var labels = ['January', 'February', 'March', 'April', 'May', 'June'];
        var newData = {
            labels: labels,
            datasets: [{
                label: 'Sample Dataset',
                backgroundColor: 'rgba(52, 107, 171, 100)',
                borderColor: 'rgba(52, 107, 171, 100)',
                borderWidth: 1,
                data: [65, 59, 80, 81, 56, 55],
            }]
        };

        // Update chart data with actual data
        myChart.data = newData;
        myChart.update();
    }
</script>

<!--collect all the info from the experiment itself
list of a name configuration parameters
show the title of the exp
configuration chosen for the exp
graph using chart js to show the data that are arrving by the websocket
configuration-->
</body>
</html>
