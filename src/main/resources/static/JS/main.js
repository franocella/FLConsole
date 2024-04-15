// TODO: use in the switch the id of the tab to determine the active tab
function handlePage(direction) {
    let currentPage, totalPages, getPageFunction;
    const activeTabText = $('.nav-link.active');
    switch (activeTabText.text().trim()) {
        case "FL Configurations":
            currentPage = $("#configPage");
            totalPages = totalConfigPages;
            getPageFunction = getMyConfigurations;
            break;
        case "My FL Experiments":
            currentPage = $("#expPage");
            totalPages = totalExpPages;
            getPageFunction = getMyExperiments;
            break;
        case "All FL Experiments":
            currentPage = $("#allExpPage");
            totalPages = totalAllExpPages;
            getPageFunction = getAllExperiments;
            break;
    }

    if (direction === 'next' && currentPage.val() < totalPages - 1) {
        currentPage.val(parseInt(currentPage.val()) + 1);
    } else if (direction === 'prev' && currentPage.val() > 0) {
        currentPage.val(parseInt(currentPage.val()) - 1);
    }

    getPageFunction(currentPage.val());
}

// Function to retrieve all experiments of the current page
function getAllExperiments(page = 0) {
    const executionName = $('#all-execution-name').val();
    const configName = $('#all-config-name').val();

    getData('/FLConsole/getExperiments', {
        configName: configName,
        expName: executionName,
        page: page
    }, $('#allExpPage'), updateExpTable, 'tab3Content');
}

function formatDateString(dateString) {
    return new Date(dateString).toLocaleString();
}