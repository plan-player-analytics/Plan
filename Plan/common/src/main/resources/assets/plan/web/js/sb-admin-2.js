const content = document.getElementById("content");
const navButtons = Array.from(document.getElementsByClassName("nav-button"));
const tabs = Array.from(document.getElementsByClassName("tab")).filter(tab => tab.id); // TABS NEED IDS
const tabCount = tabs.length;

function openTab(openIndex) {
    for (let navButton of navButtons) {
        if (navButton.classList.contains('active')) {
            navButton.classList.remove('active');
        }
    }

    const outOfRange = openIndex < 0 || tabCount < openIndex;
    const slideIndex = outOfRange ? 0 : openIndex;

    navButtons[slideIndex].classList.add('active');

    window.scrollTo(0, 0); // Scroll to top
    const tabWidthPercent = -100 / tabCount;
    const verticalScrollPercent = slideIndex * tabWidthPercent;
    content.style.transition = "0.5s";
    content.style.transform = `translate3d(${verticalScrollPercent}%,0px,0)`;
}

function openPage() {
    // substr removes tab- from the id
    const uriHash = (window.location.hash.substr(5)).split("&").filter(part => part);

    if (!uriHash.length) {
        openTab(0);
        return;
    }

    const tabId = uriHash[0];
    const openIndex = tabs.map(tab => tab.id).indexOf(tabId);
    openTab(openIndex);

    if (uriHash.length > 1) {
        const bootstrapTabId = uriHash[1];
        $('a[href="#' + bootstrapTabId + '"]').tab('show');
    }
}

// Prepare tabs for display
content.style.transform = "translate3d(0px,0px,0)";
content.style.width = (Math.max(100, tabCount * 100)) + "%";
content.style.opacity = "1";
for (let tab of tabs) {
    tab.style.width = `${100 / tabCount}%`;
}

window.addEventListener('hashchange', openPage);

//Sidebar navigation tabs
$('#accordionSidebar .nav-item a').click(event => {
    if (history.replaceState && event.currentTarget.href.split('#')[1].length > 0) {
        event.preventDefault();
        history.replaceState(undefined, undefined, '#' + event.currentTarget.href.split('#')[1]);
        openPage();
    }
});

// Persistent Bootstrap tabs
document.querySelectorAll(".nav-tabs a.nav-link").forEach(item => {
    item.addEventListener("click", event => {
        let uriHash;
        if (window.location.hash) {
            uriHash = (window.location.hash).split("&");
        } else {
            window.location.hash = document.querySelector(".sidebar a.nav-link").href.split("#")[1]
            uriHash = [window.location.hash];
        }
        const targetTab = event.currentTarget.href.split('#')[1];
        if (history.replaceState) {
            event.preventDefault();
            history.replaceState(undefined, undefined, uriHash[0] + '&' + targetTab);
            openPage();
        } else
            window.location.hash = uriHash[0] + '&' + targetTab;
    });
});

let oldWidth = null;

function reduceSidebar() {
    const newWidth = $(window).width();
    if (oldWidth && oldWidth === newWidth) {
        return;
    }

    const body = document.querySelector('body');
    const closeModal = document.querySelector('.sidebar-close-modal');
    const isSidebarHidden = body.classList.contains('sidebar-hidden');
    const isModalCloserHidden = closeModal.classList.contains('hidden');
    if ($(window).width() < 1350) {
        if (!isSidebarHidden) body.classList.add('sidebar-hidden');
        if (!isModalCloserHidden) closeModal.classList.add('hidden');

        // Close any open menu accordions when window is resized
        document.querySelectorAll('.sidebar .collapse').forEach(element => {
            let elCollapse = bootstrap.Collapse.getInstance(element);
            if (elCollapse) { elCollapse.hide(); }
        })
    } else if ($(window).width() > 1400 && isSidebarHidden) {
        body.classList.remove('sidebar-hidden');
        if (!isModalCloserHidden) closeModal.classList.add('hidden');
    }
    oldWidth = newWidth;
}

reduceSidebar();
$(window).resize(reduceSidebar);

function toggleSidebar() {
    document.querySelector('body').classList.toggle('sidebar-hidden');
    // Close any open menu accordions
    document.querySelectorAll('.sidebar .collapse').forEach(element => {
        let elCollapse = bootstrap.Collapse.getInstance(element);
        if (elCollapse) { elCollapse.hide(); }
    })

    const closeModal = document.querySelector('.sidebar-close-modal');
    if ($(window).width() < 900) {
        closeModal.classList.toggle('hidden');
    } else if (!closeModal.classList.contains('hidden')) {
        closeModal.classList.add('hidden');
    }
}

document.querySelectorAll('.sidebar-toggler,.sidebar-close-modal')
    .forEach(element => element.addEventListener('click', toggleSidebar));
