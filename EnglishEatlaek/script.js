
const screens = {
    home: document.getElementById("homeScreen"),
    game: document.getElementById("gameScreen"),
    confirm: document.getElementById("confirmationScreen")
};

let score = 10;
let ammo = 3;
let soundOn = true;


// CHANGE SCREEN
function showScreen(name) {

    Object.values(screens).forEach(screen => {
        screen.classList.remove("active");
    });

    screens[name].classList.add("active");
}


// UPDATE SCORE AND AMMO
function updateHud() {

    document.getElementById("scoreValue").textContent = score;

    document.getElementById("ammoValue").textContent = ammo;

}


// START GAME
document.getElementById("startButton").addEventListener("click", () => {

    score = 10;
    ammo = 3;

    updateHud();

    showScreen("game");

});


// PAUSE GAME
document.getElementById("pauseButton").addEventListener("click", () => {

    showScreen("confirm");

});


// CONTINUE BUTTON
document.getElementById("continueButton").addEventListener("click", () => {

    showScreen("confirm");

});


// YES BUTTON
document.getElementById("yesButton").addEventListener("click", () => {

    if (ammo >= 3) {
        ammo -= 3;
    }

    updateHud();

    showScreen("game");

});


// NO BUTTON
document.getElementById("noButton").addEventListener("click", () => {

    showScreen("home");

});


// SHOOT BUTTON
document.getElementById("shootButton").addEventListener("click", () => {

    if (ammo > 0) {

        ammo--;

        score++;

        updateHud();

    } else {

        showScreen("confirm");

    }

});


// MOVE BUTTON
document.getElementById("moveButton").addEventListener("click", (event) => {

    const button = event.currentTarget;

    button.textContent = "MOVING...";

    setTimeout(() => {

        button.textContent = "MOVE";

    }, 450);

});


// SOUND BUTTON
document.getElementById("soundButton").addEventListener("click", (event) => {

    soundOn = !soundOn;

    event.currentTarget.classList.toggle("muted", !soundOn);

    event.currentTarget.setAttribute(
        "aria-label",
        soundOn ? "Sound on" : "Sound off"
    );

});


// MENU CONTENT
const modal = document.getElementById("menuModal");

const title = document.getElementById("modalTitle");

const message = document.getElementById("modalText");

const content = {

    helpModal: [
        "HELP",
        "Use MOVE to move and SHOOT to use bullets."
    ],

    scoreModal: [
        "SCORE",
        () => `Your current score is ${score}.`
    ],

    powerModal: [
        "POWER",
        "Power-up items will be available here."
    ],

    settingsModal: [
        "SETTINGS",
        () => `Sound: ${soundOn ? "ON" : "OFF"}`
    ]

};


// OPEN MENU
document.querySelectorAll("[data-modal]").forEach(button => {

    button.addEventListener("click", () => {

        const item = content[button.dataset.modal];

        title.textContent = item[0];

        message.textContent =
            typeof item[1] === "function"
                ? item[1]()
                : item[1];

        modal.hidden = false;

    });

});


// CLOSE MENU
function closeModal() {

    modal.hidden = true;

}

document.getElementById("closeModal").addEventListener("click", closeModal);

modal.addEventListener("click", event => {

    if (event.target === modal) {
        closeModal();
    }

});

document.addEventListener("keydown", event => {

    if (event.key === "Escape") {
        closeModal();
    }

});
