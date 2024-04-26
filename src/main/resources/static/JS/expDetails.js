const stompClient = Stomp.over(new SockJS('http://10.2.1.102/FLConsole/ws'));
$(window).on('beforeunload', function() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
});

// Call generateCharts function once the DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
    if (jsonDataArray != null && jsonDataArray.length > 0)
        generateCharts();
    if (!(status === 'Finished')) {
        openConnection();
    }
});

function deleteExp(id) {
    $.post('/FLConsole/admin/deleteExp-' + id)
        .done(() => {
            openModal("Experiment deleted", 'error', "The experiment has been deleted");
            const redirectButton = $("#error-modal button").text("Go to dashboard");

            const redirectAfterDelay = setTimeout(() => {
                window.location.href = '/FLConsole/admin/dashboard';
            }, 3000);

            stompClient.disconnect();

            redirectButton.on("click", () => {
                window.location.href = '/FLConsole/admin/dashboard';
                clearTimeout(redirectAfterDelay);
            });
        })
        .fail(error => console.error('Error deleting experiment:', error));
}


function startTask() {
    if (!(status === 'Not Started')) {return;}
    // disable the button to prevent multiple clicks
    $("#deleteExpBtn").prop("disabled", true);
    sendStartRequest();
}

function openConnection() {
    stompClient.connect({}, () => {
        stompClient.subscribe("/experiment/" + id + "/metrics", (message) => {
            const progressUpdate = JSON.parse(message.body);

            if (progressUpdate.type != null) {
                const statusInput = $("#statusInput");
                switch (progressUpdate.type) {
                    case 'experiment_queued':
                        statusInput.val("Queued");
                        break;
                    case 'start_round':
                        if (progressUpdate.round === 1) {
                            openModal("Experiment started", 'error', "The experiment has started running");
                            statusInput.val("Running");
                            setTimeout(() => { closeModal("error") }, 3000);
                        }
                        break;
                    case 'strategy_server_metrics':
                        jsonDataArray.push(progressUpdate);
                        generateCharts();
                        break;
                    case 'END_EXPERIMENT':
                        openModal("Experiment finished", 'error', "The experiment has finished running");
                        statusInput.val("Finished");
                        $("#deleteExpBtn").prop("disabled", false);
                        stompClient.disconnect();
                        break;
                    default:
                        break;
                }
            }
        });
    }, (error) => {
        console.error("WebSocket connection error:", error);
        openModal("Error", 'error', "An error occurred while connecting to the WebSocket");
    });
}

function sendStartRequest() {
    if (conf == null) {
        openModal("Error", 'error', "Experiment configuration deleted")
        return;
    }
    // Send a request to start the experiment
    // If the request is successful, update the status and remove the button
    $.ajax({
        type: "POST",
        url: "/FLConsole/admin/start-exp",
        data: {
            config: JSON.stringify(conf),
            expId: id
        },
        success: function () {
            $('#startTaskBtn').remove();
        },
        error: function () {
            openModal("Error", 'error', "An error occurred while starting the experiment")
        }
    });
}

// Function to generate charts for modelMetrics and hostMetrics
function generateCharts() {
    const groupedData = {};
    jsonDataArray.forEach(function (data) {
        const roundNumber = data.round;
        if (!groupedData[roundNumber]) {
            groupedData[roundNumber] = [];
        }
        groupedData[roundNumber].push(data);
    });
    const metricsTypes = ["modelMetrics", "hostMetrics"];

    // Render charts for each metric by default
    Object.keys(groupedData[1][0].modelMetrics).forEach(function (metric) {
        renderChart(metric, "modelMetrics", groupedData);
    });

    Object.keys(groupedData[1][0].hostMetrics).forEach(function (metric) {
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
    Object.values(groupedData).forEach(function (roundData) {
        roundData.forEach(function (item) {
            data.push(item[metricsType][metric]);
            labels.push("Round " + item.round);
        });
    });

    // Check if chart already exists
    const existingChartCanvas = document.getElementById(metricsType + "-" + metric + "-chart");
    if (existingChartCanvas) {
        const existingChart = Chart.getChart(existingChartCanvas);
        existingChart.data.labels = labels;
        existingChart.data.datasets[0].data = data;
        existingChart.update();
        return; // Exit function after updating existing chart
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

    Object.values(groupedData).forEach(function (roundData) {
        roundData.forEach(function (data) {
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