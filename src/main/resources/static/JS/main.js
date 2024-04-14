function displayMessageModal(title, message) {
    // Check if overlay already exists
    let overlay = $('#overlay');

    if (!overlay.length) {
        // If overlay does not exist, create HTML element
        overlay = $('<div id="overlay" class="overlay"></div>');

        // Add overlay to the page
        $('body').append(overlay);
    }

    // Check if modal already exists
    let modal = $('#message-modal');

    if (!modal.length) {
        // If modal does not exist, create HTML elements
        modal = $('<div id="message-modal" class="myAlert-sm">' +
            '<div class="myAlertBody">' +
            '<h3 id="Msg-Title"></h3>' +
            '<p class="mt-3" id="Msg-Content"></p>' +
            '<button class="btn btn-primary">Close</button>' +
            '</div>' +
            '</div>');

        // Add modal to the page
        $('body').append(modal);
    }

    // Set titles and messages dynamically
    $('#Msg-Title').text(title);
    $('#Msg-Content').text(message);

    // Show overlay and modal
    $('#overlay, #message-modal').css('display', 'block');

    // Bind close modal function to close button
    $('#message-modal button').on('click', closeMessageModal);
}

function closeMessageModal() {
    // Remove modal, hide overlay
    $('#message-modal').remove();
    $('#overlay').css('display', 'none');
}

function openModal(title, type, params) {
    const overlay = $("#overlay");
    const body = $("body");

    overlay.show();
    body.css("overflow-y", "hidden");

    switch (type) {
        case 'error':
            displayErrorModal(title, params);
            break;
        case 'config':
            displayConfigModal();
            break;
        case 'message':
            displayMessageModal(title, params);
            break;
        case 'exp':
            displayExpModal();
            break;
        case 'configDetails':
            displayConfigDetailsModal(params);
            break;
        default:
            console.error('Unknown modal type:', type);
    }
}

function closeModal(type) {
    switch (type) {
        case 'error':
            $('#error-modal').hide();
            break;
        case 'config':
            $('#config-modal').hide();
            break;
        case 'exp':
            $('#exp-modal').hide();
            break;
        case 'configDetails':
            $('#config-details-modal').hide();
            break;
        case 'message':
            $('#message-modal').hide();
            break;
        default:
            console.error('Unknown modal type:', type);
    }

    $('#overlay').hide();
    $('#overlay-ov').hide();
    $('body').css('overflow-y', 'auto');
}

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