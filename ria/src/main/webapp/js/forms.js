function FormTextInput(_textInputEl, _errorBoxEl, _validationCheck) {
    this.textInputEl = _textInputEl;
    this.errorBoxEl = _errorBoxEl;
    this.validationCheck = _validationCheck;

    this.validate = function() {
        let validation = this.validationCheck(this.textInputEl.value);
        if (validation.hasError) {
            this.errorBoxEl.innerText = validation.errorMessage;
            this.errorBoxEl.style.display = "block";
        } else {
            this.errorBoxEl.style.display = "none";
        }
    }

    this.textInputEl.addEventListener("blur", (evt) => {
        if (this.textInputEl.value.length > 0)
            this.validate();
    });
    
    this.displayError = function(msg) {
		this.errorBoxEl.innerText = msg;
        this.errorBoxEl.style.display = "block";
	}
}