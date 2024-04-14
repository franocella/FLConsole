function handlePage(direction) {
    let currentPage, totalPages, getPageFunction;
    const activeTabText = $('.nav-link.active');
    switch (activeTabText.text().trim()) {
        case "FL Configurations":
            currentPage = $("#configPage");
            totalPages = totalConfigPages;

            console.log("Configurations totalPages " + totalPages);
            console.log("Configurations currentPage " + currentPage.val());

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

    console.log('totalPages:', totalPages);
    if (direction === 'next' && currentPage.val() < totalPages - 1) {
        currentPage.val(parseInt(currentPage.val()) + 1);
    } else if (direction === 'prev' && currentPage.val() > 0) {
        currentPage.val(parseInt(currentPage.val()) - 1);
    }

    getPageFunction(currentPage.val());
}

function formatDateString(dateString) {
    return new Date(dateString).toLocaleString();
}