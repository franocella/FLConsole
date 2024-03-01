<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Test web socket</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous"/>
    <link rel="stylesheet" href="CSS/main.css"/>

    <!-- WebSocket connection and dynamic data display with JavaScript -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.2/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>


    <script>
        const dataContainer = document.getElementById('your-data-container');
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
    </script>



</head>

<body style="background-color: #f8f8fe;">

<!-- Header section with navbar -->
<%@ include file="components/header.txt" %>

<!-- Container for the dynamic data display and start task button -->
<div class="container" style="margin-top: 50px;">
    <div id="data-container"></div>
    <button onclick="startTask()" class="btn btn-primary">Start Task</button>
</div>

<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</body>
</html>
