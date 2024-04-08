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
    const existingChart = metricsChartsContainer.querySelector("#" + metricsType + "-" + metric + "-chart");
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
    list.classList.add("custom-list-style"); // Add class to remove bullet point
    for (const key in obj) {
        if (Object.hasOwnProperty.call(obj, key)) {
            const listItem = document.createElement("li");
            listItem.innerHTML = "<b>" + key + "</b>: " + obj[key];
            list.appendChild(listItem);
        }
    }
    return list.outerHTML;
}
