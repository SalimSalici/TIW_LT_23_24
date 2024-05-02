/**
 * AJAX call management
 */

function makeCall(method, url, formData, cback) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        cback(req)
    };
    req.open(method, url);
    if (formData == null) {
        req.send();
    } else {
        req.send(formData);
    }
}