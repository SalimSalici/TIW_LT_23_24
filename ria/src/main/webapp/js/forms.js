function FormTextInput(_textInputEl, _errorBoxEl, _validationCheck) {
    this.textInputEl = _textInputEl;
    this.errorBoxEl = _errorBoxEl;
    this.validationCheck = _validationCheck;

    this.isValid = function() {
        let validation = this.validationCheck(this.textInputEl.value);
        if (validation.hasError) {
			this.displayError(validation.errorMessage);
            return false;
        } else {
            this.hideError();
            return true;
        }
    }

    this.textInputEl.addEventListener("blur", (evt) => {
        this.hideError();
    });
    
    this.displayError = function(msg) {
		this.errorBoxEl.innerText = msg;
        this.errorBoxEl.style.display = "block";
	}
	
	this.hideError = function(msg) {
		this.errorBoxEl.style.display = "none";
	}
	
	this.getValue = function() {
		return this.textInputEl.value;
	}
}