<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Admin Dashboard</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js"></script>
    <%-- Custom scripts --%>
    <script src="${pageContext.request.contextPath}/JS/main.js"></script>
    <script src="${pageContext.request.contextPath}/JS/adminDashboard.js"></script>
    <script src="${pageContext.request.contextPath}/JS/modals.js"></script>
</head>

<body style="background-color: #f8f8fe;">
    <!-- Header section with navbar -->
    <%@ include file="components/header.txt" %>

    <!-- Overlay -->
    <div id="overlay" class="overlay"></div>
    <div id="overlay-ov" class="overlay-ov"></div>

    <!-- Error modal -->
    <div id="error-modal" class="myAlert-sm" style="z-index: 9999"></div>

    <!-- Config details modal -->
    <div id="config-details-modal" class="myAlert" style="z-index: 9997"></div>

    <!-- New config modal  -->
    <div id="config-modal" class="myAlert">
        <div class="myAlertBody" style="padding-left:100px; padding-right:100px;">
            <h3 style="margin-bottom: 25px;">New FL configuration</h3>
            <form id="Form" action="" method="post" class="align-items-center">
                <input type="text" class="form-control me-2 my-2" name="config-name-modal" id="config-name-modal"
                    required placeholder="Configuration Name" />

                <select id="AlgorithmModal" class="form-select me-2">
                    <option selected>Algorithm</option>
                    <option value="fcmeans">Fcmeans</option>
                </select>

                <select id="codeLanguage" class="form-select me-2 my-2">
                    <option selected>Code Language</option>
                    <option value="java">Java</option>
                    <option value="python">Python</option>
                </select>

                <select id="ClientStrategyModal" class="form-select me-2 my-2">
                    <option selected>Client Strategy</option>
                    <option value="probability">Probability</option>
                    <option value="ranking">Ranking</option>
                    <option value="threshold">Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="ClientSelectionRatio" min="0" max="1" step="0.00001"
                       id="ClientSelectionRatio" required placeholder="Client Selection Ratio" />

                <input type="number" class="form-control me-2 my-2" name="MinNumberOfClients" step="1"
                    id="MinNumberOfClients" required placeholder="Minimum Number of Clients" />

                <select id="StopConditionModal" class="form-select me-2 my-2">
                    <option selected>Stop Condition</option>
                    <option value="custom">Custom</option>
                    <option value="metric_under_threshold">Metric Under Threshold</option>
                    <option value="metric_over_threshold">Metric Over Threshold</option>
                </select>

                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="0.00001" min="0"
                    max="1" id="StopThreshold" required placeholder="Stop Condition Threshold" />

                <input type="number" class="form-control me-2 my-2" name="StopThreshold" step="1" min="1"
                       max="30" id="MaxNumRounds" required placeholder="Maximum Number of Rounds" />

                <table id="parametersTable" class="table mt-3 text-center my-2">
                    <thead>
                        <tr>
                            <th>Parameter Name</th>
                            <th>Parameter Value</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div class="text-end my-3">
                    <a onclick="removeParameterInputField()" id="remove-parameter" class="btn btn-outline-danger btn-sm" style="display: none">Delete Row</a>
                    <a onclick="addParameterInputField()" id="add-parameter" class="btn btn-outline-primary btn-sm me-2">Add Row</a>
                </div>
                <div class="text-end mt-5">
                    <a class="btn btn-primary me-2" onclick="submitConfigForm()">Save</a>
                    <a onclick="closeModal('config')" class="btn btn-danger">Cancel</a>
                </div>
            </form>
        </div>
    </div>

    <!-- New experiment modal  -->
    <div id="exp-modal" class="myAlert-sm">
        <div class="myAlertBody">
            <h3 style="margin-bottom: 25px;">New Experiment</h3>
            <div class="align-items-center">
                <input type="text" class="form-control me-2 my-2" id="config-name-exp-modal" required
                    placeholder="Configuration Name" />

                <select id="FL_config_value" class="form-select me-2 my-2">
                    <option selected>FL Configuration</option>
                    <c:forEach items="${allConfigurations}" var="config">
                        <option id="${config.id}" value='${config.toJson()}'>${config.name}</option>
                    </c:forEach>
                </select>

                <div class="text-end my-3">
                    <a class="btn btn-primary me-2" onclick="submitExpForm()">Save</a>
                    <a onclick="closeModal('exp')" class="btn btn-danger">Cancel</a>
                </div>
            </div>

        </div>
    </div>

    <!-- Container -->
    <div class="container" style="margin-top: 50px;">
        <ul class="nav nav-underline">
            <li class="nav-item">
                <a class="nav-link active" href="#tab1Content">FL Configurations</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#tab2Content">My FL Experiments</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" onclick="getAllExperiments()" href="#tab3Content">All FL Experiments</a>
            </li>
        </ul>

        <!-- Add the "tab-content" class to your tab content divs -->

        <!-- TAB 1 -->
        <div id="tab1Content" class="container tab-content" style="display: block;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" name="config-name" id="ExpConfigName" required
                        placeholder="Configuration name" />

                    <select class="form-select me-2" id="Algorithm">
                        <option value="" selected>Algorithm</option>
                        <option value="fcmeans">Fcmeans</option>
                    </select>

                    <select class="form-select me-2" id="ClientStrategy">
                        <option value="" selected>Client strategy</option>
                        <option value="probability">Probability</option>
                        <option value="ranking">Ranking</option>
                        <option value="threshold">Threshold</option>
                    </select>

                    <select class="form-select me-2" id="StopCondition">
                        <option value="" selected>Stop condition</option>
                        <option value="custom">Custom</option>
                        <option value="metric_under_threshold">Metric Under Threshold</option>
                        <option value="metric_over_threshold">Metric Over Threshold</option>
                    </select>

                    <input type="hidden" id="configPage" value="0">

                    <a onclick="openModal('','config','')" class="btn btn-primary">New</a>
                </div>

                <table id="ConfigTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Config Name</th>
                            <th>Algorithm</th>
                            <th>Creation Date</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${configurations.content}" var="config">
                        <tr>
                            <td class='align-middle'>${config.id}</td>
                            <td class='align-middle'>${config.name}</td>
                            <td class='align-middle'>${config.algorithm}</td>
                            <td class='align-middle'>${configsDate[config.id]}</td>
                            <td class='align-middle'><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px" onclick="openModal('', 'config-details', '${config.id}')"></td>
                            <td class='align-middle'><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteConfig('${config.id}')" height="20px" width="20px"></figure></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- TAB 2 -->
        <div id="tab2Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="execution-name" required
                        placeholder="Experiment name" />
                    <input type="text" class="form-control me-2" id="config-name" required
                        placeholder="Configuration name" />
                    <input type="hidden" id="expPage" value="0">

                    <a onclick="openModal('','exp','')" class="btn btn-primary">New</a>
                </div>


                <table id="ExpTable" class="table mt-3 text-center"
                    style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Experiment Name</th>
                            <th>Config Name</th>
                            <th>Creation Date</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${experiments.content}" var="exp">
                        <tr>
                            <td class='align-middle'>${exp.id}</td>
                            <td class='align-middle'>${exp.name}</td>
                            <td class='align-middle'>${exp.configName}</td>
                            <td class='align-middle'>${experimentsDate[exp.id]}</td>
                            <td class='align-middle'><a href="/FLConsole/experiment-${exp.id}"><img src="${pageContext.request.contextPath}/Images/icon _chevron circle right alt_.svg" alt="Open" width="25px" height="25px"></a></td>
                            <td class='align-middle'><figure class="m-0"><img src="${pageContext.request.contextPath}/Images/icon_delete.svg" alt="Delete" onclick="deleteExp('${exp.id}')" height="20px" width="20px"></figure></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <%--TAB 3--%>
        <div id="tab3Content" class="container tab-content" style="display: none;">
            <div class="container py-2 my-2" style="box-shadow: 0 3px 4px rgba(0, 0, 0, 0.1);">
                <div class="d-flex align-items-center">
                    <input type="text" class="form-control me-2" id="all-execution-name" required
                           placeholder="Experiment name" />
                    <input type="text" class="form-control me-2" id="all-config-name" required
                           placeholder="Configuration name" />
                    <input type="hidden" id="allExpPage" value="0">
                </div>
                <table id="all-ExpTable" class="table mt-3 text-center"
                       style="box-shadow: 0 2px 3px rgba(0, 0, 0, 0.1);">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Experiment Name</th>
                            <th>Config Name</th>
                            <th>Creation Date</th>
                            <th></th>
                        </tr>
                    </thead>

                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Pagination buttons -->
        <div class="d-flex justify-content-between position-fixed bottom-0 end-0" style="margin-bottom: 120px; margin-right: 80px">
            <div class="d-flex gap-2">
                <!-- Left arrow to decrease the page -->
                <button id="prevPageButton" class="btn btn-primary" onclick="handlePage('prev')">
                    &lt; Previous
                </button>
                <!-- Right arrow to increase the page -->
                <button id="nextPageButton" class="btn btn-primary" onclick="handlePage('next')">
                    Next &gt;
                </button>
            </div>
        </div>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        let totalConfigPages = ${configurations.totalPages};
        let totalExpPages = 1;
        <c:if test="${experiments != null}">
            totalExpPages = ${experiments.totalPages};
        </c:if>
        let totalAllExpPages = 0;

        $(function () {
            // Event listener for tab clicks
            $('.nav-link').on('click', function (e) {
                e.preventDefault();
                $('.nav-link').removeClass('active');
                $(this).addClass('active');
                $('.tab-content').hide();
                const targetTab = $(this).attr('href');
                $(targetTab).show();
            });

            $('#execution-name, #config-name').on('input', function () {getMyExperiments();});
            $('#ExpConfigName, #ClientStrategy, #StopCondition, #Algorithm').on('input', function () {getMyConfigurations();});
            $('#all-execution-name, #all-config-name').on('input', function () {getAllExperiments();});
        });
    </script>
</body>
</html>