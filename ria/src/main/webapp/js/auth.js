{
    // LOGIN FORM -------------
    let loginForm = document.getElementById("loginForm");
    let loginErrorBox = document.getElementById("loginErrorBox");
    let loginFieldset = document.getElementById("loginSubmitBtn").closest("fieldset");
    loginForm.addEventListener("submit", evt => {
        evt.preventDefault();
        if (loginForm.checkValidity()) {
            let formData = new FormData(loginForm);
            loginFieldset.disabled = true;
            makeCall("POST", "login", formData, (response) => {
                if (response.readyState == XMLHttpRequest.DONE) {
                    switch (response.status) {
                        case 200:
                            console.log("Login successfull", response);
                            break;
                        case 401:
                            createPopup("Login failed", "error", 4000);
                            loginErrorBox.innerText = response.responseText;
                            loginErrorBox.style.display = "block";
                            break;
                        default:
                            console.log(response)
                            createPopup("Error " + response.status + ": " + response.responseText, "error", 5000);
                    }
                    loginFieldset.disabled = false;
                }
            }, false);
        } else {
            loginForm.reportValidity();
        }
    });

    // REGISTER FORM -------------
    let registerForm = document.getElementById("registerForm");
    let registerFieldset = document.getElementById("registerSubmitBtn").closest("fieldset");
    
    let registerFields = {
        username: new FormTextInput(
            document.getElementById("registerUsername"),
            document.getElementById("registerUsernameErrorBox"),
            value => { return { hasError: false } }
        ),
        name: new FormTextInput(
            document.getElementById("registerName"),
            document.getElementById("registerNameErrorBox"),
            value => { return { hasError: false } }
        ),
        surname: new FormTextInput(
            document.getElementById("registerSurname"),
            document.getElementById("registerSurnameErrorBox"),
            value => { return { hasError: false } }
        ),
        email: new FormTextInput(
            document.getElementById("registerEmail"),
            document.getElementById("registerEmailErrorBox"),
            value => { return { hasError: false } }
        ),
        password: new FormTextInput(
            document.getElementById("registerPassword"),
            document.getElementById("registerPasswordErrorBox"),
            value => { return { hasError: false } }
        ),
        confirmPassword: new FormTextInput(
            document.getElementById("registerConfirmPassword"),
            document.getElementById("registerConfirmPasswordErrorBox"),
            value => { return { hasError: false } }
        ),
    }

    registerForm.addEventListener("submit", evt => {
        evt.preventDefault();
        if (registerForm.checkValidity()) {
            console.log("Making post call");
            let formData = new FormData(registerForm);
            registerFieldset.disabled = true;
            makeCall("POST", "register", formData, (response) => {
                if (response.readyState == XMLHttpRequest.DONE) {
                    switch (response.status) {
                        case 200:
                            console.log("Register successfull", response);
                            break;
                        case 400:
                            let errors = JSON.parse(response.responseText);
                            createPopup("Registration failed", "error", 5000);
                            for (const [errorField, errorMessage] of Object.entries(errors))
                                registerFields[errorField].displayError(errorMessage);
                            break;
                        default:
                            console.log(response)
                            createPopup("Error " + response.status + ": " + response.responseText, "error", 5000);
                    }
                    registerFieldset.disabled = false;
                }
            }, false);
        } else {
            registerForm.reportValidity();
        }
    });
}