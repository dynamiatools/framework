function initDarkMode(toggleSelector) {
    const body = document.body;
    const toggleBtn = document.querySelector(toggleSelector);

    // Función para aplicar modo según preferencia
    function applyTheme(theme) {
        if (theme === "dark") {
            body.classList.add("dark-mode");
            body.classList.remove("light-mode");
        } else {
            body.classList.add("light-mode");
            body.classList.remove("dark-mode");
        }
        localStorage.setItem("theme", theme);
    }

    // Inicializar: leer localStorage o usar default "light"
    const savedTheme = localStorage.getItem("theme") || "light";
    applyTheme(savedTheme);

    // Toggle al dar clic
    toggleBtn.addEventListener("click", () => {
        const newTheme = body.classList.contains("dark-mode") ? "light" : "dark";
        applyTheme(newTheme);
    });
}

function detectDevice() {
    const body = document.body;
    body.classList.remove("device-smartphone", "device-tablet", "device-desktop");

    const width = window.innerWidth;

    if (width <= 767) {
        body.classList.add("device-smartphone");
    } else if (width <= 1024) {
        body.classList.add("device-tablet");
    } else {
        body.classList.add("device-desktop");
    }
}


zk.afterMount(function () {
    initDarkMode(".dark-mode-switch");
    detectDevice();
    window.addEventListener("resize", detectDevice);
});
