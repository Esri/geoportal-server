/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2015 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 */
/* global CQURLInfo:false */
(function(window) {
    "use strict";

    window.Granite = window.Granite || {};
    window.Granite.HTTP = window.Granite.HTTP || {};

    var contextPath = null;

    function detectContextPath() {
        // eslint-disable-next-line max-len
        var SCRIPT_URL_REGEXP = /^(?:http|https):\/\/[^/]+(\/.*)\/(?:etc\.clientlibs|etc(\/.*)*\/clientlibs|libs(\/.*)*\/clientlibs|apps(\/.*)*\/clientlibs|etc\/designs).*\.js(\?.*)?$/;
        try {
            if (window.CQURLInfo) {
                contextPath = CQURLInfo.contextPath || "";
            } else {
                var scripts = document.getElementsByTagName("script");
                for (var i = 0; i < scripts.length; i++) {
                    var result = SCRIPT_URL_REGEXP.exec(scripts[i].src);
                    if (result) {
                        contextPath = result[1];
                        return;
                    }
                }
                contextPath = "";
            }
        } catch (e) {
            // ignored
        }
    }

    window.Granite.HTTP.externalize = window.Granite.HTTP.externalize || function(url) {
        if (contextPath === null) {
            detectContextPath();
        }

        try {
            if (url.indexOf("/") === 0 && contextPath && url.indexOf(contextPath + "/") !== 0) {
                url = contextPath + url;
            }
        } catch (e) {
            // ignored
        }

        return url;
    };
})(this);

/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2015 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 */
(function(factory) {
    "use strict";

    // GRANITE-22281 Check for multiple initialization
    if (window.Granite.csrf) {
        return;
    }

    window.Granite.csrf = factory(window.Granite.HTTP);
}(function(http) {
    "use strict";

    // AdobePatentID="P5296"

    function Promise() {
        this._handler = [];
    }

    Promise.prototype = {
        then: function(resolveFn, rejectFn) {
            this._handler.push({ resolve: resolveFn, reject: rejectFn });
        },
        resolve: function() {
            this._execute("resolve", arguments);
        },
        reject: function() {
            this._execute("reject", arguments);
        },
        _execute: function(result, args) {
            if (this._handler === null) {
                throw new Error("Promise already completed.");
            }

            for (var i = 0, ln = this._handler.length; i < ln; i++) {
                this._handler[i][result].apply(window, args);
            }

            this.then = function(resolveFn, rejectFn) {
                (result === "resolve" ? resolveFn : rejectFn).apply(window, args);
            };

            this._handler = null;
        }
    };

    function verifySameOrigin(url) {
        // url could be relative or scheme relative or absolute
        // host + port
        var host = document.location.host;
        var protocol = document.location.protocol;
        var relativeOrigin = "//" + host;
        var origin = protocol + relativeOrigin;

        // Allow absolute or scheme relative URLs to same origin
        return (url === origin || url.slice(0, origin.length + 1) === origin + "/") ||
                (url === relativeOrigin || url.slice(0, relativeOrigin.length + 1) === relativeOrigin + "/") ||
                // or any other URL that isn't scheme relative or absolute i.e relative.
                !(/^(\/\/|http:|https:).*/.test(url));
    }

    var FIELD_NAME = ":cq_csrf_token";
    var HEADER_NAME = "CSRF-Token";
    var TOKEN_SERVLET = http.externalize("/libs/granite/csrf/token.json");

    var promise;
    var globalToken;

    function logFailRequest(error) {
        if (window.console) {
            // eslint-disable-next-line no-console
            console.warn("CSRF data not available;" +
                    "The data may be unavailable by design, such as during non-authenticated requests: " + error);
        }
    }

    function getToken() {
        var localPromise = new Promise();
        promise = localPromise;

        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    globalToken = data.token;
                    localPromise.resolve(globalToken);
                } catch (ex) {
                    logFailRequest(ex);
                    localPromise.reject(xhr.responseText);
                }
            }
        };
        xhr.open("GET", TOKEN_SERVLET, true);
        xhr.send();

        return localPromise;
    }

    function getTokenSync() {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", TOKEN_SERVLET, false);
        xhr.send();

        try {
            return globalToken = JSON.parse(xhr.responseText).token;
        } catch (ex) {
            logFailRequest(ex);
        }
    }

    function clearToken() {
        globalToken = undefined;
        getToken();
    }

    function addField(form) {
        var action = form.getAttribute("action");
        if (form.method.toUpperCase() === "GET" || (action && !verifySameOrigin(action))) {
            return;
        }

        if (!globalToken) {
            getTokenSync();
        }

        if (!globalToken) {
            return;
        }

        var input = form.querySelector('input[name="' + FIELD_NAME + '"]');

        if (!input) {
            input = document.createElement("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", FIELD_NAME);
            form.appendChild(input);
        }

        input.setAttribute("value", globalToken);
    }

    function handleForm(document) {
        var handler = function(ev) {
            var t = ev.target;

            if (t.nodeName === "FORM") {
                addField(t);
            }
        };

        if (document.addEventListener) {
            document.addEventListener("submit", handler, true);
        } else if (document.attachEvent) {
            document.attachEvent("submit", handler);
        }
    }

    handleForm(document);

    var open = XMLHttpRequest.prototype.open;

    XMLHttpRequest.prototype.open = function(method, url, async) {
        if (method.toLowerCase() !== "get" && verifySameOrigin(url)) {
            this._csrf = true;
            this._async = async;
        }

        return open.apply(this, arguments);
    };

    var send = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.send = function() {
        if (!this._csrf) {
            send.apply(this, arguments);
            return;
        }

        if (globalToken) {
            this.setRequestHeader(HEADER_NAME, globalToken);
            send.apply(this, arguments);
            return;
        }

        if (this._async === false) {
            getTokenSync();

            if (globalToken) {
                this.setRequestHeader(HEADER_NAME, globalToken);
            }

            send.apply(this, arguments);
            return;
        }

        var self = this;
        var args = Array.prototype.slice.call(arguments);

        promise.then(function(token) {
            self.setRequestHeader(HEADER_NAME, token);
            send.apply(self, args);
        }, function() {
            send.apply(self, args);
        });
    };

    var submit = HTMLFormElement.prototype.submit;

    HTMLFormElement.prototype.submit = function() {
        addField(this);
        return submit.apply(this, arguments);
    };

    if (window.Node) {
        var ac = Node.prototype.appendChild;

        Node.prototype.appendChild = function() {
            var result = ac.apply(this, arguments);

            if (result.nodeName === "IFRAME") {
                try {
                    if (result.contentWindow && !result._csrf) {
                        result._csrf = true;
                        handleForm(result.contentWindow.document);
                    }
                } catch (ex) {
                    if (result.src && result.src.length && verifySameOrigin(result.src)) {
                        if (window.console) {
                            // eslint-disable-next-line no-console
                            console.error("Unable to attach CSRF token to an iframe element on the same origin");
                        }
                    }

                    // Potential error: Access is Denied
                    // we can safely ignore CORS security errors here
                    // because we do not want to expose the csrf anyways to another domain
                }
            }

            return result;
        };
    }

    // refreshing csrf token periodically
    getToken();

    setInterval(function() {
        getToken();
    }, 300000);

    return {
        initialised: false,
        refreshToken: getToken,
        _clearToken: clearToken
    };
}));

