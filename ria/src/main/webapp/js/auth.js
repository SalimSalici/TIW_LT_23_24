{
    // LOGIN FORM -------------
    let loginForm = document.getElementById("loginForm");
    let loginErrorBox = document.getElementById("loginErrorBox");
    let loginFieldset = document.getElementById("loginSubmitBtn").closest("fieldset");
    let rememberMeEl = document.getElementById("rememberMe");
    loginForm.addEventListener("submit", evt => {
        evt.preventDefault();
        if (loginForm.checkValidity()) {
            let formData = new FormData(loginForm);
            loginFieldset.disabled = true;
            makeCall("POST", "login", formData, (response) => {
                if (response.readyState == 4) {
                    switch (response.status) {
                        case 200:
                            let userJsonString = response.responseText;
                            sessionStorage.setItem("user", userJsonString);
                            if (rememberMeEl.checked === true)
								localStorage.setItem("rememberMe", formData.get("loginUsername"));
							else
								localStorage.removeItem("rememberMe");
                            window.location.href = "home.html";
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
    
    window.addEventListener("load", _ => {
		const username = localStorage.getItem("rememberMe");
		if (username) {
			document.getElementById("loginUsername").value = username;
			rememberMeEl.checked = true;
		}
	})

    // REGISTER FORM -------------
    let registerForm = document.getElementById("registerForm");
    let registerFieldset = document.getElementById("registerSubmitBtn").closest("fieldset");
    
    let registerFields = {
        username: new FormTextInput(
            document.getElementById("registerUsername"),
            document.getElementById("registerUsernameErrorBox"),
            _ => { return { hasError: false } }
        ),
        name: new FormTextInput(
            document.getElementById("registerName"),
            document.getElementById("registerNameErrorBox"),
            _ => { return { hasError: false } }
        ),
        surname: new FormTextInput(
            document.getElementById("registerSurname"),
            document.getElementById("registerSurnameErrorBox"),
            _ => { return { hasError: false } }
        ),
        email: new FormTextInput(
            document.getElementById("registerEmail"),
            document.getElementById("registerEmailErrorBox"),
            value => {
				const regex = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
				let result = {
					hasError: false,
					errorMessage: "Invalid email format. Please insert a valid email."
				}
				if (!value.toLowerCase().match(regex))
					result.hasError = true;
				return result;
			 }
        ),
        password: new FormTextInput(
            document.getElementById("registerPassword"),
            document.getElementById("registerPasswordErrorBox"),
            _ => { return { hasError: false } }
        ),
        confirmPassword: new FormTextInput(
            document.getElementById("registerConfirmPassword"),
            document.getElementById("registerConfirmPasswordErrorBox"),
            value => { 
				let result = {
					hasError: false,
					errorMessage: "The passwords do not match."
				}
				if (value != document.getElementById("registerPassword").value)
					result.hasError = true;
				return result;
			 }
        ),
    }

    registerForm.addEventListener("submit", evt => {
        evt.preventDefault();
        if (registerForm.checkValidity() && customCheckFormValidity(registerFields)) {
            let formData = new FormData(registerForm);
            registerFieldset.disabled = true;
            makeCall("POST", "register", formData, (req) => {
                if (req.readyState == XMLHttpRequest.DONE) {
                    switch (req.status) {
                        case 200:
                            let userJsonString = req.responseText;
                            sessionStorage.setItem("user", userJsonString);
                            window.location.href = "home.html";
                            break;
                        case 400:
                            let errors = JSON.parse(req.responseText);
                            createPopup("Registration failed.", "error", 5000);
                            for (const [errorField, errorMessage] of Object.entries(errors))
                                registerFields[errorField].displayError(errorMessage);
                            break;
                        default:
                            console.log(req)
                            createPopup("Error " + req.status + ": " + req.responseText, "error", 5000);
                    }
                    registerFieldset.disabled = false;
                }
            }, false);
        } else {
            registerForm.reportValidity();
            createPopup("Registration failed.", "error", 5000);
        }
    });
    
    function customCheckFormValidity(formTextInputs) {
		let isValid = true;
		for (const textInput in formTextInputs) {
			if (!formTextInputs[textInput].isValid()) isValid = false;
		}
		return isValid;
	}
}