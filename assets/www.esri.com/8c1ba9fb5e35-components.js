/*******************************************************************************
 * Copyright 2018 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 * Element.matches()
 * https://developer.mozilla.org/enUS/docs/Web/API/Element/matches#Polyfill
 */
if (!Element.prototype.matches) {
    Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
}

// eslint-disable-next-line valid-jsdoc
/**
 * Element.closest()
 * https://developer.mozilla.org/enUS/docs/Web/API/Element/closest#Polyfill
 */
if (!Element.prototype.closest) {
    Element.prototype.closest = function(s) {
        "use strict";
        var el = this;
        if (!document.documentElement.contains(el)) {
            return null;
        }
        do {
            if (el.matches(s)) {
                return el;
            }
            el = el.parentElement || el.parentNode;
        } while (el !== null && el.nodeType === 1);
        return null;
    };
}

/*******************************************************************************
 * Copyright 2018 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/* global
    CQ
 */
(function() {
    "use strict";

    var containerUtils = window.CQ && window.CQ.CoreComponents && window.CQ.CoreComponents.container && window.CQ.CoreComponents.container.utils ? window.CQ.CoreComponents.container.utils : undefined;
    if (!containerUtils) {
        // eslint-disable-next-line no-console
        console.warn("Tabs: container utilities at window.CQ.CoreComponents.container.utils are not available. This can lead to missing features. Ensure the core.wcm.components.commons.site.container client library is included on the page.");
    }
    var dataLayerEnabled;
    var dataLayer;
    var dataLayerName;

    var NS = "cmp";
    var IS = "tabs";

    var keyCodes = {
        END: 35,
        HOME: 36,
        ARROW_LEFT: 37,
        ARROW_UP: 38,
        ARROW_RIGHT: 39,
        ARROW_DOWN: 40
    };

    var selectors = {
        self: "[data-" + NS + '-is="' + IS + '"]',
        active: {
            tab: "cmp-tabs__tab--active",
            tabpanel: "cmp-tabs__tabpanel--active"
        }
    };

    /**
     * Tabs Configuration
     *
     * @typedef {Object} TabsConfig Represents a Tabs configuration
     * @property {HTMLElement} element The HTMLElement representing the Tabs
     * @property {Object} options The Tabs options
     */

    /**
     * Tabs
     *
     * @class Tabs
     * @classdesc An interactive Tabs component for navigating a list of tabs
     * @param {TabsConfig} config The Tabs configuration
     */
    function Tabs(config) {
        var that = this;

        if (config && config.element) {
            init(config);
        }

        /**
         * Initializes the Tabs
         *
         * @private
         * @param {TabsConfig} config The Tabs configuration
         */
        function init(config) {
            that._config = config;

            // prevents multiple initialization
            config.element.removeAttribute("data-" + NS + "-is");

            cacheElements(config.element);
            that._active = getActiveIndex(that._elements["tab"]);

            if (that._elements.tabpanel) {
                refreshActive();
                bindEvents();
                scrollToDeepLinkIdInTabs();
            }

            if (window.Granite && window.Granite.author && window.Granite.author.MessageChannel) {
                /*
                 * Editor message handling:
                 * - subscribe to "cmp.panelcontainer" message requests sent by the editor frame
                 * - check that the message data panel container type is correct and that the id (path) matches this specific Tabs component
                 * - if so, route the "navigate" operation to enact a navigation of the Tabs based on index data
                 */
                CQ.CoreComponents.MESSAGE_CHANNEL = CQ.CoreComponents.MESSAGE_CHANNEL || new window.Granite.author.MessageChannel("cqauthor", window);
                CQ.CoreComponents.MESSAGE_CHANNEL.subscribeRequestMessage("cmp.panelcontainer", function(message) {
                    if (message.data && message.data.type === "cmp-tabs" && message.data.id === that._elements.self.dataset["cmpPanelcontainerId"]) {
                        if (message.data.operation === "navigate") {
                            navigate(message.data.index);
                        }
                    }
                });
            }
        }

        /**
         * Displays the panel containing the element that corresponds to the deep link in the URI fragment
         * and scrolls the browser to this element.
         */
        function scrollToDeepLinkIdInTabs() {
            if (containerUtils) {
                var deepLinkItemIdx = containerUtils.getDeepLinkItemIdx(that, "tab", "tabpanel");
                if (deepLinkItemIdx > -1) {
                    var deepLinkItem = that._elements["tab"][deepLinkItemIdx];
                    if (deepLinkItem && that._elements["tab"][that._active].id !== deepLinkItem.id) {
                        navigateAndFocusTab(deepLinkItemIdx, true);
                    }
                    var hashId = window.location.hash.substring(1);
                    if (hashId) {
                        var hashItem = document.querySelector("[id='" + hashId + "']");
                        if (hashItem) {
                            hashItem.scrollIntoView();
                        }
                    }
                }
            }
        }

        /**
         * Returns the index of the active tab, if no tab is active returns 0
         *
         * @param {Array} tabs Tab elements
         * @returns {Number} Index of the active tab, 0 if none is active
         */
        function getActiveIndex(tabs) {
            if (tabs) {
                for (var i = 0; i < tabs.length; i++) {
                    if (tabs[i].classList.contains(selectors.active.tab)) {
                        return i;
                    }
                }
            }
            return 0;
        }

        /**
         * Caches the Tabs elements as defined via the {@code data-tabs-hook="ELEMENT_NAME"} markup API
         *
         * @private
         * @param {HTMLElement} wrapper The Tabs wrapper element
         */
        function cacheElements(wrapper) {
            that._elements = {};
            that._elements.self = wrapper;
            var hooks = that._elements.self.querySelectorAll("[data-" + NS + "-hook-" + IS + "]");

            for (var i = 0; i < hooks.length; i++) {
                var hook = hooks[i];
                if (hook.closest("." + NS + "-" + IS) === that._elements.self) { // only process own tab elements
                    var capitalized = IS;
                    capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
                    var key = hook.dataset[NS + "Hook" + capitalized];
                    if (that._elements[key]) {
                        if (!Array.isArray(that._elements[key])) {
                            var tmp = that._elements[key];
                            that._elements[key] = [tmp];
                        }
                        that._elements[key].push(hook);
                    } else {
                        that._elements[key] = hook;
                    }
                }
            }
        }

        /**
         * Binds Tabs event handling
         *
         * @private
         */
        function bindEvents() {
            window.addEventListener("hashchange", scrollToDeepLinkIdInTabs, false);
            var tabs = that._elements["tab"];
            if (tabs) {
                for (var i = 0; i < tabs.length; i++) {
                    (function(index) {
                        tabs[i].addEventListener("click", function(event) {
                            navigateAndFocusTab(index);
                        });
                        tabs[i].addEventListener("keydown", function(event) {
                            onKeyDown(event);
                        });
                    })(i);
                }
            }
        }

        /**
         * Handles tab keydown events
         *
         * @private
         * @param {Object} event The keydown event
         */
        function onKeyDown(event) {
            var index = that._active;
            var lastIndex = that._elements["tab"].length - 1;

            switch (event.keyCode) {
                case keyCodes.ARROW_LEFT:
                case keyCodes.ARROW_UP:
                    event.preventDefault();
                    if (index > 0) {
                        navigateAndFocusTab(index - 1);
                    }
                    break;
                case keyCodes.ARROW_RIGHT:
                case keyCodes.ARROW_DOWN:
                    event.preventDefault();
                    if (index < lastIndex) {
                        navigateAndFocusTab(index + 1);
                    }
                    break;
                case keyCodes.HOME:
                    event.preventDefault();
                    navigateAndFocusTab(0);
                    break;
                case keyCodes.END:
                    event.preventDefault();
                    navigateAndFocusTab(lastIndex);
                    break;
                default:
                    return;
            }
        }

        /**
         * Refreshes the tab markup based on the current {@code Tabs#_active} index
         *
         * @private
         */
        function refreshActive() {
            var tabpanels = that._elements["tabpanel"];
            var tabs = that._elements["tab"];

            if (tabpanels) {
                if (Array.isArray(tabpanels)) {
                    for (var i = 0; i < tabpanels.length; i++) {
                        if (i === parseInt(that._active)) {
                            tabpanels[i].classList.add(selectors.active.tabpanel);
                            tabpanels[i].removeAttribute("aria-hidden");
                            tabs[i].classList.add(selectors.active.tab);
                            tabs[i].setAttribute("aria-selected", true);
                            tabs[i].setAttribute("tabindex", "0");
                        } else {
                            tabpanels[i].classList.remove(selectors.active.tabpanel);
                            tabpanels[i].setAttribute("aria-hidden", true);
                            tabs[i].classList.remove(selectors.active.tab);
                            tabs[i].setAttribute("aria-selected", false);
                            tabs[i].setAttribute("tabindex", "-1");
                        }
                    }
                } else {
                    // only one tab
                    tabpanels.classList.add(selectors.active.tabpanel);
                    tabs.classList.add(selectors.active.tab);
                }
            }
        }

        /**
         * Focuses the element and prevents scrolling the element into view
         *
         * @param {HTMLElement} element Element to focus
         */
        function focusWithoutScroll(element) {
            var x = window.scrollX || window.pageXOffset;
            var y = window.scrollY || window.pageYOffset;
            element.focus();
            window.scrollTo(x, y);
        }

        /**
         * Navigates to the tab at the provided index
         *
         * @private
         * @param {Number} index The index of the tab to navigate to
         */
        function navigate(index) {
            that._active = index;
            refreshActive();
        }

        /**
         * Navigates to the item at the provided index and ensures the active tab gains focus
         *
         * @private
         * @param {Number} index The index of the item to navigate to
         * @param {Boolean} keepHash true to keep the hash in the URL, false to update it
         */
        function navigateAndFocusTab(index, keepHash) {
            var exActive = that._active;
            if (!keepHash && containerUtils) {
                containerUtils.updateUrlHash(that, "tab", index);
            }
            navigate(index);
            focusWithoutScroll(that._elements["tab"][index]);

            if (dataLayerEnabled) {

                var activeItem = getDataLayerId(that._elements.tabpanel[index]);
                var exActiveItem = getDataLayerId(that._elements.tabpanel[exActive]);

                dataLayer.push({
                    event: "cmp:show",
                    eventInfo: {
                        path: "component." + activeItem
                    }
                });

                dataLayer.push({
                    event: "cmp:hide",
                    eventInfo: {
                        path: "component." + exActiveItem
                    }
                });

                var tabsId = that._elements.self.id;
                var uploadPayload = { component: {} };
                uploadPayload.component[tabsId] = { shownItems: [activeItem] };

                var removePayload = { component: {} };
                removePayload.component[tabsId] = { shownItems: undefined };

                dataLayer.push(removePayload);
                dataLayer.push(uploadPayload);
            }
        }
    }

    /**
     * Reads options data from the Tabs wrapper element, defined via {@code data-cmp-*} data attributes
     *
     * @private
     * @param {HTMLElement} element The Tabs element to read options data from
     * @returns {Object} The options read from the component data attributes
     */
    function readData(element) {
        var data = element.dataset;
        var options = [];
        var capitalized = IS;
        capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
        var reserved = ["is", "hook" + capitalized];

        for (var key in data) {
            if (Object.prototype.hasOwnProperty.call(data, key)) {
                var value = data[key];

                if (key.indexOf(NS) === 0) {
                    key = key.slice(NS.length);
                    key = key.charAt(0).toLowerCase() + key.substring(1);

                    if (reserved.indexOf(key) === -1) {
                        options[key] = value;
                    }
                }
            }
        }

        return options;
    }

    /**
     * Parses the dataLayer string and returns the ID
     *
     * @private
     * @param {HTMLElement} item the accordion item
     * @returns {String} dataLayerId or undefined
     */
    function getDataLayerId(item) {
        if (item) {
            if (item.dataset.cmpDataLayer) {
                return Object.keys(JSON.parse(item.dataset.cmpDataLayer))[0];
            } else {
                return item.id;
            }
        }
        return null;
    }

    /**
     * Document ready handler and DOM mutation observers. Initializes Tabs components as necessary.
     *
     * @private
     */
    function onDocumentReady() {
        dataLayerEnabled = document.body.hasAttribute("data-cmp-data-layer-enabled");
        if (dataLayerEnabled) {
            dataLayerName = document.body.getAttribute("data-cmp-data-layer-name") || "adobeDataLayer";
            dataLayer = window[dataLayerName] = window[dataLayerName] || [];
        }

        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new Tabs({ element: elements[i], options: readData(elements[i]) });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new Tabs({ element: element, options: readData(element) });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

    if (containerUtils) {
        window.addEventListener("load", containerUtils.scrollToAnchor, false);
    }

}());


/*******************************************************************************
 * Copyright 2022 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function(document) {
    "use strict";

    window.CMP = window.CMP || {};
    window.CMP.utils = (function() {
        var NS = "cmp";

        /**
         * Reads options data from the Component wrapper element, defined via {@code data-cmp-*} data attributes
         *
         * @param {HTMLElement} element The component element to read options data from
         * @param {String} is The component identifier
         * @returns {String[]} The options read from the component data attributes
         */
        var readData = function(element, is) {
            var data = element.dataset;
            var options = [];
            var capitalized = is;
            capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
            var reserved = ["is", "hook" + capitalized];

            for (var key in data) {
                if (Object.prototype.hasOwnProperty.call(data, key)) {
                    var value = data[key];

                    if (key.indexOf(NS) === 0) {
                        key = key.slice(NS.length);
                        key = key.charAt(0).toLowerCase() + key.substring(1);

                        if (reserved.indexOf(key) === -1) {
                            options[key] = value;
                        }
                    }
                }
            }
            return options;
        };

        /**
         * Set up the final properties of a component by evaluating the transform function or fall back to the default value on demand
         * @param {String[]} options the options to transform
         * @param {Object} properties object of properties of property functions
         * @returns {Object} transformed properties
         */
        var setupProperties = function(options, properties) {
            var transformedProperties = {};

            for (var key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    var property = properties[key];
                    if (options && options[key] != null) {
                        if (property && typeof property.transform === "function") {
                            transformedProperties[key] = property.transform(options[key]);
                        } else {
                            transformedProperties[key] = options[key];
                        }
                    } else {
                        transformedProperties[key] = properties[key]["default"];
                    }
                }
            }
            return transformedProperties;
        };


        return {
            readData: readData,
            setupProperties: setupProperties
        };
    }());
}(window.document));

/*******************************************************************************
 * Copyright 2018 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function() {
    "use strict";

    var containerUtils = window.CQ && window.CQ.CoreComponents && window.CQ.CoreComponents.container && window.CQ.CoreComponents.container.utils ? window.CQ.CoreComponents.container.utils : undefined;
    if (!containerUtils) {
        // eslint-disable-next-line no-console
        console.warn("Tabs: container utilities at window.CQ.CoreComponents.container.utils are not available. This can lead to missing features. Ensure the core.wcm.components.commons.site.container client library is included on the page.");
    }
    var dataLayerEnabled;
    var dataLayer;
    var dataLayerName;

    var NS = "cmp";
    var IS = "carousel";

    var keyCodes = {
        SPACE: 32,
        END: 35,
        HOME: 36,
        ARROW_LEFT: 37,
        ARROW_UP: 38,
        ARROW_RIGHT: 39,
        ARROW_DOWN: 40
    };

    var selectors = {
        self: "[data-" + NS + '-is="' + IS + '"]'
    };

    var properties = {
        /**
         * Determines whether the Carousel will automatically transition between slides
         *
         * @memberof Carousel
         * @type {Boolean}
         * @default false
         */
        "autoplay": {
            "default": false,
            "transform": function(value) {
                return !(value === null || typeof value === "undefined");
            }
        },
        /**
         * Duration (in milliseconds) before automatically transitioning to the next slide
         *
         * @memberof Carousel
         * @type {Number}
         * @default 5000
         */
        "delay": {
            "default": 5000,
            "transform": function(value) {
                value = parseFloat(value);
                return !isNaN(value) ? value : null;
            }
        },
        /**
         * Determines whether automatic pause on hovering the carousel is disabled
         *
         * @memberof Carousel
         * @type {Boolean}
         * @default false
         */
        "autopauseDisabled": {
            "default": false,
            "transform": function(value) {
                return !(value === null || typeof value === "undefined");
            }
        }
    };

    /**
     * Carousel Configuration
     *
     * @typedef {Object} CarouselConfig Represents a Carousel configuration
     * @property {HTMLElement} element The HTMLElement representing the Carousel
     * @property {*[]} options The Carousel options
     */

    /**
     * Carousel
     *
     * @class Carousel
     * @classdesc An interactive Carousel component for navigating a list of generic items
     * @param {CarouselConfig} config The Carousel configuration
     */
    function Carousel(config) {
        var that = this;

        if (config && config.element) {
            init(config);
        }

        /**
         * Initializes the Carousel
         *
         * @private
         * @param {CarouselConfig} config The Carousel configuration
         */
        function init(config) {
            that._config = config;

            // prevents multiple initialization
            config.element.removeAttribute("data-" + NS + "-is");

            setupProperties(config.options);
            cacheElements(config.element);

            that._active = 0;
            that._paused = false;

            if (that._elements.item) {
                initializeActive();
                bindEvents();
                resetAutoplayInterval();
                refreshPlayPauseActions();
                scrollToDeepLinkIdInCarousel();
            }

            // TODO: This section is only relevant in edit mode and should move to the editor clientLib
            if (window.Granite && window.Granite.author && window.Granite.author.MessageChannel) {
                /*
                 * Editor message handling:
                 * - subscribe to "cmp.panelcontainer" message requests sent by the editor frame
                 * - check that the message data panel container type is correct and that the id (path) matches this specific Carousel component
                 * - if so, route the "navigate" operation to enact a navigation of the Carousel based on index data
                 */
                window.CQ = window.CQ || {};
                window.CQ.CoreComponents = window.CQ.CoreComponents || {};
                window.CQ.CoreComponents.MESSAGE_CHANNEL = window.CQ.CoreComponents.MESSAGE_CHANNEL || new window.Granite.author.MessageChannel("cqauthor", window);
                window.CQ.CoreComponents.MESSAGE_CHANNEL.subscribeRequestMessage("cmp.panelcontainer", function(message) {
                    if (message.data && message.data.type === "cmp-carousel" && message.data.id === that._elements.self.dataset["cmpPanelcontainerId"]) {
                        if (message.data.operation === "navigate") {
                            navigate(message.data.index);
                        }
                    }
                });
            }
        }

        /**
         * Displays the slide containing the element that corresponds to the deep link in the URI fragment
         * and scrolls the browser to this element.
         */
        function scrollToDeepLinkIdInCarousel() {
            if (containerUtils) {
                var deepLinkItemIdx = containerUtils.getDeepLinkItemIdx(that, "item", "item");
                if (deepLinkItemIdx > -1) {
                    var deepLinkItem = that._elements["item"][deepLinkItemIdx];
                    if (deepLinkItem && that._elements["item"][that._active].id !== deepLinkItem.id) {
                        navigateAndFocusIndicator(deepLinkItemIdx, true);
                        // pause the carousel auto-rotation
                        pause();
                    }
                    var hashId = window.location.hash.substring(1);
                    if (hashId) {
                        var hashItem = document.querySelector("[id='" + hashId + "']");
                        if (hashItem) {
                            hashItem.scrollIntoView();
                        }
                    }
                }
            }
        }

        /**
         * Caches the Carousel elements as defined via the {@code data-carousel-hook="ELEMENT_NAME"} markup API
         *
         * @private
         * @param {HTMLElement} wrapper The Carousel wrapper element
         */
        function cacheElements(wrapper) {
            that._elements = {};
            that._elements.self = wrapper;
            var hooks = that._elements.self.querySelectorAll("[data-" + NS + "-hook-" + IS + "]");

            for (var i = 0; i < hooks.length; i++) {
                var hook = hooks[i];
                var capitalized = IS;
                capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
                var key = hook.dataset[NS + "Hook" + capitalized];
                if (that._elements[key]) {
                    if (!Array.isArray(that._elements[key])) {
                        var tmp = that._elements[key];
                        that._elements[key] = [tmp];
                    }
                    that._elements[key].push(hook);
                } else {
                    that._elements[key] = hook;
                }
            }
        }

        /**
         * Sets up properties for the Carousel based on the passed options.
         *
         * @private
         * @param {Object} options The Carousel options
         */
        function setupProperties(options) {
            that._properties = {};

            for (var key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    var property = properties[key];
                    var value = null;

                    if (options && options[key] != null) {
                        value = options[key];

                        // transform the provided option
                        if (property && typeof property.transform === "function") {
                            value = property.transform(value);
                        }
                    }

                    if (value === null) {
                        // value still null, take the property default
                        value = properties[key]["default"];
                    }

                    that._properties[key] = value;
                }
            }
        }

        /**
         * Binds Carousel event handling
         *
         * @private
         */
        function bindEvents() {
            window.addEventListener("hashchange", scrollToDeepLinkIdInCarousel, false);
            if (that._elements["previous"]) {
                that._elements["previous"].addEventListener("click", function() {
                    var index = getPreviousIndex();
                    navigate(index);
                    if (dataLayerEnabled) {
                        dataLayer.push({
                            event: "cmp:show",
                            eventInfo: {
                                path: "component." + getDataLayerId(that._elements.item[index])
                            }
                        });
                    }
                });
            }

            if (that._elements["next"]) {
                that._elements["next"].addEventListener("click", function() {
                    var index = getNextIndex();
                    navigate(index);
                    if (dataLayerEnabled) {
                        dataLayer.push({
                            event: "cmp:show",
                            eventInfo: {
                                path: "component." + getDataLayerId(that._elements.item[index])
                            }
                        });
                    }
                });
            }

            var indicators = that._elements["indicator"];
            if (indicators) {
                for (var i = 0; i < indicators.length; i++) {
                    (function(index) {
                        indicators[i].addEventListener("click", function(event) {
                            navigateAndFocusIndicator(index);
                            // pause the carousel auto-rotation
                            pause();
                        });
                    })(i);
                }
            }

            if (that._elements["pause"]) {
                if (that._properties.autoplay) {
                    that._elements["pause"].addEventListener("click", onPauseClick);
                }
            }

            if (that._elements["play"]) {
                if (that._properties.autoplay) {
                    that._elements["play"].addEventListener("click", onPlayClick);
                }
            }

            that._elements.self.addEventListener("keydown", onKeyDown);

            if (!that._properties.autopauseDisabled) {
                that._elements.self.addEventListener("mouseenter", onMouseEnter);
                that._elements.self.addEventListener("mouseleave", onMouseLeave);
            }

            // for accessibility we pause animation when a element get focused
            var items = that._elements["item"];
            if (items) {
                for (var j = 0; j < items.length; j++) {
                    items[j].addEventListener("focusin", onMouseEnter);
                    items[j].addEventListener("focusout", onMouseLeave);
                }
            }
        }

        /**
         * Handles carousel keydown events
         *
         * @private
         * @param {Object} event The keydown event
         */
        function onKeyDown(event) {
            var index = that._active;
            var lastIndex = that._elements["indicator"].length - 1;

            switch (event.keyCode) {
                case keyCodes.ARROW_LEFT:
                case keyCodes.ARROW_UP:
                    event.preventDefault();
                    if (index > 0) {
                        navigateAndFocusIndicator(index - 1);
                    }
                    break;
                case keyCodes.ARROW_RIGHT:
                case keyCodes.ARROW_DOWN:
                    event.preventDefault();
                    if (index < lastIndex) {
                        navigateAndFocusIndicator(index + 1);
                    }
                    break;
                case keyCodes.HOME:
                    event.preventDefault();
                    navigateAndFocusIndicator(0);
                    break;
                case keyCodes.END:
                    event.preventDefault();
                    navigateAndFocusIndicator(lastIndex);
                    break;
                case keyCodes.SPACE:
                    if (that._properties.autoplay && (event.target !== that._elements["previous"] && event.target !== that._elements["next"])) {
                        event.preventDefault();
                        if (!that._paused) {
                            pause();
                        } else {
                            play();
                        }
                    }
                    if (event.target === that._elements["pause"]) {
                        that._elements["play"].focus();
                    }
                    if (event.target === that._elements["play"]) {
                        that._elements["pause"].focus();
                    }
                    break;
                default:
                    return;
            }
        }

        /**
         * Handles carousel mouseenter events
         *
         * @private
         * @param {Object} event The mouseenter event
         */
        function onMouseEnter(event) {
            clearAutoplayInterval();
        }

        /**
         * Handles carousel mouseleave events
         *
         * @private
         * @param {Object} event The mouseleave event
         */
        function onMouseLeave(event) {
            resetAutoplayInterval();
        }

        /**
         * Handles pause element click events
         *
         * @private
         * @param {Object} event The click event
         */
        function onPauseClick(event) {
            pause();
            that._elements["play"].focus();
        }

        /**
         * Handles play element click events
         *
         * @private
         * @param {Object} event The click event
         */
        function onPlayClick() {
            play();
            that._elements["pause"].focus();
        }

        /**
         * Pauses the playing of the Carousel. Sets {@code Carousel#_paused} marker.
         * Only relevant when autoplay is enabled
         *
         * @private
         */
        function pause() {
            that._paused = true;
            clearAutoplayInterval();
            refreshPlayPauseActions();
        }

        /**
         * Enables the playing of the Carousel. Sets {@code Carousel#_paused} marker.
         * Only relevant when autoplay is enabled
         *
         * @private
         */
        function play() {
            that._paused = false;

            // If the Carousel is hovered, don't begin auto transitioning until the next mouse leave event
            var hovered = false;
            if (that._elements.self.parentElement) {
                hovered = that._elements.self.parentElement.querySelector(":hover") === that._elements.self;
            }
            if (that._properties.autopauseDisabled || !hovered) {
                resetAutoplayInterval();
            }

            refreshPlayPauseActions();
        }

        /**
         * Refreshes the play/pause action markup based on the {@code Carousel#_paused} state
         *
         * @private
         */
        function refreshPlayPauseActions() {
            setActionDisabled(that._elements["pause"], that._paused);
            setActionDisabled(that._elements["play"], !that._paused);
        }

        /**
         * Initialize {@code Carousel#_active} based on the active item of the carousel.
         */
        function initializeActive() {
            var items = that._elements["item"];
            if (items && Array.isArray(items)) {
                for (var i = 0; i < items.length; i++) {
                    if (items[i].classList.contains("cmp-carousel__item--active")) {
                        that._active = i;
                        break;
                    }
                }
            }
        }

        /**
         * Refreshes the item markup based on the current {@code Carousel#_active} index
         *
         * @private
         */
        function refreshActive() {
            var items = that._elements["item"];
            var indicators = that._elements["indicator"];

            if (items) {
                if (Array.isArray(items)) {
                    for (var i = 0; i < items.length; i++) {
                        if (i === parseInt(that._active)) {
                            items[i].classList.add("cmp-carousel__item--active");
                            items[i].removeAttribute("aria-hidden");
                            indicators[i].classList.add("cmp-carousel__indicator--active");
                            indicators[i].setAttribute("aria-selected", true);
                            indicators[i].setAttribute("tabindex", "0");
                        } else {
                            items[i].classList.remove("cmp-carousel__item--active");
                            items[i].setAttribute("aria-hidden", true);
                            indicators[i].classList.remove("cmp-carousel__indicator--active");
                            indicators[i].setAttribute("aria-selected", false);
                            indicators[i].setAttribute("tabindex", "-1");
                        }
                    }
                } else {
                    // only one item
                    items.classList.add("cmp-carousel__item--active");
                    indicators.classList.add("cmp-carousel__indicator--active");
                }
            }
        }

        /**
         * Focuses the element and prevents scrolling the element into view
         *
         * @param {HTMLElement} element Element to focus
         */
        function focusWithoutScroll(element) {
            var x = window.scrollX || window.pageXOffset;
            var y = window.scrollY || window.pageYOffset;
            element.focus();
            window.scrollTo(x, y);
        }

        /**
         * Retrieves the next active index, with looping
         *
         * @private
         * @returns {Number} Index of the next carousel item
         */
        function getNextIndex() {
            return that._active === (that._elements["item"].length - 1) ? 0 : that._active + 1;
        }

        /**
         * Retrieves the previous active index, with looping
         *
         * @private
         * @returns {Number} Index of the previous carousel item
         */
        function getPreviousIndex() {
            return that._active === 0 ? (that._elements["item"].length - 1) : that._active - 1;
        }

        /**
         * Navigates to the item at the provided index
         *
         * @private
         * @param {Number} index The index of the item to navigate to
         * @param {Boolean} keepHash true to keep the hash in the URL, false to update it
         */
        function navigate(index, keepHash) {
            if (index < 0 || index > (that._elements["item"].length - 1)) {
                return;
            }

            that._active = index;
            refreshActive();

            if (!keepHash && containerUtils) {
                containerUtils.updateUrlHash(that, "item", index);
            }

            if (dataLayerEnabled) {
                var carouselId = that._elements.self.id;
                var activeItem = getDataLayerId(that._elements.item[index]);
                var updatePayload = { component: {} };
                updatePayload.component[carouselId] = { shownItems: [activeItem] };

                var removePayload = { component: {} };
                removePayload.component[carouselId] = { shownItems: undefined };

                dataLayer.push(removePayload);
                dataLayer.push(updatePayload);
            }

            // reset the autoplay transition interval following navigation, if not already hovering the carousel
            if (that._elements.self.parentElement) {
                if (that._elements.self.parentElement.querySelector(":hover") !== that._elements.self) {
                    resetAutoplayInterval();
                }
            }
        }

        /**
         * Navigates to the item at the provided index and ensures the active indicator gains focus
         *
         * @private
         * @param {Number} index The index of the item to navigate to
         * @param {Boolean} keepHash true to keep the hash in the URL, false to update it
         */
        function navigateAndFocusIndicator(index, keepHash) {
            navigate(index, keepHash);
            focusWithoutScroll(that._elements["indicator"][index]);

            if (dataLayerEnabled) {
                dataLayer.push({
                    event: "cmp:show",
                    eventInfo: {
                        path: "component." + getDataLayerId(that._elements.item[index])
                    }
                });
            }
        }

        /**
         * Starts/resets automatic slide transition interval
         *
         * @private
         */
        function resetAutoplayInterval() {
            if (that._paused || !that._properties.autoplay) {
                return;
            }
            clearAutoplayInterval();
            that._autoplayIntervalId = window.setInterval(function() {
                if (document.visibilityState && document.hidden) {
                    return;
                }
                var indicators = that._elements["indicators"];
                if (indicators !== document.activeElement && indicators.contains(document.activeElement)) {
                    // if an indicator has focus, ensure we switch focus following navigation
                    navigateAndFocusIndicator(getNextIndex(), true);
                } else {
                    navigate(getNextIndex(), true);
                }
            }, that._properties.delay);
        }

        /**
         * Clears/pauses automatic slide transition interval
         *
         * @private
         */
        function clearAutoplayInterval() {
            window.clearInterval(that._autoplayIntervalId);
            that._autoplayIntervalId = null;
        }

        /**
         * Sets the disabled state for an action and toggles the appropriate CSS classes
         *
         * @private
         * @param {HTMLElement} action Action to disable
         * @param {Boolean} [disable] {@code true} to disable, {@code false} to enable
         */
        function setActionDisabled(action, disable) {
            if (!action) {
                return;
            }
            if (disable !== false) {
                action.disabled = true;
                action.classList.add("cmp-carousel__action--disabled");
            } else {
                action.disabled = false;
                action.classList.remove("cmp-carousel__action--disabled");
            }
        }
    }

    /**
     * Parses the dataLayer string and returns the ID
     *
     * @private
     * @param {HTMLElement} item the accordion item
     * @returns {String} dataLayerId or undefined
     */
    function getDataLayerId(item) {
        if (item) {
            if (item.dataset.cmpDataLayer) {
                return Object.keys(JSON.parse(item.dataset.cmpDataLayer))[0];
            } else {
                return item.id;
            }
        }
        return null;
    }

    /**
     * Document ready handler and DOM mutation observers. Initializes Carousel components as necessary.
     *
     * @private
     */
    function onDocumentReady() {
        dataLayerEnabled = document.body.hasAttribute("data-cmp-data-layer-enabled");
        if (dataLayerEnabled) {
            dataLayerName = document.body.getAttribute("data-cmp-data-layer-name") || "adobeDataLayer";
            dataLayer = window[dataLayerName] = window[dataLayerName] || [];
        }

        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new Carousel({ element: elements[i], options: CMP.utils.readData(elements[i], IS) });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new Carousel({ element: element, options: CMP.utils.readData(element, IS) });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    var documentReady = document.readyState !== "loading" ? Promise.resolve() : new Promise(function(resolve) {
        document.addEventListener("DOMContentLoaded", resolve);
    });
    Promise.all([documentReady]).then(onDocumentReady);

    if (containerUtils) {
        window.addEventListener("load", containerUtils.scrollToAnchor, false);
    }

}());


/*******************************************************************************
 * Copyright 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
if (window.Element && !Element.prototype.closest) {
    // eslint valid-jsdoc: "off"
    Element.prototype.closest =
        function(s) {
            "use strict";
            var matches = (this.document || this.ownerDocument).querySelectorAll(s);
            var el      = this;
            var i;
            do {
                i = matches.length;
                while (--i >= 0 && matches.item(i) !== el) {
                    // continue
                }
            } while ((i < 0) && (el = el.parentElement));
            return el;
        };
}

if (window.Element && !Element.prototype.matches) {
    Element.prototype.matches =
        Element.prototype.matchesSelector ||
        Element.prototype.mozMatchesSelector ||
        Element.prototype.msMatchesSelector ||
        Element.prototype.oMatchesSelector ||
        Element.prototype.webkitMatchesSelector ||
        function(s) {
            "use strict";
            var matches = (this.document || this.ownerDocument).querySelectorAll(s);
            var i       = matches.length;
            while (--i >= 0 && matches.item(i) !== this) {
                // continue
            }
            return i > -1;
        };
}

if (!Object.assign) {
    Object.assign = function(target, varArgs) { // .length of function is 2
        "use strict";
        if (target === null) {
            throw new TypeError("Cannot convert undefined or null to object");
        }

        var to = Object(target);

        for (var index = 1; index < arguments.length; index++) {
            var nextSource = arguments[index];

            if (nextSource !== null) {
                for (var nextKey in nextSource) {
                    if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                        to[nextKey] = nextSource[nextKey];
                    }
                }
            }
        }
        return to;
    };
}

(function(arr) {
    "use strict";
    arr.forEach(function(item) {
        if (Object.prototype.hasOwnProperty.call(item, "remove")) {
            return;
        }
        Object.defineProperty(item, "remove", {
            configurable: true,
            enumerable: true,
            writable: true,
            value: function remove() {
                this.parentNode.removeChild(this);
            }
        });
    });
})([Element.prototype, CharacterData.prototype, DocumentType.prototype]);

/*******************************************************************************
 * Copyright 2022 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function(document) {
    "use strict";

    window.CMP = window.CMP || {};
    window.CMP.utils = (function() {
        var NS = "cmp";

        /**
         * Reads options data from the Component wrapper element, defined via {@code data-cmp-*} data attributes
         *
         * @param {HTMLElement} element The component element to read options data from
         * @param {String} is The component identifier
         * @returns {String[]} The options read from the component data attributes
         */
        var readData = function(element, is) {
            var data = element.dataset;
            var options = [];
            var capitalized = is;
            capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
            var reserved = ["is", "hook" + capitalized];

            for (var key in data) {
                if (Object.prototype.hasOwnProperty.call(data, key)) {
                    var value = data[key];

                    if (key.indexOf(NS) === 0) {
                        key = key.slice(NS.length);
                        key = key.charAt(0).toLowerCase() + key.substring(1);

                        if (reserved.indexOf(key) === -1) {
                            options[key] = value;
                        }
                    }
                }
            }
            return options;
        };

        /**
         * Set up the final properties of a component by evaluating the transform function or fall back to the default value on demand
         * @param {String[]} options the options to transform
         * @param {Object} properties object of properties of property functions
         * @returns {Object} transformed properties
         */
        var setupProperties = function(options, properties) {
            var transformedProperties = {};

            for (var key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    var property = properties[key];
                    if (options && options[key] != null) {
                        if (property && typeof property.transform === "function") {
                            transformedProperties[key] = property.transform(options[key]);
                        } else {
                            transformedProperties[key] = options[key];
                        }
                    } else {
                        transformedProperties[key] = properties[key]["default"];
                    }
                }
            }
            return transformedProperties;
        };


        return {
            readData: readData,
            setupProperties: setupProperties
        };
    }());
}(window.document));

/*******************************************************************************
 * Copyright 2022 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function(document) {
    "use strict";

    window.CMP = window.CMP || {};
    window.CMP.image = window.CMP.image || {};
    window.CMP.image.dynamicMedia = (function() {
        var autoSmartCrops = {};
        var SRC_URI_TEMPLATE_WIDTH_VAR = "{.width}";
        var SRC_URI_TEMPLATE_DPR_VAR = "{dpr}";
        var SRC_URI_DPR_OFF = "dpr=off";
        var SRC_URI_DPR_ON = "dpr=on,{dpr}";
        var dpr = window.devicePixelRatio || 1;
        var config = {
            minWidth: 20
        };

        /**
         * get auto smart crops from dm
         * @param {String} src the src uri
         * @returns {{}} the smart crop json object
         */
        var getAutoSmartCrops = function(src) {
            var request = new XMLHttpRequest();
            var url = src.split(SRC_URI_TEMPLATE_WIDTH_VAR)[0] + "?req=set,json";
            request.open("GET", url, false);
            request.onload = function() {
                if (request.status >= 200 && request.status < 400) {
                    // success status
                    var responseText = request.responseText;
                    var rePayload = new RegExp(/^(?:\/\*jsonp\*\/)?\s*([^()]+)\(([\s\S]+),\s*"[0-9]*"\);?$/gmi);
                    var rePayloadJSON = new RegExp(/^{[\s\S]*}$/gmi);
                    var resPayload = rePayload.exec(responseText);
                    var payload;
                    if (resPayload) {
                        var payloadStr = resPayload[2];
                        if (rePayloadJSON.test(payloadStr)) {
                            payload = JSON.parse(payloadStr);
                        }

                    }
                    // check "relation" - only in case of smartcrop preset
                    if (payload && payload.set.relation && payload.set.relation.length > 0) {
                        for (var i = 0; i < payload.set.relation.length; i++) {
                            autoSmartCrops[parseInt(payload.set.relation[i].userdata.SmartCropWidth)] =
                                ":" + payload.set.relation[i].userdata.SmartCropDef;
                        }
                    }
                } else {
                    // error status
                }
            };
            request.send();
            return autoSmartCrops;
        };

        /**
         * Build and return the srcset value based on the available auto smart crops
         * @param {String} src the src uri
         * @param {Object} smartCrops the smart crops object
         * @returns {String} the srcset
         */
        var getSrcSet = function(src, smartCrops) {
            var srcset;
            var keys = Object.keys(smartCrops);
            if (keys.length > 0) {
                srcset = [];
                for (var key in autoSmartCrops) {
                    srcset.push(src.replace(SRC_URI_TEMPLATE_WIDTH_VAR, smartCrops[key]) + " " + key + "w");
                }
            }
            return  srcset.join(",");
        };

        /**
         * Get the optimal width based on the available sizes
         * @param {[Number]} sizes the available sizes
         * @param {Number} width the element width
         * @returns {String} the optimal width
         */
        function getOptimalWidth(sizes, width) {
            var len = sizes.length;
            var key = 0;

            while ((key < len - 1) && (sizes[key] < width)) {
                key++;
            }

            return sizes[key] !== undefined ? sizes[key].toString() : width;
        }

        /**
         * Get the width of an element or parent element if the width is smaller than the minimum width
         * @param {HTMLElement} component the image component
         * @param {HTMLElement | Node} parent the parent element
         * @returns {Number} the width of the element
         */
        var getWidth = function(component, parent) {
            var width = component.offsetWidth;
            while (width < config.minWidth && parent && !component._autoWidth) {
                width =  parent.offsetWidth;
                parent = parent.parentNode;
            }
            return width;
        };

        /**
         * Set the src and srcset attribute for a Dynamic Media Image which auto smart crops enabled.
         * @param {HTMLElement} component the image component
         * @param {{}} properties the component properties
         */
        var setDMAttributes = function(component, properties) {
            // for v3 we first have to turn the dpr on
            var src = properties.src.replace(SRC_URI_DPR_OFF, SRC_URI_DPR_ON);
            src = src.replace(SRC_URI_TEMPLATE_DPR_VAR, dpr);
            var smartCrops = {};
            var width;
            if (properties["smartcroprendition"] === "SmartCrop:Auto") {
                smartCrops = getAutoSmartCrops(src);
            }
            var hasWidths = (properties.widths && properties.widths.length > 0) || Object.keys(smartCrops).length > 0;
            if (hasWidths) {
                var image = component.querySelector("img");
                var elemWidth = getWidth(component, component.parentNode);
                if (properties["smartcroprendition"] === "SmartCrop:Auto") {
                    image.setAttribute("srcset", CMP.image.dynamicMedia.getSrcSet(src, smartCrops));
                    width = getOptimalWidth(Object.keys(smartCrops, elemWidth));
                    image.setAttribute("src", CMP.image.dynamicMedia.getSrc(src, smartCrops[width]));
                } else {
                    width = getOptimalWidth(properties.widths, elemWidth);
                    image.setAttribute("src", CMP.image.dynamicMedia.getSrc(src, width));
                }
            }
        };

        /**
         * Get the src attribute based on the optimal width
         * @param {String} src the src uri
         * @param {String} width the element width
         * @returns {String} the final src attribute
         */
        var getSrc = function(src, width) {
            if (src.indexOf(SRC_URI_TEMPLATE_WIDTH_VAR) > -1) {
                src = src.replace(SRC_URI_TEMPLATE_WIDTH_VAR, width);
            }
            return src;
        };


        return {
            getAutoSmartCrops: getAutoSmartCrops,
            getSrcSet: getSrcSet,
            getSrc: getSrc,
            setDMAttributes: setDMAttributes,
            getWidth: getWidth
        };
    }());
    document.dispatchEvent(new CustomEvent("core.wcm.components.commons.site.image.dynamic-media.loaded"));
}(window.document));

/*******************************************************************************
 * Copyright 2016 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function() {
    "use strict";

    var NS = "cmp";
    var IS = "image";

    var EMPTY_PIXEL = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    var LAZY_THRESHOLD_DEFAULT = 0;
    var SRC_URI_TEMPLATE_WIDTH_VAR = "{.width}";
    var SRC_URI_TEMPLATE_WIDTH_VAR_ASSET_DELIVERY = "width={width}";
    var SRC_URI_TEMPLATE_DPR_VAR = "{dpr}";

    var selectors = {
        self: "[data-" + NS + '-is="' + IS + '"]',
        image: '[data-cmp-hook-image="image"]',
        map: '[data-cmp-hook-image="map"]',
        area: '[data-cmp-hook-image="area"]'
    };

    var lazyLoader = {
        "cssClass": "cmp-image__image--is-loading",
        "style": {
            "height": 0,
            "padding-bottom": "" // will be replaced with % ratio
        }
    };

    var properties = {
        /**
         * An array of alternative image widths (in pixels).
         * Used to replace a {.width} variable in the src property with an optimal width if a URI template is provided.
         *
         * @memberof Image
         * @type {Number[]}
         * @default []
         */
        "widths": {
            "default": [],
            "transform": function(value) {
                var widths = [];
                value.split(",").forEach(function(item) {
                    item = parseFloat(item);
                    if (!isNaN(item)) {
                        widths.push(item);
                    }
                });
                return widths;
            }
        },
        /**
         * Indicates whether the image should be rendered lazily.
         *
         * @memberof Image
         * @type {Boolean}
         * @default false
         */
        "lazy": {
            "default": false,
            "transform": function(value) {
                return !(value === null || typeof value === "undefined");
            }
        },
        /**
         * Indicates image is DynamicMedia image.
         *
         * @memberof Image
         * @type {Boolean}
         * @default false
         */
        "dmimage": {
            "default": false,
            "transform": function(value) {
                return !(value === null || typeof value === "undefined");
            }
        },
        /**
         * The lazy threshold.
         * This is the number of pixels, in advance of becoming visible, when an lazy-loading image should begin
         * to load.
         *
         * @memberof Image
         * @type {Number}
         * @default 0
         */
        "lazythreshold": {
            "default": 0,
            "transform": function(value) {
                var val =  parseInt(value);
                if (isNaN(val)) {
                    return LAZY_THRESHOLD_DEFAULT;
                }
                return val;
            }
        },
        /**
         * The image source.
         *
         * Can be a simple image source, or a URI template representation that
         * can be variable expanded - useful for building an image configuration with an alternative width.
         * e.g. '/path/image.coreimg{.width}.jpeg/1506620954214.jpeg'
         *
         * @memberof Image
         * @type {String}
         */
        "src": {
            "transform": function(value) {
                return decodeURIComponent(value);
            }
        }
    };

    var devicePixelRatio = window.devicePixelRatio || 1;

    function Image(config) {
        var that = this;

        var smartCrops = {};

        var useAssetDelivery = false;
        var srcUriTemplateWidthVar = SRC_URI_TEMPLATE_WIDTH_VAR;

        function init(config) {
            // prevents multiple initialization
            config.element.removeAttribute("data-" + NS + "-is");

            // check if asset delivery is used
            if (config.options.src && config.options.src.indexOf(SRC_URI_TEMPLATE_WIDTH_VAR_ASSET_DELIVERY) >= 0) {
                useAssetDelivery = true;
                srcUriTemplateWidthVar = SRC_URI_TEMPLATE_WIDTH_VAR_ASSET_DELIVERY;
            }

            that._properties = CMP.utils.setupProperties(config.options, properties);
            cacheElements(config.element);
            // check image is DM asset; if true try to make req=set
            if (config.options.src && Object.prototype.hasOwnProperty.call(config.options, "dmimage") && (config.options["smartcroprendition"] === "SmartCrop:Auto")) {
                smartCrops = CMP.image.dynamicMedia.getAutoSmartCrops(config.options.src);
            }

            if (!that._elements.noscript) {
                return;
            }

            that._elements.container = that._elements.link ? that._elements.link : that._elements.self;

            unwrapNoScript();

            if (that._properties.lazy) {
                addLazyLoader();
            }

            if (that._elements.map) {
                that._elements.image.addEventListener("load", onLoad);
            }

            window.addEventListener("resize", onWindowResize);
            ["focus", "click", "load", "transitionend", "animationend", "scroll"].forEach(function(name) {
                document.addEventListener(name, that.update);
            });

            that._elements.image.addEventListener("cmp-image-redraw", that.update);

            that._interSectionObserver = new IntersectionObserver(function(entries, interSectionObserver) {
                entries.forEach(function(entry) {
                    if (entry.intersectionRatio > 0) {
                        that.update();
                    }
                });
            });
            that._interSectionObserver.observe(that._elements.self);

            that.update();
        }

        function loadImage() {
            var hasWidths = (that._properties.widths && that._properties.widths.length > 0) || Object.keys(smartCrops).length > 0;
            var replacement;
            if (Object.keys(smartCrops).length > 0) {
                var optimalWidth = getOptimalWidth(Object.keys(smartCrops), false);
                replacement = smartCrops[optimalWidth];
            } else {
                replacement = hasWidths ? (that._properties.dmimage ? "" : ".") + getOptimalWidth(that._properties.widths, true) : "";
            }
            if (useAssetDelivery) {
                replacement = replacement !== "" ? ("width=" + replacement.substring(1)) : "";
            }
            var url = that._properties.src.replace(srcUriTemplateWidthVar, replacement);
            url = url.replace(SRC_URI_TEMPLATE_DPR_VAR, devicePixelRatio);

            var imgSrcAttribute = that._elements.image.getAttribute("src");

            if (url !== imgSrcAttribute) {
                if (imgSrcAttribute === null || imgSrcAttribute === EMPTY_PIXEL) {
                    that._elements.image.setAttribute("src", url);
                } else {
                    var urlTemplateParts = that._properties.src.split(srcUriTemplateWidthVar);
                    // check if image src was dynamically swapped meanwhile (e.g. by Target)
                    var isImageRefSame = imgSrcAttribute.startsWith(urlTemplateParts[0]);
                    if (isImageRefSame && urlTemplateParts.length > 1) {
                        isImageRefSame = imgSrcAttribute.endsWith(urlTemplateParts[urlTemplateParts.length - 1]);
                    }
                    if (isImageRefSame) {
                        that._elements.image.setAttribute("src", url);
                        if (!hasWidths) {
                            window.removeEventListener("scroll", that.update);
                        }
                    }
                }
            }
            if (that._lazyLoaderShowing) {
                that._elements.image.addEventListener("load", removeLazyLoader);
            }
            that._interSectionObserver.unobserve(that._elements.self);
        }

        function getOptimalWidth(widths, useDevicePixelRatio) {
            var container = that._elements.self;
            var containerWidth = container.clientWidth;
            while (containerWidth === 0 && container.parentNode) {
                container = container.parentNode;
                containerWidth = container.clientWidth;
            }

            var dpr = useDevicePixelRatio ? devicePixelRatio : 1;
            var optimalWidth = containerWidth * dpr;
            var len = widths.length;
            var key = 0;

            while ((key < len - 1) && (widths[key] < optimalWidth)) {
                key++;
            }

            return widths[key].toString();
        }

        function addLazyLoader() {
            var width = that._elements.image.getAttribute("width");
            var height = that._elements.image.getAttribute("height");

            if (width && height) {
                var ratio = (height / width) * 100;
                var styles = lazyLoader.style;

                styles["padding-bottom"] = ratio + "%";

                for (var s in styles) {
                    if (Object.prototype.hasOwnProperty.call(styles, s)) {
                        that._elements.image.style[s] = styles[s];
                    }
                }
            }
            that._elements.image.setAttribute("src", EMPTY_PIXEL);
            that._elements.image.classList.add(lazyLoader.cssClass);
            that._lazyLoaderShowing = true;
        }

        function unwrapNoScript() {
            var markup = decodeNoscript(that._elements.noscript.textContent.trim());
            var parser = new DOMParser();

            // temporary document avoids requesting the image before removing its src
            var temporaryDocument = parser.parseFromString(markup, "text/html");
            var imageElement = temporaryDocument.querySelector(selectors.image);
            imageElement.removeAttribute("src");
            that._elements.container.insertBefore(imageElement, that._elements.noscript);

            var mapElement = temporaryDocument.querySelector(selectors.map);
            if (mapElement) {
                that._elements.container.insertBefore(mapElement, that._elements.noscript);
            }

            that._elements.noscript.parentNode.removeChild(that._elements.noscript);
            if (that._elements.container.matches(selectors.image)) {
                that._elements.image = that._elements.container;
            } else {
                that._elements.image = that._elements.container.querySelector(selectors.image);
            }

            that._elements.map = that._elements.container.querySelector(selectors.map);
            that._elements.areas = that._elements.container.querySelectorAll(selectors.area);
        }

        function removeLazyLoader() {
            that._elements.image.classList.remove(lazyLoader.cssClass);
            for (var property in lazyLoader.style) {
                if (Object.prototype.hasOwnProperty.call(lazyLoader.style, property)) {
                    that._elements.image.style[property] = "";
                }
            }
            that._elements.image.removeEventListener("load", removeLazyLoader);
            that._lazyLoaderShowing = false;
        }

        function isLazyVisible() {
            if (that._elements.container.offsetParent === null) {
                return false;
            }

            var wt = window.pageYOffset;
            var wb = wt + document.documentElement.clientHeight;
            var et = that._elements.container.getBoundingClientRect().top + wt;
            var eb = et + that._elements.container.clientHeight;

            return eb >= wt - that._properties.lazythreshold && et <= wb + that._properties.lazythreshold;
        }

        function resizeAreas() {
            if (that._elements.areas && that._elements.areas.length > 0) {
                for (var i = 0; i < that._elements.areas.length; i++) {
                    var width = that._elements.image.width;
                    var height = that._elements.image.height;

                    if (width && height) {
                        var relcoords = that._elements.areas[i].dataset.cmpRelcoords;
                        if (relcoords) {
                            var relativeCoordinates = relcoords.split(",");
                            var coordinates = new Array(relativeCoordinates.length);

                            for (var j = 0; j < coordinates.length; j++) {
                                if (j % 2 === 0) {
                                    coordinates[j] = parseInt(relativeCoordinates[j] * width);
                                } else {
                                    coordinates[j] = parseInt(relativeCoordinates[j] * height);
                                }
                            }

                            that._elements.areas[i].coords = coordinates;
                        }
                    }
                }
            }
        }

        function cacheElements(wrapper) {
            that._elements = {};
            that._elements.self = wrapper;
            var hooks = that._elements.self.querySelectorAll("[data-" + NS + "-hook-" + IS + "]");

            for (var i = 0; i < hooks.length; i++) {
                var hook = hooks[i];
                var capitalized = IS;
                capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
                var key = hook.dataset[NS + "Hook" + capitalized];
                that._elements[key] = hook;
            }
        }

        function onWindowResize() {
            that.update();
            resizeAreas();
        }

        function onLoad() {
            resizeAreas();
        }

        that.update = function() {
            if (that._properties.lazy) {
                if (isLazyVisible()) {
                    loadImage();
                }
            } else {
                loadImage();
            }
        };

        if (config && config.element) {
            init(config);
        }
    }

    function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new Image({ element: elements[i], options: CMP.utils.readData(elements[i], IS) });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body             = document.querySelector("body");
        var observer         = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new Image({ element: element, options: CMP.utils.readData(element, IS) });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    var documentReady = document.readyState !== "loading" ? Promise.resolve() : new Promise(function(resolve) {
        document.addEventListener("DOMContentLoaded", resolve);
    });

    Promise.all([documentReady]).then(onDocumentReady);
    /*
        on drag & drop of the component into a parsys, noscript's content will be escaped multiple times by the editor which creates
        the DOM for editing; the HTML parser cannot be used here due to the multiple escaping
     */
    function decodeNoscript(text) {
        text = text.replace(/&(amp;)*lt;/g, "<");
        text = text.replace(/&(amp;)*gt;/g, ">");
        return text;
    }

})();


/*******************************************************************************
 * Copyright 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 * Element.matches()
 * https://developer.mozilla.org/enUS/docs/Web/API/Element/matches#Polyfill
 */
if (!Element.prototype.matches) {
    Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
}

// eslint-disable-next-line valid-jsdoc
/**
 * Element.closest()
 * https://developer.mozilla.org/enUS/docs/Web/API/Element/closest#Polyfill
 */
if (!Element.prototype.closest) {
    Element.prototype.closest = function(s) {
        "use strict";
        var el = this;
        if (!document.documentElement.contains(el)) {
            return null;
        }
        do {
            if (el.matches(s)) {
                return el;
            }
            el = el.parentElement || el.parentNode;
        } while (el !== null && el.nodeType === 1);
        return null;
    };
}

/*******************************************************************************
 * Copyright 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function() {
    "use strict";

    var containerUtils = window.CQ && window.CQ.CoreComponents && window.CQ.CoreComponents.container && window.CQ.CoreComponents.container.utils ? window.CQ.CoreComponents.container.utils : undefined;
    if (!containerUtils) {
        // eslint-disable-next-line no-console
        console.warn("Accordion: container utilities at window.CQ.CoreComponents.container.utils are not available. This can lead to missing features. Ensure the core.wcm.components.commons.site.container client library is included on the page.");
    }
    var dataLayerEnabled;
    var dataLayer;
    var dataLayerName;
    var delay = 100;

    var NS = "cmp";
    var IS = "accordion";

    var keyCodes = {
        ENTER: 13,
        SPACE: 32,
        END: 35,
        HOME: 36,
        ARROW_LEFT: 37,
        ARROW_UP: 38,
        ARROW_RIGHT: 39,
        ARROW_DOWN: 40
    };

    var selectors = {
        self: "[data-" + NS + '-is="' + IS + '"]'
    };

    var cssClasses = {
        button: {
            disabled: "cmp-accordion__button--disabled",
            expanded: "cmp-accordion__button--expanded"
        },
        panel: {
            hidden: "cmp-accordion__panel--hidden",
            expanded: "cmp-accordion__panel--expanded"
        }
    };

    var dataAttributes = {
        item: {
            expanded: "data-cmp-expanded"
        }
    };

    var properties = {
        /**
         * Determines whether a single accordion item is forced to be expanded at a time.
         * Expanding one item will collapse all others.
         *
         * @memberof Accordion
         * @type {Boolean}
         * @default false
         */
        "singleExpansion": {
            "default": false,
            "transform": function(value) {
                return !(value === null || typeof value === "undefined");
            }
        }
    };

    /**
     * Accordion Configuration.
     *
     * @typedef {Object} AccordionConfig Represents an Accordion configuration
     * @property {HTMLElement} element The HTMLElement representing the Accordion
     * @property {Object} options The Accordion options
     */

    /**
     * Accordion.
     *
     * @class Accordion
     * @classdesc An interactive Accordion component for toggling panels of related content
     * @param {AccordionConfig} config The Accordion configuration
     */
    function Accordion(config) {
        var that = this;

        if (config && config.element) {
            init(config);
        }

        /**
         * Initializes the Accordion.
         *
         * @private
         * @param {AccordionConfig} config The Accordion configuration
         */
        function init(config) {
            that._config = config;

            // prevents multiple initialization
            config.element.removeAttribute("data-" + NS + "-is");

            setupProperties(config.options);
            cacheElements(config.element);

            if (that._elements["item"]) {
                // ensures multiple element types are arrays.
                that._elements["item"] = Array.isArray(that._elements["item"]) ? that._elements["item"] : [that._elements["item"]];
                that._elements["button"] = Array.isArray(that._elements["button"]) ? that._elements["button"] : [that._elements["button"]];
                that._elements["panel"] = Array.isArray(that._elements["panel"]) ? that._elements["panel"] : [that._elements["panel"]];

                if (that._properties.singleExpansion) {
                    var expandedItems = getExpandedItems();
                    // multiple expanded items annotated, display the last item open.
                    if (expandedItems.length > 1) {
                        toggle(expandedItems.length - 1);
                    }
                }

                refreshItems();
                bindEvents();
                scrollToDeepLinkIdInAccordion();
            }
            if (window.Granite && window.Granite.author && window.Granite.author.MessageChannel) {
                /*
                 * Editor message handling:
                 * - subscribe to "cmp.panelcontainer" message requests sent by the editor frame
                 * - check that the message data panel container type is correct and that the id (path) matches this specific Accordion component
                 * - if so, route the "navigate" operation to enact a navigation of the Accordion based on index data
                 */
                window.CQ.CoreComponents.MESSAGE_CHANNEL = window.CQ.CoreComponents.MESSAGE_CHANNEL || new window.Granite.author.MessageChannel("cqauthor", window);
                window.CQ.CoreComponents.MESSAGE_CHANNEL.subscribeRequestMessage("cmp.panelcontainer", function(message) {
                    if (message.data && message.data.type === "cmp-accordion" && message.data.id === that._elements.self.dataset["cmpPanelcontainerId"]) {
                        if (message.data.operation === "navigate") {
                            // switch to single expansion mode when navigating in edit mode.
                            var singleExpansion = that._properties.singleExpansion;
                            that._properties.singleExpansion = true;
                            toggle(message.data.index);

                            // revert to the configured state.
                            that._properties.singleExpansion = singleExpansion;
                        }
                    }
                });
            }
        }

        /**
         * Displays the panel containing the element that corresponds to the deep link in the URI fragment
         * and scrolls the browser to this element.
         */
        function scrollToDeepLinkIdInAccordion() {
            if (containerUtils) {
                var deepLinkItemIdx = containerUtils.getDeepLinkItemIdx(that, "item", "item");
                if (deepLinkItemIdx > -1) {
                    var deepLinkItem = that._elements["item"][deepLinkItemIdx];
                    if (deepLinkItem && !deepLinkItem.hasAttribute(dataAttributes.item.expanded)) {
                        // if single expansion: close all accordion items
                        if (that._properties.singleExpansion) {
                            for (var j = 0; j < that._elements["item"].length; j++) {
                                if (that._elements["item"][j].hasAttribute(dataAttributes.item.expanded)) {
                                    setItemExpanded(that._elements["item"][j], false, true);
                                }
                            }
                        }
                        // expand the accordion item containing the deep link
                        setItemExpanded(deepLinkItem, true, true);
                    }
                    var hashId = window.location.hash.substring(1);
                    if (hashId) {
                        var hashItem = document.querySelector("[id='" + hashId + "']");
                        if (hashItem) {
                            hashItem.scrollIntoView();
                        }
                    }
                }
            }
        }

        /**
         * Caches the Accordion elements as defined via the {@code data-accordion-hook="ELEMENT_NAME"} markup API.
         *
         * @private
         * @param {HTMLElement} wrapper The Accordion wrapper element
         */
        function cacheElements(wrapper) {
            that._elements = {};
            that._elements.self = wrapper;
            var hooks = that._elements.self.querySelectorAll("[data-" + NS + "-hook-" + IS + "]");

            for (var i = 0; i < hooks.length; i++) {
                var hook = hooks[i];
                if (hook.closest("." + NS + "-" + IS) === that._elements.self) { // only process own accordion elements
                    var capitalized = IS;
                    capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
                    var key = hook.dataset[NS + "Hook" + capitalized];
                    if (that._elements[key]) {
                        if (!Array.isArray(that._elements[key])) {
                            var tmp = that._elements[key];
                            that._elements[key] = [tmp];
                        }
                        that._elements[key].push(hook);
                    } else {
                        that._elements[key] = hook;
                    }
                }
            }
        }

        /**
         * Sets up properties for the Accordion based on the passed options.
         *
         * @private
         * @param {Object} options The Accordion options
         */
        function setupProperties(options) {
            that._properties = {};

            for (var key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    var property = properties[key];
                    var value = null;

                    if (options && options[key] != null) {
                        value = options[key];

                        // transform the provided option
                        if (property && typeof property.transform === "function") {
                            value = property.transform(value);
                        }
                    }

                    if (value === null) {
                        // value still null, take the property default
                        value = properties[key]["default"];
                    }

                    that._properties[key] = value;
                }
            }
        }

        /**
         * Binds Accordion event handling.
         *
         * @private
         */
        function bindEvents() {
            window.addEventListener("hashchange", scrollToDeepLinkIdInAccordion, false);
            var buttons = that._elements["button"];
            if (buttons) {
                for (var i = 0; i < buttons.length; i++) {
                    (function(index) {
                        buttons[i].addEventListener("click", function(event) {
                            toggle(index);
                            focusButton(index);
                        });
                        buttons[i].addEventListener("keydown", function(event) {
                            onButtonKeyDown(event, index);
                        });
                    })(i);
                }
            }
        }

        /**
         * Handles button keydown events.
         *
         * @private
         * @param {Object} event The keydown event
         * @param {Number} index The index of the button triggering the event
         */
        function onButtonKeyDown(event, index) {
            var lastIndex = that._elements["button"].length - 1;

            switch (event.keyCode) {
                case keyCodes.ARROW_LEFT:
                case keyCodes.ARROW_UP:
                    event.preventDefault();
                    if (index > 0) {
                        focusButton(index - 1);
                    }
                    break;
                case keyCodes.ARROW_RIGHT:
                case keyCodes.ARROW_DOWN:
                    event.preventDefault();
                    if (index < lastIndex) {
                        focusButton(index + 1);
                    }
                    break;
                case keyCodes.HOME:
                    event.preventDefault();
                    focusButton(0);
                    break;
                case keyCodes.END:
                    event.preventDefault();
                    focusButton(lastIndex);
                    break;
                case keyCodes.ENTER:
                case keyCodes.SPACE:
                    event.preventDefault();
                    toggle(index);
                    focusButton(index);
                    break;
                default:
                    return;
            }
        }

        /**
         * General handler for toggle of an item.
         *
         * @private
         * @param {Number} index The index of the item to toggle
         */
        function toggle(index) {
            var item = that._elements["item"][index];
            if (item) {
                if (that._properties.singleExpansion) {
                    // ensure only a single item is expanded if single expansion is enabled.
                    for (var i = 0; i < that._elements["item"].length; i++) {
                        if (that._elements["item"][i] !== item) {
                            var expanded = getItemExpanded(that._elements["item"][i]);
                            if (expanded) {
                                setItemExpanded(that._elements["item"][i], false);
                            }
                        }
                    }
                }
                setItemExpanded(item, !getItemExpanded(item));

                if (dataLayerEnabled) {
                    var accordionId = that._elements.self.id;
                    var expandedItems = getExpandedItems()
                        .map(function(item) {
                            return getDataLayerId(item);
                        });

                    var uploadPayload = { component: {} };
                    uploadPayload.component[accordionId] = { shownItems: expandedItems };

                    var removePayload = { component: {} };
                    removePayload.component[accordionId] = { shownItems: undefined };

                    dataLayer.push(removePayload);
                    dataLayer.push(uploadPayload);
                }
            }
        }

        /**
         * Sets an item's expanded state based on the provided flag and refreshes its internals.
         *
         * @private
         * @param {HTMLElement} item The item to mark as expanded, or not expanded
         * @param {Boolean} expanded true to mark the item expanded, false otherwise
         * @param {Boolean} keepHash true to keep the hash in the URL, false to update it
         */
        function setItemExpanded(item, expanded, keepHash) {
            if (expanded) {
                item.setAttribute(dataAttributes.item.expanded, "");
                var index = that._elements["item"].indexOf(item);
                if (!keepHash && containerUtils) {
                    containerUtils.updateUrlHash(that, "item", index);
                }
                if (dataLayerEnabled) {
                    dataLayer.push({
                        event: "cmp:show",
                        eventInfo: {
                            path: "component." + getDataLayerId(item)
                        }
                    });
                }

            } else {
                item.removeAttribute(dataAttributes.item.expanded);
                if (!keepHash && containerUtils) {
                    containerUtils.removeUrlHash();
                }
                if (dataLayerEnabled) {
                    dataLayer.push({
                        event: "cmp:hide",
                        eventInfo: {
                            path: "component." + getDataLayerId(item)
                        }
                    });
                }
            }
            refreshItem(item);
        }

        /**
         * Gets an item's expanded state.
         *
         * @private
         * @param {HTMLElement} item The item for checking its expanded state
         * @returns {Boolean} true if the item is expanded, false otherwise
         */
        function getItemExpanded(item) {
            return item && item.dataset && item.dataset["cmpExpanded"] !== undefined;
        }

        /**
         * Refreshes an item based on its expanded state.
         *
         * @private
         * @param {HTMLElement} item The item to refresh
         */
        function refreshItem(item) {
            var expanded = getItemExpanded(item);
            if (expanded) {
                expandItem(item);
            } else {
                collapseItem(item);
            }
        }

        /**
         * Refreshes all items based on their expanded state.
         *
         * @private
         */
        function refreshItems() {
            for (var i = 0; i < that._elements["item"].length; i++) {
                refreshItem(that._elements["item"][i]);
            }
        }

        /**
         * Returns all expanded items.
         *
         * @private
         * @returns {HTMLElement[]} The expanded items
         */
        function getExpandedItems() {
            var expandedItems = [];

            for (var i = 0; i < that._elements["item"].length; i++) {
                var item = that._elements["item"][i];
                var expanded = getItemExpanded(item);
                if (expanded) {
                    expandedItems.push(item);
                }
            }

            return expandedItems;
        }

        /**
         * Annotates the item and its internals with
         * the necessary style and accessibility attributes to indicate it is expanded.
         *
         * @private
         * @param {HTMLElement} item The item to annotate as expanded
         */
        function expandItem(item) {
            var index = that._elements["item"].indexOf(item);
            if (index > -1) {
                var button = that._elements["button"][index];
                var panel = that._elements["panel"][index];
                button.classList.add(cssClasses.button.expanded);
                // used to fix some known screen readers issues in reading the correct state of the 'aria-expanded' attribute
                // e.g. https://bugs.webkit.org/show_bug.cgi?id=210934
                setTimeout(function() {
                    button.setAttribute("aria-expanded", true);
                }, delay);
                panel.classList.add(cssClasses.panel.expanded);
                panel.classList.remove(cssClasses.panel.hidden);
                panel.setAttribute("aria-hidden", false);
            }
        }

        /**
         * Annotates the item and its internals with
         * the necessary style and accessibility attributes to indicate it is not expanded.
         *
         * @private
         * @param {HTMLElement} item The item to annotate as not expanded
         */
        function collapseItem(item) {
            var index = that._elements["item"].indexOf(item);
            if (index > -1) {
                var button = that._elements["button"][index];
                var panel = that._elements["panel"][index];
                button.classList.remove(cssClasses.button.expanded);
                // used to fix some known screen readers issues in reading the correct state of the 'aria-expanded' attribute
                // e.g. https://bugs.webkit.org/show_bug.cgi?id=210934
                setTimeout(function() {
                    button.setAttribute("aria-expanded", false);
                }, delay);
                panel.classList.add(cssClasses.panel.hidden);
                panel.classList.remove(cssClasses.panel.expanded);
                panel.setAttribute("aria-hidden", true);
            }
        }

        /**
         * Focuses the button at the provided index.
         *
         * @private
         * @param {Number} index The index of the button to focus
         */
        function focusButton(index) {
            var button = that._elements["button"][index];
            button.focus();
        }
    }

    /**
     * Reads options data from the Accordion wrapper element, defined via {@code data-cmp-*} data attributes.
     *
     * @private
     * @param {HTMLElement} element The Accordion element to read options data from
     * @returns {Object} The options read from the component data attributes
     */
    function readData(element) {
        var data = element.dataset;
        var options = [];
        var capitalized = IS;
        capitalized = capitalized.charAt(0).toUpperCase() + capitalized.slice(1);
        var reserved = ["is", "hook" + capitalized];

        for (var key in data) {
            if (Object.prototype.hasOwnProperty.call(data, key)) {
                var value = data[key];

                if (key.indexOf(NS) === 0) {
                    key = key.slice(NS.length);
                    key = key.charAt(0).toLowerCase() + key.substring(1);

                    if (reserved.indexOf(key) === -1) {
                        options[key] = value;
                    }
                }
            }
        }

        return options;
    }

    /**
     * Parses the dataLayer string and returns the ID
     *
     * @private
     * @param {HTMLElement} item the accordion item
     * @returns {String} dataLayerId or undefined
     */
    function getDataLayerId(item) {
        if (item) {
            if (item.dataset.cmpDataLayer) {
                return Object.keys(JSON.parse(item.dataset.cmpDataLayer))[0];
            } else {
                return item.id;
            }
        }
        return null;
    }

    /**
     * Document ready handler and DOM mutation observers. Initializes Accordion components as necessary.
     *
     * @private
     */
    function onDocumentReady() {
        dataLayerEnabled = document.body.hasAttribute("data-cmp-data-layer-enabled");
        if (dataLayerEnabled) {
            dataLayerName = document.body.getAttribute("data-cmp-data-layer-name") || "adobeDataLayer";
            dataLayer = window[dataLayerName] = window[dataLayerName] || [];
        }

        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new Accordion({ element: elements[i], options: readData(elements[i]) });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new Accordion({ element: element, options: readData(element) });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

    if (containerUtils) {
        window.addEventListener("load", containerUtils.scrollToAnchor, false);
    }

}());



(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{function e(){const e=document.createElement("div");return e.className="play-button-wrapper",e.innerHTML='<div class="btn-play-container">\n      <calcite-icon scale="s" appearance="solid" icon="play-f"></calcite-icon>\n    </div>',e}document.querySelectorAll(".esri-teaser:not(.case-study) a.cmp-teaser__link").forEach(t=>{let r;try{r=new URL(t.href,window.location.origin).hostname.toLowerCase()}catch(e){return}if("mediaspace.esri.com"!==r)return;const n=t.closest(".cmp-teaser");if(!n)return;n.closest(".esri-teaser.card-standard")&&t.setAttribute("data-modal","true");const a=n.querySelector("div.cmp-teaser__image"),o=n.querySelector("img.cmp-teaser__image");if(a&&!a.querySelector(".play-button-wrapper")&&a.prepend(e()),o&&!o.parentElement.classList.contains("play-button-wrapper")){const t=e();o.parentNode.insertBefore(t,o),t.appendChild(o)}});document.querySelectorAll(".esri-teaser.case-study").forEach(e=>{const t=e.querySelector("a.cmp-teaser__action-link");t&&(t.classList.add("esri-ui-button","esri-ui-button--outline","esri-ui-button--neutral"),function(e){if(!e)return!1;try{return"mediaspace.esri.com"===new URL(e,window.location.origin).hostname.toLowerCase()}catch(e){return!1}}(t.href)&&(t.classList.add("icon-end","icon-play"),t.setAttribute("data-modal","true")));const r=e.querySelector(".cmp-teaser__title");if(r){const e=r.querySelector("a.cmp-teaser__title-link");e&&(r.textContent=e.textContent,e.remove())}})})})();
(()=>{var t={7168(t,e,i){var n;!function(s,a,r,o){"use strict";var c,l=["","webkit","Moz","MS","ms","o"],h=a.createElement("div"),u=Math.round,d=Math.abs,p=Date.now;function v(t,e,i){return setTimeout(w(t,i),e)}function f(t,e,i){return!!Array.isArray(t)&&(m(t,i[e],i),!0)}function m(t,e,i){var n;if(t)if(t.forEach)t.forEach(e,i);else if(t.length!==o)for(n=0;n<t.length;)e.call(i,t[n],n,t),n++;else for(n in t)t.hasOwnProperty(n)&&e.call(i,t[n],n,t)}function b(t,e,i){var n="DEPRECATED METHOD: "+e+"\n"+i+" AT \n";return function(){var e=new Error("get-stack-trace"),i=e&&e.stack?e.stack.replace(/^[^\(]+?[\n$]/gm,"").replace(/^\s+at\s+/gm,"").replace(/^Object.<anonymous>\s*\(/gm,"{anonymous}()@"):"Unknown Stack Trace",a=s.console&&(s.console.warn||s.console.log);return a&&a.call(s.console,n,i),t.apply(this,arguments)}}c="function"!=typeof Object.assign?function(t){if(t===o||null===t)throw new TypeError("Cannot convert undefined or null to object");for(var e=Object(t),i=1;i<arguments.length;i++){var n=arguments[i];if(n!==o&&null!==n)for(var s in n)n.hasOwnProperty(s)&&(e[s]=n[s])}return e}:Object.assign;var T=b(function(t,e,i){for(var n=Object.keys(e),s=0;s<n.length;)(!i||i&&t[n[s]]===o)&&(t[n[s]]=e[n[s]]),s++;return t},"extend","Use `assign`."),g=b(function(t,e){return T(t,e,!0)},"merge","Use `assign`.");function y(t,e,i){var n,s=e.prototype;(n=t.prototype=Object.create(s)).constructor=t,n._super=s,i&&c(n,i)}function w(t,e){return function(){return t.apply(e,arguments)}}function C(t,e){return"function"==typeof t?t.apply(e&&e[0]||o,e):t}function E(t,e){return t===o?e:t}function I(t,e,i){m(A(e),function(e){t.addEventListener(e,i,!1)})}function x(t,e,i){m(A(e),function(e){t.removeEventListener(e,i,!1)})}function S(t,e){for(;t;){if(t==e)return!0;t=t.parentNode}return!1}function _(t,e){return t.indexOf(e)>-1}function A(t){return t.trim().split(/\s+/g)}function L(t,e,i){if(t.indexOf&&!i)return t.indexOf(e);for(var n=0;n<t.length;){if(i&&t[n][i]==e||!i&&t[n]===e)return n;n++}return-1}function N(t){return Array.prototype.slice.call(t,0)}function P(t,e,i){for(var n=[],s=[],a=0;a<t.length;){var r=e?t[a][e]:t[a];L(s,r)<0&&n.push(t[a]),s[a]=r,a++}return i&&(n=e?n.sort(function(t,i){return t[e]>i[e]}):n.sort()),n}function D(t,e){for(var i,n,s=e[0].toUpperCase()+e.slice(1),a=0;a<l.length;){if((n=(i=l[a])?i+s:e)in t)return n;a++}return o}var W=1;function M(t){var e=t.ownerDocument||t;return e.defaultView||e.parentWindow||s}var k="ontouchstart"in s,R=D(s,"PointerEvent")!==o,U=k&&/mobile|tablet|ip(ad|hone|od)|android/i.test(navigator.userAgent),O="touch",F="mouse",z=24,H=["x","y"],q=["clientX","clientY"];function Y(t,e){var i=this;this.manager=t,this.callback=e,this.element=t.element,this.target=t.options.inputTarget,this.domHandler=function(e){C(t.options.enable,[t])&&i.handler(e)},this.init()}function X(t,e,i){var n=i.pointers.length,s=i.changedPointers.length,a=1&e&&n-s===0,r=12&e&&n-s===0;i.isFirst=!!a,i.isFinal=!!r,a&&(t.session={}),i.eventType=e,function(t,e){var i=t.session,n=e.pointers,s=n.length;i.firstInput||(i.firstInput=B(e));s>1&&!i.firstMultiple?i.firstMultiple=B(e):1===s&&(i.firstMultiple=!1);var a=i.firstInput,r=i.firstMultiple,c=r?r.center:a.center,l=e.center=V(n);e.timeStamp=p(),e.deltaTime=e.timeStamp-a.timeStamp,e.angle=$(c,l),e.distance=Z(c,l),function(t,e){var i=e.center,n=t.offsetDelta||{},s=t.prevDelta||{},a=t.prevInput||{};1!==e.eventType&&4!==a.eventType||(s=t.prevDelta={x:a.deltaX||0,y:a.deltaY||0},n=t.offsetDelta={x:i.x,y:i.y});e.deltaX=s.x+(i.x-n.x),e.deltaY=s.y+(i.y-n.y)}(i,e),e.offsetDirection=G(e.deltaX,e.deltaY);var h=j(e.deltaTime,e.deltaX,e.deltaY);e.overallVelocityX=h.x,e.overallVelocityY=h.y,e.overallVelocity=d(h.x)>d(h.y)?h.x:h.y,e.scale=r?(u=r.pointers,v=n,Z(v[0],v[1],q)/Z(u[0],u[1],q)):1,e.rotation=r?function(t,e){return $(e[1],e[0],q)+$(t[1],t[0],q)}(r.pointers,n):0,e.maxPointers=i.prevInput?e.pointers.length>i.prevInput.maxPointers?e.pointers.length:i.prevInput.maxPointers:e.pointers.length,function(t,e){var i,n,s,a,r=t.lastInterval||e,c=e.timeStamp-r.timeStamp;if(8!=e.eventType&&(c>25||r.velocity===o)){var l=e.deltaX-r.deltaX,h=e.deltaY-r.deltaY,u=j(c,l,h);n=u.x,s=u.y,i=d(u.x)>d(u.y)?u.x:u.y,a=G(l,h),t.lastInterval=e}else i=r.velocity,n=r.velocityX,s=r.velocityY,a=r.direction;e.velocity=i,e.velocityX=n,e.velocityY=s,e.direction=a}(i,e);var u,v;var f=t.element;S(e.srcEvent.target,f)&&(f=e.srcEvent.target);e.target=f}(t,i),t.emit("hammer.input",i),t.recognize(i),t.session.prevInput=i}function B(t){for(var e=[],i=0;i<t.pointers.length;)e[i]={clientX:u(t.pointers[i].clientX),clientY:u(t.pointers[i].clientY)},i++;return{timeStamp:p(),pointers:e,center:V(e),deltaX:t.deltaX,deltaY:t.deltaY}}function V(t){var e=t.length;if(1===e)return{x:u(t[0].clientX),y:u(t[0].clientY)};for(var i=0,n=0,s=0;s<e;)i+=t[s].clientX,n+=t[s].clientY,s++;return{x:u(i/e),y:u(n/e)}}function j(t,e,i){return{x:e/t||0,y:i/t||0}}function G(t,e){return t===e?1:d(t)>=d(e)?t<0?2:4:e<0?8:16}function Z(t,e,i){i||(i=H);var n=e[i[0]]-t[i[0]],s=e[i[1]]-t[i[1]];return Math.sqrt(n*n+s*s)}function $(t,e,i){i||(i=H);var n=e[i[0]]-t[i[0]],s=e[i[1]]-t[i[1]];return 180*Math.atan2(s,n)/Math.PI}Y.prototype={handler:function(){},init:function(){this.evEl&&I(this.element,this.evEl,this.domHandler),this.evTarget&&I(this.target,this.evTarget,this.domHandler),this.evWin&&I(M(this.element),this.evWin,this.domHandler)},destroy:function(){this.evEl&&x(this.element,this.evEl,this.domHandler),this.evTarget&&x(this.target,this.evTarget,this.domHandler),this.evWin&&x(M(this.element),this.evWin,this.domHandler)}};var J={mousedown:1,mousemove:2,mouseup:4},K="mousedown",Q="mousemove mouseup";function tt(){this.evEl=K,this.evWin=Q,this.pressed=!1,Y.apply(this,arguments)}y(tt,Y,{handler:function(t){var e=J[t.type];1&e&&0===t.button&&(this.pressed=!0),2&e&&1!==t.which&&(e=4),this.pressed&&(4&e&&(this.pressed=!1),this.callback(this.manager,e,{pointers:[t],changedPointers:[t],pointerType:F,srcEvent:t}))}});var et={pointerdown:1,pointermove:2,pointerup:4,pointercancel:8,pointerout:8},it={2:O,3:"pen",4:F,5:"kinect"},nt="pointerdown",st="pointermove pointerup pointercancel";function at(){this.evEl=nt,this.evWin=st,Y.apply(this,arguments),this.store=this.manager.session.pointerEvents=[]}s.MSPointerEvent&&!s.PointerEvent&&(nt="MSPointerDown",st="MSPointerMove MSPointerUp MSPointerCancel"),y(at,Y,{handler:function(t){var e=this.store,i=!1,n=t.type.toLowerCase().replace("ms",""),s=et[n],a=it[t.pointerType]||t.pointerType,r=a==O,o=L(e,t.pointerId,"pointerId");1&s&&(0===t.button||r)?o<0&&(e.push(t),o=e.length-1):12&s&&(i=!0),o<0||(e[o]=t,this.callback(this.manager,s,{pointers:e,changedPointers:[t],pointerType:a,srcEvent:t}),i&&e.splice(o,1))}});var rt={touchstart:1,touchmove:2,touchend:4,touchcancel:8};function ot(){this.evTarget="touchstart",this.evWin="touchstart touchmove touchend touchcancel",this.started=!1,Y.apply(this,arguments)}function ct(t,e){var i=N(t.touches),n=N(t.changedTouches);return 12&e&&(i=P(i.concat(n),"identifier",!0)),[i,n]}y(ot,Y,{handler:function(t){var e=rt[t.type];if(1===e&&(this.started=!0),this.started){var i=ct.call(this,t,e);12&e&&i[0].length-i[1].length===0&&(this.started=!1),this.callback(this.manager,e,{pointers:i[0],changedPointers:i[1],pointerType:O,srcEvent:t})}}});var lt={touchstart:1,touchmove:2,touchend:4,touchcancel:8},ht="touchstart touchmove touchend touchcancel";function ut(){this.evTarget=ht,this.targetIds={},Y.apply(this,arguments)}function dt(t,e){var i=N(t.touches),n=this.targetIds;if(3&e&&1===i.length)return n[i[0].identifier]=!0,[i,i];var s,a,r=N(t.changedTouches),o=[],c=this.target;if(a=i.filter(function(t){return S(t.target,c)}),1===e)for(s=0;s<a.length;)n[a[s].identifier]=!0,s++;for(s=0;s<r.length;)n[r[s].identifier]&&o.push(r[s]),12&e&&delete n[r[s].identifier],s++;return o.length?[P(a.concat(o),"identifier",!0),o]:void 0}y(ut,Y,{handler:function(t){var e=lt[t.type],i=dt.call(this,t,e);i&&this.callback(this.manager,e,{pointers:i[0],changedPointers:i[1],pointerType:O,srcEvent:t})}});function pt(){Y.apply(this,arguments);var t=w(this.handler,this);this.touch=new ut(this.manager,t),this.mouse=new tt(this.manager,t),this.primaryTouch=null,this.lastTouches=[]}function vt(t,e){1&t?(this.primaryTouch=e.changedPointers[0].identifier,ft.call(this,e)):12&t&&ft.call(this,e)}function ft(t){var e=t.changedPointers[0];if(e.identifier===this.primaryTouch){var i={x:e.clientX,y:e.clientY};this.lastTouches.push(i);var n=this.lastTouches;setTimeout(function(){var t=n.indexOf(i);t>-1&&n.splice(t,1)},2500)}}function mt(t){for(var e=t.srcEvent.clientX,i=t.srcEvent.clientY,n=0;n<this.lastTouches.length;n++){var s=this.lastTouches[n],a=Math.abs(e-s.x),r=Math.abs(i-s.y);if(a<=25&&r<=25)return!0}return!1}y(pt,Y,{handler:function(t,e,i){var n=i.pointerType==O,s=i.pointerType==F;if(!(s&&i.sourceCapabilities&&i.sourceCapabilities.firesTouchEvents)){if(n)vt.call(this,e,i);else if(s&&mt.call(this,i))return;this.callback(t,e,i)}},destroy:function(){this.touch.destroy(),this.mouse.destroy()}});var bt=D(h.style,"touchAction"),Tt=bt!==o,gt="compute",yt="auto",wt="manipulation",Ct="none",Et="pan-x",It="pan-y",xt=function(){if(!Tt)return!1;var t={},e=s.CSS&&s.CSS.supports;return["auto","manipulation","pan-y","pan-x","pan-x pan-y","none"].forEach(function(i){t[i]=!e||s.CSS.supports("touch-action",i)}),t}();function St(t,e){this.manager=t,this.set(e)}St.prototype={set:function(t){t==gt&&(t=this.compute()),Tt&&this.manager.element.style&&xt[t]&&(this.manager.element.style[bt]=t),this.actions=t.toLowerCase().trim()},update:function(){this.set(this.manager.options.touchAction)},compute:function(){var t=[];return m(this.manager.recognizers,function(e){C(e.options.enable,[e])&&(t=t.concat(e.getTouchAction()))}),function(t){if(_(t,Ct))return Ct;var e=_(t,Et),i=_(t,It);if(e&&i)return Ct;if(e||i)return e?Et:It;if(_(t,wt))return wt;return yt}(t.join(" "))},preventDefaults:function(t){var e=t.srcEvent,i=t.offsetDirection;if(this.manager.session.prevented)e.preventDefault();else{var n=this.actions,s=_(n,Ct)&&!xt[Ct],a=_(n,It)&&!xt[It],r=_(n,Et)&&!xt[Et];if(s){var o=1===t.pointers.length,c=t.distance<2,l=t.deltaTime<250;if(o&&c&&l)return}if(!r||!a)return s||a&&6&i||r&&i&z?this.preventSrc(e):void 0}},preventSrc:function(t){this.manager.session.prevented=!0,t.preventDefault()}};var _t=32;function At(t){this.options=c({},this.defaults,t||{}),this.id=W++,this.manager=null,this.options.enable=E(this.options.enable,!0),this.state=1,this.simultaneous={},this.requireFail=[]}function Lt(t){return 16&t?"cancel":8&t?"end":4&t?"move":2&t?"start":""}function Nt(t){return 16==t?"down":8==t?"up":2==t?"left":4==t?"right":""}function Pt(t,e){var i=e.manager;return i?i.get(t):t}function Dt(){At.apply(this,arguments)}function Wt(){Dt.apply(this,arguments),this.pX=null,this.pY=null}function Mt(){Dt.apply(this,arguments)}function kt(){At.apply(this,arguments),this._timer=null,this._input=null}function Rt(){Dt.apply(this,arguments)}function Ut(){Dt.apply(this,arguments)}function Ot(){At.apply(this,arguments),this.pTime=!1,this.pCenter=!1,this._timer=null,this._input=null,this.count=0}function Ft(t,e){return(e=e||{}).recognizers=E(e.recognizers,Ft.defaults.preset),new zt(t,e)}At.prototype={defaults:{},set:function(t){return c(this.options,t),this.manager&&this.manager.touchAction.update(),this},recognizeWith:function(t){if(f(t,"recognizeWith",this))return this;var e=this.simultaneous;return e[(t=Pt(t,this)).id]||(e[t.id]=t,t.recognizeWith(this)),this},dropRecognizeWith:function(t){return f(t,"dropRecognizeWith",this)||(t=Pt(t,this),delete this.simultaneous[t.id]),this},requireFailure:function(t){if(f(t,"requireFailure",this))return this;var e=this.requireFail;return-1===L(e,t=Pt(t,this))&&(e.push(t),t.requireFailure(this)),this},dropRequireFailure:function(t){if(f(t,"dropRequireFailure",this))return this;t=Pt(t,this);var e=L(this.requireFail,t);return e>-1&&this.requireFail.splice(e,1),this},hasRequireFailures:function(){return this.requireFail.length>0},canRecognizeWith:function(t){return!!this.simultaneous[t.id]},emit:function(t){var e=this,i=this.state;function n(i){e.manager.emit(i,t)}i<8&&n(e.options.event+Lt(i)),n(e.options.event),t.additionalEvent&&n(t.additionalEvent),i>=8&&n(e.options.event+Lt(i))},tryEmit:function(t){if(this.canEmit())return this.emit(t);this.state=_t},canEmit:function(){for(var t=0;t<this.requireFail.length;){if(!(33&this.requireFail[t].state))return!1;t++}return!0},recognize:function(t){var e=c({},t);if(!C(this.options.enable,[this,e]))return this.reset(),void(this.state=_t);56&this.state&&(this.state=1),this.state=this.process(e),30&this.state&&this.tryEmit(e)},process:function(t){},getTouchAction:function(){},reset:function(){}},y(Dt,At,{defaults:{pointers:1},attrTest:function(t){var e=this.options.pointers;return 0===e||t.pointers.length===e},process:function(t){var e=this.state,i=t.eventType,n=6&e,s=this.attrTest(t);return n&&(8&i||!s)?16|e:n||s?4&i?8|e:2&e?4|e:2:_t}}),y(Wt,Dt,{defaults:{event:"pan",threshold:10,pointers:1,direction:30},getTouchAction:function(){var t=this.options.direction,e=[];return 6&t&&e.push(It),t&z&&e.push(Et),e},directionTest:function(t){var e=this.options,i=!0,n=t.distance,s=t.direction,a=t.deltaX,r=t.deltaY;return s&e.direction||(6&e.direction?(s=0===a?1:a<0?2:4,i=a!=this.pX,n=Math.abs(t.deltaX)):(s=0===r?1:r<0?8:16,i=r!=this.pY,n=Math.abs(t.deltaY))),t.direction=s,i&&n>e.threshold&&s&e.direction},attrTest:function(t){return Dt.prototype.attrTest.call(this,t)&&(2&this.state||!(2&this.state)&&this.directionTest(t))},emit:function(t){this.pX=t.deltaX,this.pY=t.deltaY;var e=Nt(t.direction);e&&(t.additionalEvent=this.options.event+e),this._super.emit.call(this,t)}}),y(Mt,Dt,{defaults:{event:"pinch",threshold:0,pointers:2},getTouchAction:function(){return[Ct]},attrTest:function(t){return this._super.attrTest.call(this,t)&&(Math.abs(t.scale-1)>this.options.threshold||2&this.state)},emit:function(t){if(1!==t.scale){var e=t.scale<1?"in":"out";t.additionalEvent=this.options.event+e}this._super.emit.call(this,t)}}),y(kt,At,{defaults:{event:"press",pointers:1,time:251,threshold:9},getTouchAction:function(){return[yt]},process:function(t){var e=this.options,i=t.pointers.length===e.pointers,n=t.distance<e.threshold,s=t.deltaTime>e.time;if(this._input=t,!n||!i||12&t.eventType&&!s)this.reset();else if(1&t.eventType)this.reset(),this._timer=v(function(){this.state=8,this.tryEmit()},e.time,this);else if(4&t.eventType)return 8;return _t},reset:function(){clearTimeout(this._timer)},emit:function(t){8===this.state&&(t&&4&t.eventType?this.manager.emit(this.options.event+"up",t):(this._input.timeStamp=p(),this.manager.emit(this.options.event,this._input)))}}),y(Rt,Dt,{defaults:{event:"rotate",threshold:0,pointers:2},getTouchAction:function(){return[Ct]},attrTest:function(t){return this._super.attrTest.call(this,t)&&(Math.abs(t.rotation)>this.options.threshold||2&this.state)}}),y(Ut,Dt,{defaults:{event:"swipe",threshold:10,velocity:.3,direction:30,pointers:1},getTouchAction:function(){return Wt.prototype.getTouchAction.call(this)},attrTest:function(t){var e,i=this.options.direction;return 30&i?e=t.overallVelocity:6&i?e=t.overallVelocityX:i&z&&(e=t.overallVelocityY),this._super.attrTest.call(this,t)&&i&t.offsetDirection&&t.distance>this.options.threshold&&t.maxPointers==this.options.pointers&&d(e)>this.options.velocity&&4&t.eventType},emit:function(t){var e=Nt(t.offsetDirection);e&&this.manager.emit(this.options.event+e,t),this.manager.emit(this.options.event,t)}}),y(Ot,At,{defaults:{event:"tap",pointers:1,taps:1,interval:300,time:250,threshold:9,posThreshold:10},getTouchAction:function(){return[wt]},process:function(t){var e=this.options,i=t.pointers.length===e.pointers,n=t.distance<e.threshold,s=t.deltaTime<e.time;if(this.reset(),1&t.eventType&&0===this.count)return this.failTimeout();if(n&&s&&i){if(4!=t.eventType)return this.failTimeout();var a=!this.pTime||t.timeStamp-this.pTime<e.interval,r=!this.pCenter||Z(this.pCenter,t.center)<e.posThreshold;if(this.pTime=t.timeStamp,this.pCenter=t.center,r&&a?this.count+=1:this.count=1,this._input=t,0===this.count%e.taps)return this.hasRequireFailures()?(this._timer=v(function(){this.state=8,this.tryEmit()},e.interval,this),2):8}return _t},failTimeout:function(){return this._timer=v(function(){this.state=_t},this.options.interval,this),_t},reset:function(){clearTimeout(this._timer)},emit:function(){8==this.state&&(this._input.tapCount=this.count,this.manager.emit(this.options.event,this._input))}}),Ft.VERSION="2.0.7",Ft.defaults={domEvents:!1,touchAction:gt,enable:!0,inputTarget:null,inputClass:null,preset:[[Rt,{enable:!1}],[Mt,{enable:!1},["rotate"]],[Ut,{direction:6}],[Wt,{direction:6},["swipe"]],[Ot],[Ot,{event:"doubletap",taps:2},["tap"]],[kt]],cssProps:{userSelect:"none",touchSelect:"none",touchCallout:"none",contentZooming:"none",userDrag:"none",tapHighlightColor:"rgba(0,0,0,0)"}};function zt(t,e){var i;this.options=c({},Ft.defaults,e||{}),this.options.inputTarget=this.options.inputTarget||t,this.handlers={},this.session={},this.recognizers=[],this.oldCssProps={},this.element=t,this.input=new((i=this).options.inputClass||(R?at:U?ut:k?pt:tt))(i,X),this.touchAction=new St(this,this.options.touchAction),Ht(this,!0),m(this.options.recognizers,function(t){var e=this.add(new t[0](t[1]));t[2]&&e.recognizeWith(t[2]),t[3]&&e.requireFailure(t[3])},this)}function Ht(t,e){var i,n=t.element;n.style&&(m(t.options.cssProps,function(s,a){i=D(n.style,a),e?(t.oldCssProps[i]=n.style[i],n.style[i]=s):n.style[i]=t.oldCssProps[i]||""}),e||(t.oldCssProps={}))}zt.prototype={set:function(t){return c(this.options,t),t.touchAction&&this.touchAction.update(),t.inputTarget&&(this.input.destroy(),this.input.target=t.inputTarget,this.input.init()),this},stop:function(t){this.session.stopped=t?2:1},recognize:function(t){var e=this.session;if(!e.stopped){var i;this.touchAction.preventDefaults(t);var n=this.recognizers,s=e.curRecognizer;(!s||s&&8&s.state)&&(s=e.curRecognizer=null);for(var a=0;a<n.length;)i=n[a],2===e.stopped||s&&i!=s&&!i.canRecognizeWith(s)?i.reset():i.recognize(t),!s&&14&i.state&&(s=e.curRecognizer=i),a++}},get:function(t){if(t instanceof At)return t;for(var e=this.recognizers,i=0;i<e.length;i++)if(e[i].options.event==t)return e[i];return null},add:function(t){if(f(t,"add",this))return this;var e=this.get(t.options.event);return e&&this.remove(e),this.recognizers.push(t),t.manager=this,this.touchAction.update(),t},remove:function(t){if(f(t,"remove",this))return this;if(t=this.get(t)){var e=this.recognizers,i=L(e,t);-1!==i&&(e.splice(i,1),this.touchAction.update())}return this},on:function(t,e){if(t!==o&&e!==o){var i=this.handlers;return m(A(t),function(t){i[t]=i[t]||[],i[t].push(e)}),this}},off:function(t,e){if(t!==o){var i=this.handlers;return m(A(t),function(t){e?i[t]&&i[t].splice(L(i[t],e),1):delete i[t]}),this}},emit:function(t,e){this.options.domEvents&&function(t,e){var i=a.createEvent("Event");i.initEvent(t,!0,!0),i.gesture=e,e.target.dispatchEvent(i)}(t,e);var i=this.handlers[t]&&this.handlers[t].slice();if(i&&i.length){e.type=t,e.preventDefault=function(){e.srcEvent.preventDefault()};for(var n=0;n<i.length;)i[n](e),n++}},destroy:function(){this.element&&Ht(this,!1),this.handlers={},this.session={},this.input.destroy(),this.element=null}},c(Ft,{INPUT_START:1,INPUT_MOVE:2,INPUT_END:4,INPUT_CANCEL:8,STATE_POSSIBLE:1,STATE_BEGAN:2,STATE_CHANGED:4,STATE_ENDED:8,STATE_RECOGNIZED:8,STATE_CANCELLED:16,STATE_FAILED:_t,DIRECTION_NONE:1,DIRECTION_LEFT:2,DIRECTION_RIGHT:4,DIRECTION_UP:8,DIRECTION_DOWN:16,DIRECTION_HORIZONTAL:6,DIRECTION_VERTICAL:z,DIRECTION_ALL:30,Manager:zt,Input:Y,TouchAction:St,TouchInput:ut,MouseInput:tt,PointerEventInput:at,TouchMouseInput:pt,SingleTouchInput:ot,Recognizer:At,AttrRecognizer:Dt,Tap:Ot,Pan:Wt,Swipe:Ut,Pinch:Mt,Rotate:Rt,Press:kt,on:I,off:x,each:m,merge:g,extend:T,assign:c,inherit:y,bindFn:w,prefixed:D}),(void 0!==s?s:"undefined"!=typeof self?self:{}).Hammer=Ft,(n=function(){return Ft}.call(e,i,e,t))===o||(t.exports=n)}(window,document)}},e={};function i(n){var s=e[n];if(void 0!==s)return s.exports;var a=e[n]={exports:{}};return t[n](a,a.exports,i),a.exports}(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){this.setExperienceFragmentToFullHeight(t);const e=this.createSnapScrollParent(t);let i=this.createSnapScrollChildren(e);const n=this.getScrollerNavListElements(t),s=this.createListOfScrollerNavLinkTitles(n);window.addEventListener("resize",()=>{this.createScrollerNav(e,i,t,s)}),this.createScrollerNav(e,i,t,s),this.removeAnyDownChevrons(),this.createChevronsForTabPanels(i,s),esriClientUtils.select(".section-scroller .cmp-tabs__tablist").style.opacity=1}setExperienceFragmentToFullHeight(){let t=esriClientUtils.selectAll(".experiencefragment",e);t.length&&t.forEach(t=>{t.style.height="100vh";let e=esriClientUtils.select(".calcite-container",t),i=esriClientUtils.select(".accordian",t);if(e&&i){let n=i.getBoundingClientRect().height;e.style.height=t.getBoundingClientRect().height-n+"px",e.style.display="flex",e.style.alignItems="center",e.style.justifyContent="center"}})}removeAnyDownChevrons(){esriClientUtils.selectAll("#scroll-down").forEach(t=>{t.parentNode.removeChild(t)})}createChevronsForTabPanels(t,e){let i=this.getNavThemesFromEachChild(t);for(let n=0;n<t.length-2;n++){let s=document.createElement("a");s.classList.add("chevron-down"),s.setAttribute("href","#scroll-id-"+(n+1)),s.setAttribute("data-event","track-component"),s.setAttribute("data-component-name","section-scroll-chevron"),s.setAttribute("data-component-link","Chevron - "+e[n+1]),s.onclick=function(e){e.preventDefault(),e.stopPropagation(),t[n+1].scrollIntoView({behavior:"smooth"})};let a=i[n]||"light-text";s.setAttribute("data-theme",a),t[n].appendChild(s)}}createSnapScrollParent(t){let e=esriClientUtils.select(".cmp-tabs",t),i=esriClientUtils.select("#globalfooter");return i&&e&&e.appendChild(i),e}createSnapScrollChildren(t){let e=this.getChildren(t),i=0,n=esriClientUtils.select("#globalnav");return e.reduce((t,e)=>{if("DIV"===e.tagName&&"cmp-tabs__tablist-wrapper"!=e.className){if("globalfooter"!==e.id){if(e.setAttribute("id","scroll-id-"+i),0===i&&n){let t=n.getBoundingClientRect().height,i=e.getBoundingClientRect().height,s=esriClientUtils.select(".es-nav-wrapper");e.style.height=s?i-t-s.getBoundingClientRect().height+"px":i-t+"px"}i++}t.push(e)}return t},[])}getScrollerNavListElements(t){let e=esriClientUtils.select(".cmp-tabs__tablist",t);return esriClientUtils.selectAll("li",e)}createListOfScrollerNavLinkTitles(t){return t.reduce((t,e)=>(t.push(e.innerText),t),[])}getChildren(t){return Array.from(t.childNodes)}createScrollerNav(t,e,i,n){let s=this.getNavThemesFromEachChild(e);this.setInitialNavTheme(s[0]);let a=esriClientUtils.select(".cmp-tabs__tablist",i);esriClientUtils.selectAll("li",a).forEach((t,i)=>{let s=document.createElement("a");s.setAttribute("href","#"+e[i].id),s.innerText=n[i],t.innerHTML="",t.appendChild(s)});let r=esriClientUtils.selectAll(".cmp-tabs__tab a",t);this.addClickListenersToLinks(r,e);let o=this.getScrollableChildrenRanges(e);this.connectSectionScrollerNavToSnapScrollParentNode(t,o,r,s)}getNavThemesFromEachChild(t){let e=t.reduce((t,e)=>{let i=esriClientUtils.select("[data-theme]",e)||esriClientUtils.select(".light-mode",e)||esriClientUtils.select(".dark-mode",e);return i&&i.attributes["data-theme"]&&"undefined"!==i.attributes["data-theme"].value&&"null"!==i.attributes["data-theme"].value?t.push(i.attributes["data-theme"].value):i&&i.classList.contains("light-mode")?t.push("light-text"):i&&i.classList.contains("dark-mode")?t.push("dark-text"):t.push("light-text"),t},[]);return esriClientUtils.select("#globalfooter")&&(e[e.length-1]="dark-text",e[e.length-2]="dark-text"),e}setInitialNavTheme(t){let e=esriClientUtils.select(".cmp-tabs__tablist");e&&e.setAttribute("data-theme",t||"light-text")}addClickListenersToLinks(t,e){t.forEach((t,i,n)=>{t.onclick=0===i?function(t){t.preventDefault(),t.stopPropagation(),window.scroll({top:0,left:0,behavior:"smooth"})}:function(t){t.preventDefault(),t.stopPropagation(),e[i].scrollIntoView({behavior:"smooth"})}})}getScrollableChildrenRanges(t){let e=0;return t.reduce((t,i,n,s)=>{let a,r,o=i.getBoundingClientRect();return 0==n?(a=0-o.height/2,r=o.height-o.height/2,e+=o.height):(a=e-o.height/2,r=e+o.height/2,e+=o.height),t.push([a,r]),t},[])}connectSectionScrollerNavToSnapScrollParentNode(t,e,i,n){let s=esriClientUtils.select(".cmp-tabs__tablist");window.onscroll=function(t){let a=!1;esriClientUtils.select(".cmp-tabs__tab--active")&&esriClientUtils.select(".cmp-tabs__tab--active").classList.remove("cmp-tabs__tab--active");for(let t=0;t<e.length&&!a;t++){let r=e[t][0],o=e[t][1];window.pageYOffset>=r&&window.pageYOffset<=o&&(i[t].parentNode.classList.add("cmp-tabs__tab--active"),a=!0,s.setAttribute("data-theme",n[t]||"light-text")),window.pageYOffset>e[e.length-2][1]&&(i[i.length-1].parentNode.classList.add("cmp-tabs__tab--active"),a=!0,s.setAttribute("data-theme",n[t]||"light-text"))}}}}const e=esriClientUtils.select(".section-scroller");e&&setTimeout(function(){new t(e)},500)})})(),(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){this.initialize(t)}initialize(t){const e=this.getAllTabs(t),i=this.getAllTabPanels(t);this.moveIcons(e,i)}getAllTabs(t){return t.querySelectorAll(".cmp-tabs__tab")}getAllTabPanels(t){return t.querySelectorAll(".cmp-tabs__tabpanel")}moveIcons(t,e){t&&t.length&&e&&e.length&&Array.prototype.slice.call(e,0).forEach((e,i)=>{this.moveIcon(e,t[i])})}moveIcon(t,e){const i=t.querySelector(".tab--icon");i&&e.prepend(i)}}const e=esriClientUtils.selectAll(".tab-icons");e.length>0&&e.forEach(e=>{new t(e)})})})(),(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){this.coreTab=t,this.localNavDesktopHeight=60,this.localNavMobileHeight=56,this.desktopWidth=1024,this.init()}init(){const t=document.querySelector(".local-navigation"),e=this.coreTab.querySelector(".cmp-tabs__tablist-wrapper");t&&window.innerWidth>=this.desktopWidth&&this.stickyForDesktop(t,e),t&&window.innerWidth<this.desktopWidth&&this.stickyForMobile(t,e),t||this.stickyForNoLocalNav(e)}stickyForDesktop(t,e){const i=t&&t.offsetHeight>0?t.offsetHeight:this.localNavDesktopHeight;e&&(e.style.top="".concat(i,"px"),e.classList.add("sticky-tabs"))}stickyForMobile(t,e){const i=t&&t.offsetHeight>0?t.offsetHeight:this.localNavMobileHeight;e&&(e.style.top="".concat(i,"px"),e.classList.add("sticky-tabs"))}stickyForNoLocalNav(t){t&&(t.style.top="0px",t.classList.add("sticky-tabs"))}}const e=document.querySelectorAll(".esri-tabs");e&&e.forEach(e=>{const i=new IntersectionObserver(n=>{n.forEach(n=>{n.isIntersecting&&(new t(e),i.unobserve(n.target))})});i.observe(e)})})})(),(()=>{"use strict";var t=i(7168);window.coreTabs=function(e,i){const n=this,s=window.innerWidth;n.isContactPage=!(!window.location.href.includes("esri.com/en-us/contact")&&!window.location.href.includes("esri.com/content/esri-sites/language-masters/en/contact")),n.identifier="coreTab".concat(i),n.container=e,n.addPaddleTabStructure=function(){const t=document.createElement("div");t.classList.add("cmp-tabs__tablist-wrapper");const e=esriClientUtils.select(".cmp-tabs",n.container),i=esriClientUtils.select(".cmp-tabs__tablist",n.container),s=esriClientUtils.selectAll(".cmp-tabs__tablist",n.container);if(i){t.appendChild(i),e.insertBefore(t,e.firstChild);const a=esriClientUtils.selectAll(".cmp-tabs__tab",n.container);a.forEach(t=>n.wrapTabInnerHTML(t)),a.forEach(t=>n.ensureIconPlaceholder(t)),a.forEach(t=>n.findEmptyTabIcon(t)),window.innerWidth>768&&s.forEach(t=>n.findMaxHeightParagraphElement(t))}},n.findEmptyTabIcon=function(t){const e=t.querySelector(".esri-image.tab--icon");if(!e||null!==e.innerHTML&&""!==e.innerHTML.trim()){if(e){e.classList.remove("tab--icon-placeholder");const i=t.querySelector("p");i&&i.classList.add("has-icon")}}else e.classList.add("tab--icon-placeholder")},n.findMaxHeightParagraphElement=function(t){const e=t.querySelectorAll("p");let i=0;e.forEach(t=>{const e=t.getBoundingClientRect().height;e>i&&(i=e)}),e.forEach(t=>{t.style.minHeight="".concat(i,"px")})},n.ensureIconPlaceholder=function(t){const e=t.querySelector(".esri-image.tab--icon"),i=e?e.querySelector("svg"):null;e&&!i&&window.innerWidth>767&&(e.style.blockSize="48px")},n.wrapTabInnerHTML=function(t){if(t.querySelector(".esri-image.tab--icon")){const e=t.querySelector(".esri-image.tab--icon"),i=t.textContent.trim(),n=document.createElement("p");n.textContent=i,t.innerHTML="",t.appendChild(e),t.appendChild(n)}else{const e=document.createElement("p");e.textContent=t.textContent.trim(),t.innerHTML="",t.appendChild(e)}},n.calculateTabWidths=function(t){if(window.innerWidth>1526)return null;const e=window.innerWidth;let i=0;for(let t=0;t<n.navTabs.length;t++){const s=n.navTabs[t].getBoundingClientRect();s.left>=100&&s.right<=e&&i++}return 0===i?null:(n.visibleTabs=i,n.visibleTabWidth=t/i,function(){for(let s=0;s<n.navTabs.length;s++){const a=n.navTabs[s].getBoundingClientRect();if(a.left>=100&&a.right>e){i++,n.visibleTabWidth=t/i,n.visibleTabWidth>280&&(n.visibleTabWidth=280),n.visibleTabWidth<72&&(n.visibleTabWidth=72);break}}}(),n.visibleTabWidth)},n.addPaddleTabStructure(),n.navContainer=n.container.querySelector(".cmp-tabs__tablist-wrapper"),n.nav=n.navContainer.querySelector(".cmp-tabs__tablist"),n.navTabs=n.navContainer.getElementsByClassName("cmp-tabs__tab"),n.tabSections=n.container.getElementsByClassName("cmp-tabs__tabpanel"),n.numTabs=Number(n.navTabs.length),n.navContainerWidth=Number.parseInt(n.navContainer.offsetWidth,10),n.navWidth=Number.parseInt(n.nav.offsetWidth,10),n.containerDiff=n.navContainerWidth-n.navWidth,n.cardWidth=n.calculateTabWidths(n.navWidth)||n.navTabs[0].offsetWidth,n.travelDistance=2*n.cardWidth,n.isLeftMost=!0,n.isRightMost=!1,n.swipeLock=n.cardWidth*n.numTabs-n.cardWidth,n.isContactPage||(n.mapTabIdToTabText=function(){const t={};for(let e=0;e<n.navTabs.length;e++){const i=n.navTabs[e].innerText.trim().replace(/\s+/g,"-").toLowerCase();t[n.navTabs[e].id]=i}return t},n.tabIdsToTabText=n.mapTabIdToTabText(),n.mapTabTextToTabId=function(){const t={};for(const e in n.tabIdsToTabText)t[n.tabIdsToTabText[e]]=e;return t},n.tabTextToTabIds=n.mapTabTextToTabId()),n.setActiveTabIndex=function(){const t=decodeURI(window.location.hash).slice(1).replace(/-/g," ").toLowerCase();if(window.location.hash)if(n.isContactPage){if(document.getElementById(window.location.hash)){let t=0;for(;t<n.navTabs.length;){if(n.navTabs[t].id===window.location.hash.slice(1))return t;t++}}}else if(n.tabTextToTabIds[decodeURI(window.location.hash.slice(1))]){let e=0;for(;e<n.navTabs.length;){if(n.navTabs[e].innerText.trim().replace(/-/g," ").toLowerCase()===t)return e;e++}}if(n.navTabs&&n.navTabs.length)for(let t=0;t<n.navTabs.length;t++)if(n.navTabs[t].classList.contains("cmp-tabs__tab--active"))return t;return 0},n.activeTabIndex=n.setActiveTabIndex(),n.nav.scrollLeft=n.cardWidth*n.activeTabIndex,n.debounce=function(t,e,i){let n;return function(){let s=this,a=arguments;const r=i&&!n;clearTimeout(n),n=setTimeout(function(){n=null,i||t.apply(s,a)},e),r&&t.apply(s,a)}},n.Resize=function(){n.Draw(!0)},n.Draw=function(t){let e=!1;if(window.innerWidth>=1527&&(e=!0),e?n.navTabs.length>=6?n.nav.classList.add("tab-nav--fit-6-tabs"):n.navTabs.length>=5&&n.nav.classList.add("tab-nav--fit-5-tabs"):(n.nav.classList.remove("tab-nav--fit-6-tabs"),n.nav.classList.remove("tab-nav--fit-5-tabs")),window.innerWidth<=480)t||n.MakeVisibleActive(),n.travelDistance=n.cardWidth;else if(window.innerWidth>767&&window.innerWidth<=1526){n.travelDistance=n.cardWidth;for(let t=0;t<n.navTabs.length;t++)n.navTabs[t].style.setProperty("width","".concat(n.travelDistance,"px"))}else n.nav.removeAttribute("style"),n.travelDistance=2*n.cardWidth;n.activeTabIndex<=0?n.isLeftMost=!0:n.isLeftMost=!1,n.activeTabIndex>=n.numTabs-1||n.nav.offsetWidth+n.nav.scrollLeft>=n.nav.scrollWidth?n.isRightMost=!0:n.isRightMost=!1},n.MakeVisibleActive=function(){setTimeout(()=>{n.ActivateTab(n.activeTabIndex)},350)},n.ActivateTab=function(t){let e=arguments.length>1&&void 0!==arguments[1]&&arguments[1];const i=n.navContainer.contains(document.activeElement),s=e||i;[].forEach.call(n.navTabs,t=>{t.classList.remove("cmp-tabs__tab--active"),s&&t.blur()}),[].forEach.call(n.tabSections,t=>{t.classList.remove("cmp-tabs__tabpanel--active")}),n.navTabs[t]&&(n.navTabs[t].classList.add("cmp-tabs__tab--active"),s&&n.navTabs[t].focus()),n.tabSections[t].classList.contains("cmp-tabs__tabpanel--active")||n.tabSections[t].classList.add("cmp-tabs__tabpanel--active"),n.activeTabIndex=t;const a=n.navTabs[t],r=a.offsetLeft,o=Math.max(0,r+a.offsetWidth/2-n.nav.clientWidth/2),c=Math.min(o,n.nav.scrollWidth-n.nav.clientWidth);n.nav.scrollTo({left:c,behavior:"smooth"})},n.BindTabs=function(){[].forEach.call(n.navTabs,(t,e)=>{t.addEventListener("click",()=>{n.ActivateTab(e,!0),n.isContactPage?(history.replaceState(null,null,"#".concat(n.navTabs[e].id)),window.location.hash=n.navTabs[e].id):n.navTabs[e]&&(history.replaceState(null,null,"#".concat(n.tabIdsToTabText[n.navTabs[e].id])),window.location.hash=n.tabIdsToTabText[n.navTabs[e].id])})})},n.MoveLeft=function(){n.activeTabIndex>0&&(n.isRightMost=!1,n.activeTabIndex--,n.ActivateTab(n.activeTabIndex,!0),n.isContactPage?(history.replaceState(null,null,"#".concat(n.navTabs[n.activeTabIndex].id)),window.location.hash=n.navTabs[n.activeTabIndex].id):(history.replaceState(null,null,"#".concat(n.tabIdsToTabText[n.navTabs[n.activeTabIndex].id])),window.location.hash=n.tabIdsToTabText[n.navTabs[n.activeTabIndex].id]),setTimeout(()=>{n.Draw(!0)},350))},n.MoveRight=function(){n.activeTabIndex<n.numTabs-1&&(n.isLeftMost=!1,n.activeTabIndex++,n.ActivateTab(n.activeTabIndex,!0),n.isContactPage?(history.replaceState(null,null,"#".concat(n.navTabs[n.activeTabIndex].id)),window.location.hash=n.navTabs[n.activeTabIndex].id):(history.replaceState(null,null,"#".concat(n.tabIdsToTabText[n.navTabs[n.activeTabIndex].id])),window.location.hash=n.tabIdsToTabText[n.navTabs[n.activeTabIndex].id]))},n.lastSwipeTime=0,n.handleSwipeNavigation=function(t){const e=Date.now();e-n.lastSwipeTime<250||(n.lastSwipeTime=e,"left"===t?n.MoveRight("swipeleft"):"right"===t&&n.MoveLeft("swiperight"))};const a=new t(n.navContainer);a.on("swipeleft",()=>n.handleSwipeNavigation("left")),a.on("swiperight",()=>n.handleSwipeNavigation("right")),Array.from(n.tabSections).forEach(e=>{const i=new t(e,{cssProps:{}});i.on("swipeleft",()=>n.handleSwipeNavigation("left")),i.on("swiperight",()=>n.handleSwipeNavigation("right"))}),n.Draw(),window.addEventListener("resize",()=>{window.innerWidth!==s&&(n.Draw(!0),n.debounce(n.Resize,100))}),n.BindTabs(),n.isContactPage||n.tabTextToTabIds[decodeURI(window.location.hash.slice(1))]!==n.navTabs[n.activeTabIndex].id||setTimeout(()=>{n.navTabs[n.activeTabIndex]&&(document.getElementById(n.navTabs[n.activeTabIndex].id).click(),document.getElementById(n.navTabs[n.activeTabIndex].id).scrollIntoView({behavior:"smooth",block:"center",inline:"center"}))},500)},document.addEventListener("DOMContentLoaded",()=>{[].slice.call(document.querySelectorAll(".esri-tabs")).forEach((t,e)=>{!t.classList.contains("section-scroller")&&esriClientUtils.select(".cmp-tabs__tablist",t)&&new coreTabs(t,e+1)})})})()})();
(()=>{"use strict";window.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){this.config={localNavSelector:".local-navigation",defaultOffset:10};const e=esriClientUtils.select("ul",t);e&&(this.setDynamicTopPosition(e),this.findColumnSys(t),this.setClasses(e))}setTocHeight(t,e,n){e.style.height=t?n.clientHeight+"px":"auto"}setDynamicTopPosition(t){if(!t)return;document.querySelector(this.config.localNavSelector)||t.style.setProperty("--toc-top-offset","".concat(this.config.defaultOffset,"px"))}findColumnSys(t){const e=t.parentNode.closest(".columnsystem");if(!e)return;const n=e.nextElementSibling;if(!n)return;const s=n.querySelector('[class^="column-"]');if(!s)return;const i=globalThis.matchMedia("(min-width: 860px)");this.setTocHeight(i.matches,t,s),i.addEventListener("change",e=>{this.setTocHeight(e.matches,t,s)})}toggleChevron(t,e){if(null!==t){let n=t.parentElement;if(null!==n){let t=n.parentElement.querySelector("ul");null!==t&&(e=t)}const s=t.classList.contains("expand-icon");t.classList.toggle("expand-icon",!s),t.classList.toggle("collapse-icon",s),e.classList.toggle("hidden",!s),e.classList.toggle("not-hidden",s),e.setAttribute("aria-hidden",s?"false":"true")}}toggleBackgroundHighlight(t,e){const n=t.hasAttribute("icon")?t.parentElement:t;if(e&&e.length>0)for(const t of e){const e=t.querySelectorAll(".active");for(const t of e)t.classList.remove("active")}(n.classList.contains("parent-anchor")||n.classList.contains("child-anchor"))&&n.classList.add("active")}setClasses(t){const e=[...t.children];if(t.classList.add("cmp-toc__navigation"),e.length>0)for(const t of e)this.setupParentItem(t,e)}setupParentItem(t,e){const n=t.querySelector("ul"),s=t.querySelector("a");s.classList.add("parent-anchor"),t.classList.add("parent-list"),s.addEventListener("click",t=>{this.toggleBackgroundHighlight(t.target,e)}),null!==n?this.setupChildContainer(t,n,e):t.classList.add("no-child")}setupChildContainer(t,e,n){const s=this.createChevronIcon();e.classList.add("child-wrapper","hidden"),e.setAttribute("aria-hidden","true"),this.attachParentClickHandlers(t,e,n),this.attachChevronClickHandler(s,e,n),t.firstChild.prepend(s);const i=e.querySelectorAll("li");i.length>0&&this.setupGrandchildren(i)}createChevronIcon(){const t=document.createElement("calcite-icon");return t.classList.add("calcite-icon","expand-icon"),t.setAttribute("icon","chevronDown"),t.setAttribute("scale","s"),t}attachParentClickHandlers(t,e,n){t.addEventListener("click",t=>{const s=t.target,i=s.parentElement&&!s.parentElement.classList.contains("no-grand-child");(s.classList.contains("parent-anchor")||i)&&t.preventDefault(),this.toggleBackgroundHighlight(s,n),this.toggleChevron(t.target.querySelector("calcite-icon"),e)})}attachChevronClickHandler(t,e,n){t.addEventListener("click",t=>{const s=t.target.parentElement;(s.classList.contains("parent-anchor")||s.classList.contains("child-anchor"))&&t.preventDefault(),this.toggleBackgroundHighlight(s,n),this.toggleChevron(t.target,e)})}setupGrandchildren(t){for(const e of t){e.querySelector("a").classList.add("child-anchor");const t=e.querySelector("ul");e.classList.add("child-list"),null!==t?this.setupGrandchildContainer(e,t):e.classList.add("no-grand-child")}}setupGrandchildContainer(t,e){e.classList.add("grand-wrapper","hidden"),e.setAttribute("aria-hidden","true");const n=this.createChevronIcon();n.addEventListener("click",t=>{this.toggleChevron(t.target,e)}),t.firstChild.prepend(n)}}let e=document.querySelector(".cmp-toc__content");null!==e&&new t(e)})})();
(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t,e,s){this.comparisonTable=t,this.tabListWrapperHeight=e,this.padding=s,this.init(t)}init(t){const e=document.querySelector(".local-navigation"),s=this.comparisonTable.querySelector(".esri-table.table--comparison table tr:first-child"),o=s.offsetHeight;this.tabListWrapperHeight?this.adjustComparisonTableHeaderRowPositionDueToCoreTabs(e,s,o):(e&&window.innerWidth>=1024&&this.stickyForDesktop(e,s,o,t),e&&window.innerWidth<1024&&this.stickyForMobile(e,s,o,t),e||this.stickyForNoLocalNav(s,o))}stickyForDesktop(t,e,s,o){const i=t&&t.offsetHeight>0?t.offsetHeight:60;o.querySelector("tr").style.top="".concat(i,"px"),e&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(s+i,"px")})}stickyForMobile(t,e,s,o){const i=t&&t.offsetHeight>0?t.offsetHeight:56;o.querySelector("tr").style.top="".concat(i,"px"),e&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(s+i,"px")})}stickyForNoLocalNav(t,e){t&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(e,"px")})}adjustComparisonTableHeaderRowPositionDueToCoreTabs(t,e,s){const o=this.comparisonTable.querySelector("tr");t&&window.innerWidth>=1024&&this.stickyInsideCoreTabsDesktop(t,o,e,s),t&&window.innerWidth<1024&&this.stickyInsideCoreTabsMobile(t,o,e,s),t||this.stickyInsideCoreTabsNoLocalNav(o,e,s)}stickyInsideCoreTabsDesktop(t,e,s,o){const i=t.offsetHeight>0?t.offsetHeight:60;e.style.top="".concat(i+this.tabListWrapperHeight,"px"),s&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(o+this.tabListWrapperHeight+this.padding,"px")})}stickyInsideCoreTabsMobile(t,e,s,o){const i=t.offsetHeight>0?t.offsetHeight:56;e.style.top="".concat(i+this.tabListWrapperHeight,"px"),s&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(o+this.tabListWrapperHeight+this.padding,"px")})}stickyInsideCoreTabsNoLocalNav(t,e,s){t.style.top="".concat(this.tabListWrapperHeight,"px"),e&&this.comparisonTable.querySelectorAll(".esri-table.table--comparison table tr:has(th:first-child:last-child)").forEach(t=>{t.style.top="".concat(s+this.tabListWrapperHeight+this.padding,"px")})}}const e=document.querySelectorAll(".esri-table.table--comparison");e&&e.forEach(e=>{const s=e.closest(".esri-tabs");let o=0,i=0;if(s){const t=s.querySelector(".cmp-tabs__tablist-wrapper");o=t.offsetHeight;const a=e.closest('[class*="padding-leader-"]');i=Number.parseInt(window.getComputedStyle(a).getPropertyValue("padding-block-start"),10)}const a=new IntersectionObserver(s=>{s.forEach(s=>{s.isIntersecting&&(new t(e,o,i),a.unobserve(s.target))})});a.observe(e)})})})();
(()=>{"use strict";class e{constructor(){this.imgSVGz=[].slice.call(document.querySelectorAll("img.svg")),this.convertToSVG(this.imgSVGz)}convertToSVG(e,t){e.forEach(e=>{let t=e.getAttribute("src"),i=e.getAttribute("alt"),s=e.getAttribute("data-theme");t.indexOf(".svg")>-1&&function(e,t){let i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:()=>{};const s=new XMLHttpRequest;s.addEventListener("readystatechange",()=>{4===s.readyState&&(200===s.status?t(s.responseText):i())}),s.open("GET",e),s.send()}("".concat(t),t=>{t=(t=t.replace(/id=\"icon-ui-svg\"/g,'id="icon-ui-svg" class="icon-ui-svg"')).replace(/id=\"icons-ui-svg\"/g,'id="icon-ui-svg" class="icon-ui-svg"');let r=(new DOMParser).parseFromString(t,"text/xml"),n=r.querySelector("svg");if(i&&null!==i){const e=document.createElement("title"),t=r.querySelector("svg").firstElementChild;""!==t.innerHTML?(t.innerHTML="".concat(i),t.setAttribute("role","image")):(e.setAttribute("stroke","none"),e.setAttribute("stroke-width","1px"),e.innerHTML="".concat(i),e.setAttribute("role","image"),n.insertBefore(e,t))}else n.setAttribute("aria-hidden","true");if(s&&null!==s){const e=r.querySelector("svg");e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base")&&null!=e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base")&&(e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base").style.fill=s),e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary")&&null!=e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary")&&(e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary").style.fill=s)}null!==e.parentNode&&e.parentNode.replaceChild(r.querySelector("svg"),e)})})}}document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){let i=t.querySelector(".cmp-image__image");if(null!==i){let s=t.querySelector(".cmp-image").getAttribute("data-asset");if(null!==s){if(".svg"===s.slice(-4)){let t=new e;i.setAttribute("src",s),t.convertToSVG([i])}}}}}setTimeout(function(){const e=esriClientUtils.selectAll(".esri-image");e.length>0&&e.forEach(e=>{new t(e)})},500)})})();
(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{function e(e){e.topLimitElmt>e.viewportMiddlePos&&e.btmLeftY>e.viewportHt&&(e.ltPaddle.removeAttribute("style"),e.rtPaddle.removeAttribute("style"),e.ltPaddle.style.top="".concat(e.topThirdPos,"px"),e.rtPaddle.style.top="".concat(e.topThirdPos,"px")),e.topLimitElmt<e.viewportMiddlePos&&e.btmLimitElmt>e.viewportMiddlePos&&(e.ltPaddle.style.position="fixed",e.rtPaddle.style.position="fixed",e.ltPaddle.style.top="".concat(e.viewportMiddlePos,"px"),e.rtPaddle.style.top="".concat(e.viewportMiddlePos,"px")),e.btmLimitElmt<e.viewportMiddlePos&&e.topLeftY<0&&(e.ltPaddle.removeAttribute("style"),e.rtPaddle.removeAttribute("style"),e.ltPaddle.style.top="".concat(e.btmThirdPos,"px"),e.rtPaddle.style.top="".concat(e.btmThirdPos,"px"))}function t(e){return{ltPaddle:e.querySelector(".cmp-carousel__action--previous"),rtPaddle:e.querySelector(".cmp-carousel__action--next"),isMobile:window.matchMedia("(max-width: 1024px)"),viewportHt:window.innerHeight,viewportMiddlePos:window.innerHeight/2,topLeftY:e.getBoundingClientRect().top,btmLeftY:e.getBoundingClientRect().bottom,carouselTallerThanViewport:e.getBoundingClientRect().bottom-e.getBoundingClientRect().top>window.innerHeight,topThirdPos:.25*(e.getBoundingClientRect().bottom-e.getBoundingClientRect().top),btmThirdPos:.75*(e.getBoundingClientRect().bottom-e.getBoundingClientRect().top),posYPadl:e.querySelector(".cmp-carousel__action--previous").getBoundingClientRect().top,topLimitElmt:e.querySelector(".topLimitElmt").getBoundingClientRect().top,btmLimitElmt:e.querySelector(".btmLimitElmt").getBoundingClientRect().top}}function i(i){!function(e){const t=.25*e.getBoundingClientRect().height,i=.75*e.getBoundingClientRect().height,n=document.createElement("div");n.classList.add("btmLimitElmt"),n.setAttribute("style","position: relative; top: ".concat(i,"px")),e.insertBefore(n,e.firstChild);const o=document.createElement("div");o.classList.add("topLimitElmt"),o.setAttribute("style","position: relative; top: ".concat(t,"px")),e.insertBefore(o,e.firstChild)}(i);let n=t(i);n.carouselTallerThanViewport&&(e(n),window.addEventListener("scroll",()=>{esriClientUtils.throttle(function(i){e(t(i))}(i))}))}let n=document.querySelectorAll(".esri-carousel");n.length>0&&n.forEach(e=>{var t;!function(e){let t=e.querySelector(".cmp-carousel__action--next"),i=e.querySelector(".cmp-carousel__action--previous"),n=t.querySelector(".cmp-carousel__action-icon"),o=t.querySelector(".cmp-carousel__action-text"),c=i.querySelector(".cmp-carousel__action-icon"),r=i.querySelector(".cmp-carousel__action-text");t.removeChild(n),t.removeChild(o),i.removeChild(c),i.removeChild(r),t.parentElement;let a=document.createElement("calcite-icon");a.classList.add("cpm-carousel__chevronIcon-left"),t.setAttribute("appearance","solid"),t.setAttribute("scale","m"),t.setAttribute("alignment","center"),t.setAttribute("width","auto"),a.setAttribute("icon","chevronLeft"),i.appendChild(a);let l=document.createElement("calcite-icon");l.classList.add("cpm-carousel__chevronIcon-right"),i.setAttribute("appearance","solid"),i.setAttribute("scale","m"),i.setAttribute("alignment","center"),i.setAttribute("width","auto"),l.setAttribute("icon","chevronRight"),t.appendChild(l)}(e),function(e){let t=!!e.classList.contains("top-switcher"),i=!!e.classList.contains("bottom-switcher");if(t||i){let t=e.querySelector(".cmp-carousel").querySelector(".cmp-carousel__content").querySelector(".cmp-carousel__indicators").querySelectorAll(".cmp-carousel__indicator");t.length>0&&t.forEach(e=>{let t=document.createElement("button");t.classList.add("indicator-title"),t.setAttribute("aria-label",e.innerHTML),t.setAttribute("name",e.innerHTML),t.innerHTML=e.innerHTML,e.innerHTML="",e.appendChild(t)})}}(e),function(e){let t=!!e.classList.contains("top-switcher"),i=!!e.classList.contains("bottom-gradient");t&&i&&(e.classList.remove("bottom-gradient"),e.classList.add("top-gradient"))}(e),!(t=e).classList.contains("esri-carousel")||t.classList.contains("calcite-theme-dark")||t.classList.contains("calcite-theme-light")||t.classList.add("calcite-theme-light"),i(e),window.addEventListener("resize",function(){i(e)})})}),document.addEventListener("DOMContentLoaded",()=>{const e=document.querySelectorAll(".esri-carousel.layered-carousel"),t=window.location.hash.slice(1),i=t?document.getElementById(t):null;e.forEach(e=>{const t=e.querySelector(".cmp-carousel");if(!t)return;const n=t.querySelector(".cmp-carousel__action--next");if(!n)return;const o=t.querySelectorAll(".cmp-carousel__item"),c=!(!i||!t.contains(i));o.length>2&&!c&&n.click()}),t?requestAnimationFrame(()=>{window.location.hash="",window.location.hash=t}):(history.replaceState(null,"",window.location.pathname+window.location.search),requestAnimationFrame(()=>window.scrollTo(0,0))),e.forEach(e=>{const t=e.querySelector(".cmp-carousel");if(!t)return;const i=t.querySelector(".cmp-carousel__action--previous"),n=t.querySelector(".cmp-carousel__action--next");if(!i||!n)return;const o=window.matchMedia("(min-width: 600px) and (orientation: landscape)"),c=t.querySelector(".cmp-carousel__actions"),r=document.createElement("div");r.classList.add("cmp-carousel__action-label","cmp-carousel__action-label--next"),r.setAttribute("aria-hidden","true"),c.appendChild(r);const a=document.createElement("div");a.classList.add("cmp-carousel__action-label","cmp-carousel__action-label--previous"),a.setAttribute("aria-hidden","true"),c.appendChild(a);const l=document.createElement("div");function s(){var e,i;const n=t.querySelector(".cmp-carousel__item--active");if(!n)return;const o=null===(e=n.previousElementSibling)||void 0===e?void 0:e.querySelector(".cmp-teaser__title"),c=null===(i=n.nextElementSibling)||void 0===i?void 0:i.querySelector(".cmp-teaser__title");if(a.textContent="",o){const e=document.createElement("span");e.textContent=o.textContent.trim(),a.appendChild(e)}if(r.textContent="",c){const e=document.createElement("span");e.textContent=c.textContent.trim(),r.appendChild(e)}const s=t.querySelectorAll(".cmp-carousel__item"),d=Array.from(s).indexOf(n)+1,u=n.querySelector(".cmp-teaser__title"),m=u?": ".concat(u.textContent.trim()):"";l.textContent="Slide ".concat(d," of ").concat(s.length).concat(m)}function d(){const e=t.querySelectorAll(".cmp-carousel__item"),o=t.querySelector(".cmp-carousel__item--active");if(!o||0===e.length)return;const c=o===e[0],r=o===e[e.length-1];i.setAttribute("aria-disabled",String(c)),i.tabIndex=c?-1:0,n.setAttribute("aria-disabled",String(r)),n.tabIndex=r?-1:0}function u(){t.querySelectorAll(".cmp-carousel__item").forEach(e=>{const t=e.classList.contains("cmp-carousel__item--active");e.querySelectorAll("a, button, input, select, textarea, [tabindex]").forEach(e=>{var i;if(e.hasAttribute("data-orig-tabindex")||e.setAttribute("data-orig-tabindex",null!==(i=e.getAttribute("tabindex"))&&void 0!==i?i:""),t){const t=e.getAttribute("data-orig-tabindex");""===t?e.removeAttribute("tabindex"):e.setAttribute("tabindex",t)}else e.setAttribute("tabindex","-1")})})}function m(){if(o.matches)return;const e=t.querySelector(".cmp-carousel__item--active"),i=null==e?void 0:e.querySelector(".cmp-teaser__image");if(!i)return;const n=t.getBoundingClientRect(),c=i.getBoundingClientRect();c.height>0&&t.style.setProperty("--carousel-image-height","".concat(c.height,"px"));const r=c.left-n.left,a=n.right-c.right;if(r>=0&&a>=0){const e=parseFloat(getComputedStyle(t).getPropertyValue("--space-4"))||16;t.style.setProperty("--carousel-image-inset-start","".concat(r+e,"px")),t.style.setProperty("--carousel-image-inset-end","".concat(a+e,"px"))}}function p(){t.classList.add("fade-only"),requestAnimationFrame(()=>{s(),d(),u(),m(),t.classList.remove("fade-only")})}function h(e){o.matches||(t.classList.remove("slide-next","slide-prev"),t.offsetWidth,t.classList.add(e),t.addEventListener("animationend",()=>{t.classList.remove("slide-next","slide-prev")},{once:!0})),requestAnimationFrame(()=>{const e=t.querySelector(".cmp-carousel__item--active");if(!e)return;const i=e.querySelector(".cmp-teaser__content");i&&(i.classList.remove("calcite-animate","calcite-animate__in-up"),i.offsetWidth,i.classList.add("calcite-animate","calcite-animate__in-up"))})}l.setAttribute("aria-live","polite"),l.setAttribute("aria-atomic","true"),l.classList.add("cmp-carousel__live-region"),t.appendChild(l),s(),d(),u(),m(),new ResizeObserver(m).observe(t);let v=0,g=0;function _(e,i){t.classList.toggle(e,i)}t.addEventListener("touchstart",e=>{v=e.changedTouches[0].clientX,g=e.changedTouches[0].clientY},{passive:!0}),t.addEventListener("touchend",e=>{const t=e.changedTouches[0].clientX,o=e.changedTouches[0].clientY,c=v-t,r=g-o;Math.abs(c)<50||Math.abs(r)>Math.abs(c)*Math.tan(30*Math.PI/180)||(c>0&&"true"!==n.getAttribute("aria-disabled")?(h("slide-next"),n.click()):c<0&&"true"!==i.getAttribute("aria-disabled")&&(h("slide-prev"),i.click()))}),i.addEventListener("click",()=>{h("slide-prev"),p()}),n.addEventListener("click",()=>{h("slide-next"),p()}),t.querySelectorAll(".cmp-carousel__indicator").forEach(e=>{e.addEventListener("click",p)}),r.addEventListener("click",()=>n.click()),a.addEventListener("click",()=>i.click()),n.addEventListener("pointerenter",()=>_("next-hover",!0)),n.addEventListener("pointerleave",e=>{r.contains(e.relatedTarget)||_("next-hover",!1)}),r.addEventListener("pointerenter",()=>{t.classList.contains("next-hover")&&_("next-hover",!0)}),r.addEventListener("pointerleave",e=>{n.contains(e.relatedTarget)||_("next-hover",!1)}),i.addEventListener("pointerenter",()=>_("prev-hover",!0)),i.addEventListener("pointerleave",e=>{a.contains(e.relatedTarget)||_("prev-hover",!1)}),a.addEventListener("pointerenter",()=>{t.classList.contains("prev-hover")&&_("prev-hover",!0)}),a.addEventListener("pointerleave",e=>{i.contains(e.relatedTarget)||_("prev-hover",!1)})})})})();
(()=>{"use strict";document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t,e){const c=e+1,n=window.location.hash.slice(1),o=this.generatePanelIdToIdMap(t,"accordion",c);this.panelClick(t),o[n]&&setTimeout(()=>{document.getElementById("".concat(o[n],"-button")).click()},500),this.replaceAccordionAnchorLinks(o)}replaceAccordionAnchorLinks(t){for(const e in t){document.querySelectorAll('[href="#'.concat(e,'"]')).forEach(c=>{c.setAttribute("href","#".concat(t[e])),c.addEventListener("click",()=>{setTimeout(()=>{history.replaceState(null,null,"#".concat(e)),window.location.hash=e},100)})})}}getPanels(t){return[...t.querySelectorAll(".cmp-accordion__item")]}generatePanelIdToIdMap(t,e,c){return this.getPanels(t).reduce((t,n,o)=>{const a=o+1,r="".concat(e,"-").concat(c,"-").concat(a);n.setAttribute("attr-panel-id",r);const l=n.getAttribute("id");return t[r]=l,t},{})}panelClick(t){this.getPanels(t).forEach(t=>{t.addEventListener("click",()=>{const e=t.getAttribute("attr-panel-id");null!==t.getAttribute("data-cmp-expanded")?(history.replaceState(null,null,"#".concat(e)),window.location.hash=e):history.replaceState(null,null,"")})})}}const e=document.querySelectorAll(".esri-accordion");e&&e.forEach((e,c)=>{new t(e,c)})})})();
(()=>{var t={7168(t,e,i){var n;!function(s,r,a,o){"use strict";var h,l=["","webkit","Moz","MS","ms","o"],c=r.createElement("div"),u=Math.round,p=Math.abs,d=Date.now;function f(t,e,i){return setTimeout(E(t,i),e)}function v(t,e,i){return!!Array.isArray(t)&&(m(t,i[e],i),!0)}function m(t,e,i){var n;if(t)if(t.forEach)t.forEach(e,i);else if(t.length!==o)for(n=0;n<t.length;)e.call(i,t[n],n,t),n++;else for(n in t)t.hasOwnProperty(n)&&e.call(i,t[n],n,t)}function g(t,e,i){var n="DEPRECATED METHOD: "+e+"\n"+i+" AT \n";return function(){var e=new Error("get-stack-trace"),i=e&&e.stack?e.stack.replace(/^[^\(]+?[\n$]/gm,"").replace(/^\s+at\s+/gm,"").replace(/^Object.<anonymous>\s*\(/gm,"{anonymous}()@"):"Unknown Stack Trace",r=s.console&&(s.console.warn||s.console.log);return r&&r.call(s.console,n,i),t.apply(this,arguments)}}h="function"!=typeof Object.assign?function(t){if(t===o||null===t)throw new TypeError("Cannot convert undefined or null to object");for(var e=Object(t),i=1;i<arguments.length;i++){var n=arguments[i];if(n!==o&&null!==n)for(var s in n)n.hasOwnProperty(s)&&(e[s]=n[s])}return e}:Object.assign;var T=g(function(t,e,i){for(var n=Object.keys(e),s=0;s<n.length;)(!i||i&&t[n[s]]===o)&&(t[n[s]]=e[n[s]]),s++;return t},"extend","Use `assign`."),y=g(function(t,e){return T(t,e,!0)},"merge","Use `assign`.");function b(t,e,i){var n,s=e.prototype;(n=t.prototype=Object.create(s)).constructor=t,n._super=s,i&&h(n,i)}function E(t,e){return function(){return t.apply(e,arguments)}}function C(t,e){return"function"==typeof t?t.apply(e&&e[0]||o,e):t}function A(t,e){return t===o?e:t}function I(t,e,i){m(_(e),function(e){t.addEventListener(e,i,!1)})}function w(t,e,i){m(_(e),function(e){t.removeEventListener(e,i,!1)})}function W(t,e){for(;t;){if(t==e)return!0;t=t.parentNode}return!1}function L(t,e){return t.indexOf(e)>-1}function _(t){return t.trim().split(/\s+/g)}function x(t,e,i){if(t.indexOf&&!i)return t.indexOf(e);for(var n=0;n<t.length;){if(i&&t[n][i]==e||!i&&t[n]===e)return n;n++}return-1}function D(t){return Array.prototype.slice.call(t,0)}function R(t,e,i){for(var n=[],s=[],r=0;r<t.length;){var a=e?t[r][e]:t[r];x(s,a)<0&&n.push(t[r]),s[r]=a,r++}return i&&(n=e?n.sort(function(t,i){return t[e]>i[e]}):n.sort()),n}function P(t,e){for(var i,n,s=e[0].toUpperCase()+e.slice(1),r=0;r<l.length;){if((n=(i=l[r])?i+s:e)in t)return n;r++}return o}var M=1;function S(t){var e=t.ownerDocument||t;return e.defaultView||e.parentWindow||s}var N="ontouchstart"in s,z=P(s,"PointerEvent")!==o,k=N&&/mobile|tablet|ip(ad|hone|od)|android/i.test(navigator.userAgent),O="touch",H="mouse",U=24,Y=["x","y"],X=["clientX","clientY"];function F(t,e){var i=this;this.manager=t,this.callback=e,this.element=t.element,this.target=t.options.inputTarget,this.domHandler=function(e){C(t.options.enable,[t])&&i.handler(e)},this.init()}function q(t,e,i){var n=i.pointers.length,s=i.changedPointers.length,r=1&e&&n-s===0,a=12&e&&n-s===0;i.isFirst=!!r,i.isFinal=!!a,r&&(t.session={}),i.eventType=e,function(t,e){var i=t.session,n=e.pointers,s=n.length;i.firstInput||(i.firstInput=V(e));s>1&&!i.firstMultiple?i.firstMultiple=V(e):1===s&&(i.firstMultiple=!1);var r=i.firstInput,a=i.firstMultiple,h=a?a.center:r.center,l=e.center=j(n);e.timeStamp=d(),e.deltaTime=e.timeStamp-r.timeStamp,e.angle=$(h,l),e.distance=Z(h,l),function(t,e){var i=e.center,n=t.offsetDelta||{},s=t.prevDelta||{},r=t.prevInput||{};1!==e.eventType&&4!==r.eventType||(s=t.prevDelta={x:r.deltaX||0,y:r.deltaY||0},n=t.offsetDelta={x:i.x,y:i.y});e.deltaX=s.x+(i.x-n.x),e.deltaY=s.y+(i.y-n.y)}(i,e),e.offsetDirection=G(e.deltaX,e.deltaY);var c=B(e.deltaTime,e.deltaX,e.deltaY);e.overallVelocityX=c.x,e.overallVelocityY=c.y,e.overallVelocity=p(c.x)>p(c.y)?c.x:c.y,e.scale=a?(u=a.pointers,f=n,Z(f[0],f[1],X)/Z(u[0],u[1],X)):1,e.rotation=a?function(t,e){return $(e[1],e[0],X)+$(t[1],t[0],X)}(a.pointers,n):0,e.maxPointers=i.prevInput?e.pointers.length>i.prevInput.maxPointers?e.pointers.length:i.prevInput.maxPointers:e.pointers.length,function(t,e){var i,n,s,r,a=t.lastInterval||e,h=e.timeStamp-a.timeStamp;if(8!=e.eventType&&(h>25||a.velocity===o)){var l=e.deltaX-a.deltaX,c=e.deltaY-a.deltaY,u=B(h,l,c);n=u.x,s=u.y,i=p(u.x)>p(u.y)?u.x:u.y,r=G(l,c),t.lastInterval=e}else i=a.velocity,n=a.velocityX,s=a.velocityY,r=a.direction;e.velocity=i,e.velocityX=n,e.velocityY=s,e.direction=r}(i,e);var u,f;var v=t.element;W(e.srcEvent.target,v)&&(v=e.srcEvent.target);e.target=v}(t,i),t.emit("hammer.input",i),t.recognize(i),t.session.prevInput=i}function V(t){for(var e=[],i=0;i<t.pointers.length;)e[i]={clientX:u(t.pointers[i].clientX),clientY:u(t.pointers[i].clientY)},i++;return{timeStamp:d(),pointers:e,center:j(e),deltaX:t.deltaX,deltaY:t.deltaY}}function j(t){var e=t.length;if(1===e)return{x:u(t[0].clientX),y:u(t[0].clientY)};for(var i=0,n=0,s=0;s<e;)i+=t[s].clientX,n+=t[s].clientY,s++;return{x:u(i/e),y:u(n/e)}}function B(t,e,i){return{x:e/t||0,y:i/t||0}}function G(t,e){return t===e?1:p(t)>=p(e)?t<0?2:4:e<0?8:16}function Z(t,e,i){i||(i=Y);var n=e[i[0]]-t[i[0]],s=e[i[1]]-t[i[1]];return Math.sqrt(n*n+s*s)}function $(t,e,i){i||(i=Y);var n=e[i[0]]-t[i[0]],s=e[i[1]]-t[i[1]];return 180*Math.atan2(s,n)/Math.PI}F.prototype={handler:function(){},init:function(){this.evEl&&I(this.element,this.evEl,this.domHandler),this.evTarget&&I(this.target,this.evTarget,this.domHandler),this.evWin&&I(S(this.element),this.evWin,this.domHandler)},destroy:function(){this.evEl&&w(this.element,this.evEl,this.domHandler),this.evTarget&&w(this.target,this.evTarget,this.domHandler),this.evWin&&w(S(this.element),this.evWin,this.domHandler)}};var J={mousedown:1,mousemove:2,mouseup:4},K="mousedown",Q="mousemove mouseup";function tt(){this.evEl=K,this.evWin=Q,this.pressed=!1,F.apply(this,arguments)}b(tt,F,{handler:function(t){var e=J[t.type];1&e&&0===t.button&&(this.pressed=!0),2&e&&1!==t.which&&(e=4),this.pressed&&(4&e&&(this.pressed=!1),this.callback(this.manager,e,{pointers:[t],changedPointers:[t],pointerType:H,srcEvent:t}))}});var et={pointerdown:1,pointermove:2,pointerup:4,pointercancel:8,pointerout:8},it={2:O,3:"pen",4:H,5:"kinect"},nt="pointerdown",st="pointermove pointerup pointercancel";function rt(){this.evEl=nt,this.evWin=st,F.apply(this,arguments),this.store=this.manager.session.pointerEvents=[]}s.MSPointerEvent&&!s.PointerEvent&&(nt="MSPointerDown",st="MSPointerMove MSPointerUp MSPointerCancel"),b(rt,F,{handler:function(t){var e=this.store,i=!1,n=t.type.toLowerCase().replace("ms",""),s=et[n],r=it[t.pointerType]||t.pointerType,a=r==O,o=x(e,t.pointerId,"pointerId");1&s&&(0===t.button||a)?o<0&&(e.push(t),o=e.length-1):12&s&&(i=!0),o<0||(e[o]=t,this.callback(this.manager,s,{pointers:e,changedPointers:[t],pointerType:r,srcEvent:t}),i&&e.splice(o,1))}});var at={touchstart:1,touchmove:2,touchend:4,touchcancel:8};function ot(){this.evTarget="touchstart",this.evWin="touchstart touchmove touchend touchcancel",this.started=!1,F.apply(this,arguments)}function ht(t,e){var i=D(t.touches),n=D(t.changedTouches);return 12&e&&(i=R(i.concat(n),"identifier",!0)),[i,n]}b(ot,F,{handler:function(t){var e=at[t.type];if(1===e&&(this.started=!0),this.started){var i=ht.call(this,t,e);12&e&&i[0].length-i[1].length===0&&(this.started=!1),this.callback(this.manager,e,{pointers:i[0],changedPointers:i[1],pointerType:O,srcEvent:t})}}});var lt={touchstart:1,touchmove:2,touchend:4,touchcancel:8},ct="touchstart touchmove touchend touchcancel";function ut(){this.evTarget=ct,this.targetIds={},F.apply(this,arguments)}function pt(t,e){var i=D(t.touches),n=this.targetIds;if(3&e&&1===i.length)return n[i[0].identifier]=!0,[i,i];var s,r,a=D(t.changedTouches),o=[],h=this.target;if(r=i.filter(function(t){return W(t.target,h)}),1===e)for(s=0;s<r.length;)n[r[s].identifier]=!0,s++;for(s=0;s<a.length;)n[a[s].identifier]&&o.push(a[s]),12&e&&delete n[a[s].identifier],s++;return o.length?[R(r.concat(o),"identifier",!0),o]:void 0}b(ut,F,{handler:function(t){var e=lt[t.type],i=pt.call(this,t,e);i&&this.callback(this.manager,e,{pointers:i[0],changedPointers:i[1],pointerType:O,srcEvent:t})}});function dt(){F.apply(this,arguments);var t=E(this.handler,this);this.touch=new ut(this.manager,t),this.mouse=new tt(this.manager,t),this.primaryTouch=null,this.lastTouches=[]}function ft(t,e){1&t?(this.primaryTouch=e.changedPointers[0].identifier,vt.call(this,e)):12&t&&vt.call(this,e)}function vt(t){var e=t.changedPointers[0];if(e.identifier===this.primaryTouch){var i={x:e.clientX,y:e.clientY};this.lastTouches.push(i);var n=this.lastTouches;setTimeout(function(){var t=n.indexOf(i);t>-1&&n.splice(t,1)},2500)}}function mt(t){for(var e=t.srcEvent.clientX,i=t.srcEvent.clientY,n=0;n<this.lastTouches.length;n++){var s=this.lastTouches[n],r=Math.abs(e-s.x),a=Math.abs(i-s.y);if(r<=25&&a<=25)return!0}return!1}b(dt,F,{handler:function(t,e,i){var n=i.pointerType==O,s=i.pointerType==H;if(!(s&&i.sourceCapabilities&&i.sourceCapabilities.firesTouchEvents)){if(n)ft.call(this,e,i);else if(s&&mt.call(this,i))return;this.callback(t,e,i)}},destroy:function(){this.touch.destroy(),this.mouse.destroy()}});var gt=P(c.style,"touchAction"),Tt=gt!==o,yt="compute",bt="auto",Et="manipulation",Ct="none",At="pan-x",It="pan-y",wt=function(){if(!Tt)return!1;var t={},e=s.CSS&&s.CSS.supports;return["auto","manipulation","pan-y","pan-x","pan-x pan-y","none"].forEach(function(i){t[i]=!e||s.CSS.supports("touch-action",i)}),t}();function Wt(t,e){this.manager=t,this.set(e)}Wt.prototype={set:function(t){t==yt&&(t=this.compute()),Tt&&this.manager.element.style&&wt[t]&&(this.manager.element.style[gt]=t),this.actions=t.toLowerCase().trim()},update:function(){this.set(this.manager.options.touchAction)},compute:function(){var t=[];return m(this.manager.recognizers,function(e){C(e.options.enable,[e])&&(t=t.concat(e.getTouchAction()))}),function(t){if(L(t,Ct))return Ct;var e=L(t,At),i=L(t,It);if(e&&i)return Ct;if(e||i)return e?At:It;if(L(t,Et))return Et;return bt}(t.join(" "))},preventDefaults:function(t){var e=t.srcEvent,i=t.offsetDirection;if(this.manager.session.prevented)e.preventDefault();else{var n=this.actions,s=L(n,Ct)&&!wt[Ct],r=L(n,It)&&!wt[It],a=L(n,At)&&!wt[At];if(s){var o=1===t.pointers.length,h=t.distance<2,l=t.deltaTime<250;if(o&&h&&l)return}if(!a||!r)return s||r&&6&i||a&&i&U?this.preventSrc(e):void 0}},preventSrc:function(t){this.manager.session.prevented=!0,t.preventDefault()}};var Lt=32;function _t(t){this.options=h({},this.defaults,t||{}),this.id=M++,this.manager=null,this.options.enable=A(this.options.enable,!0),this.state=1,this.simultaneous={},this.requireFail=[]}function xt(t){return 16&t?"cancel":8&t?"end":4&t?"move":2&t?"start":""}function Dt(t){return 16==t?"down":8==t?"up":2==t?"left":4==t?"right":""}function Rt(t,e){var i=e.manager;return i?i.get(t):t}function Pt(){_t.apply(this,arguments)}function Mt(){Pt.apply(this,arguments),this.pX=null,this.pY=null}function St(){Pt.apply(this,arguments)}function Nt(){_t.apply(this,arguments),this._timer=null,this._input=null}function zt(){Pt.apply(this,arguments)}function kt(){Pt.apply(this,arguments)}function Ot(){_t.apply(this,arguments),this.pTime=!1,this.pCenter=!1,this._timer=null,this._input=null,this.count=0}function Ht(t,e){return(e=e||{}).recognizers=A(e.recognizers,Ht.defaults.preset),new Ut(t,e)}_t.prototype={defaults:{},set:function(t){return h(this.options,t),this.manager&&this.manager.touchAction.update(),this},recognizeWith:function(t){if(v(t,"recognizeWith",this))return this;var e=this.simultaneous;return e[(t=Rt(t,this)).id]||(e[t.id]=t,t.recognizeWith(this)),this},dropRecognizeWith:function(t){return v(t,"dropRecognizeWith",this)||(t=Rt(t,this),delete this.simultaneous[t.id]),this},requireFailure:function(t){if(v(t,"requireFailure",this))return this;var e=this.requireFail;return-1===x(e,t=Rt(t,this))&&(e.push(t),t.requireFailure(this)),this},dropRequireFailure:function(t){if(v(t,"dropRequireFailure",this))return this;t=Rt(t,this);var e=x(this.requireFail,t);return e>-1&&this.requireFail.splice(e,1),this},hasRequireFailures:function(){return this.requireFail.length>0},canRecognizeWith:function(t){return!!this.simultaneous[t.id]},emit:function(t){var e=this,i=this.state;function n(i){e.manager.emit(i,t)}i<8&&n(e.options.event+xt(i)),n(e.options.event),t.additionalEvent&&n(t.additionalEvent),i>=8&&n(e.options.event+xt(i))},tryEmit:function(t){if(this.canEmit())return this.emit(t);this.state=Lt},canEmit:function(){for(var t=0;t<this.requireFail.length;){if(!(33&this.requireFail[t].state))return!1;t++}return!0},recognize:function(t){var e=h({},t);if(!C(this.options.enable,[this,e]))return this.reset(),void(this.state=Lt);56&this.state&&(this.state=1),this.state=this.process(e),30&this.state&&this.tryEmit(e)},process:function(t){},getTouchAction:function(){},reset:function(){}},b(Pt,_t,{defaults:{pointers:1},attrTest:function(t){var e=this.options.pointers;return 0===e||t.pointers.length===e},process:function(t){var e=this.state,i=t.eventType,n=6&e,s=this.attrTest(t);return n&&(8&i||!s)?16|e:n||s?4&i?8|e:2&e?4|e:2:Lt}}),b(Mt,Pt,{defaults:{event:"pan",threshold:10,pointers:1,direction:30},getTouchAction:function(){var t=this.options.direction,e=[];return 6&t&&e.push(It),t&U&&e.push(At),e},directionTest:function(t){var e=this.options,i=!0,n=t.distance,s=t.direction,r=t.deltaX,a=t.deltaY;return s&e.direction||(6&e.direction?(s=0===r?1:r<0?2:4,i=r!=this.pX,n=Math.abs(t.deltaX)):(s=0===a?1:a<0?8:16,i=a!=this.pY,n=Math.abs(t.deltaY))),t.direction=s,i&&n>e.threshold&&s&e.direction},attrTest:function(t){return Pt.prototype.attrTest.call(this,t)&&(2&this.state||!(2&this.state)&&this.directionTest(t))},emit:function(t){this.pX=t.deltaX,this.pY=t.deltaY;var e=Dt(t.direction);e&&(t.additionalEvent=this.options.event+e),this._super.emit.call(this,t)}}),b(St,Pt,{defaults:{event:"pinch",threshold:0,pointers:2},getTouchAction:function(){return[Ct]},attrTest:function(t){return this._super.attrTest.call(this,t)&&(Math.abs(t.scale-1)>this.options.threshold||2&this.state)},emit:function(t){if(1!==t.scale){var e=t.scale<1?"in":"out";t.additionalEvent=this.options.event+e}this._super.emit.call(this,t)}}),b(Nt,_t,{defaults:{event:"press",pointers:1,time:251,threshold:9},getTouchAction:function(){return[bt]},process:function(t){var e=this.options,i=t.pointers.length===e.pointers,n=t.distance<e.threshold,s=t.deltaTime>e.time;if(this._input=t,!n||!i||12&t.eventType&&!s)this.reset();else if(1&t.eventType)this.reset(),this._timer=f(function(){this.state=8,this.tryEmit()},e.time,this);else if(4&t.eventType)return 8;return Lt},reset:function(){clearTimeout(this._timer)},emit:function(t){8===this.state&&(t&&4&t.eventType?this.manager.emit(this.options.event+"up",t):(this._input.timeStamp=d(),this.manager.emit(this.options.event,this._input)))}}),b(zt,Pt,{defaults:{event:"rotate",threshold:0,pointers:2},getTouchAction:function(){return[Ct]},attrTest:function(t){return this._super.attrTest.call(this,t)&&(Math.abs(t.rotation)>this.options.threshold||2&this.state)}}),b(kt,Pt,{defaults:{event:"swipe",threshold:10,velocity:.3,direction:30,pointers:1},getTouchAction:function(){return Mt.prototype.getTouchAction.call(this)},attrTest:function(t){var e,i=this.options.direction;return 30&i?e=t.overallVelocity:6&i?e=t.overallVelocityX:i&U&&(e=t.overallVelocityY),this._super.attrTest.call(this,t)&&i&t.offsetDirection&&t.distance>this.options.threshold&&t.maxPointers==this.options.pointers&&p(e)>this.options.velocity&&4&t.eventType},emit:function(t){var e=Dt(t.offsetDirection);e&&this.manager.emit(this.options.event+e,t),this.manager.emit(this.options.event,t)}}),b(Ot,_t,{defaults:{event:"tap",pointers:1,taps:1,interval:300,time:250,threshold:9,posThreshold:10},getTouchAction:function(){return[Et]},process:function(t){var e=this.options,i=t.pointers.length===e.pointers,n=t.distance<e.threshold,s=t.deltaTime<e.time;if(this.reset(),1&t.eventType&&0===this.count)return this.failTimeout();if(n&&s&&i){if(4!=t.eventType)return this.failTimeout();var r=!this.pTime||t.timeStamp-this.pTime<e.interval,a=!this.pCenter||Z(this.pCenter,t.center)<e.posThreshold;if(this.pTime=t.timeStamp,this.pCenter=t.center,a&&r?this.count+=1:this.count=1,this._input=t,0===this.count%e.taps)return this.hasRequireFailures()?(this._timer=f(function(){this.state=8,this.tryEmit()},e.interval,this),2):8}return Lt},failTimeout:function(){return this._timer=f(function(){this.state=Lt},this.options.interval,this),Lt},reset:function(){clearTimeout(this._timer)},emit:function(){8==this.state&&(this._input.tapCount=this.count,this.manager.emit(this.options.event,this._input))}}),Ht.VERSION="2.0.7",Ht.defaults={domEvents:!1,touchAction:yt,enable:!0,inputTarget:null,inputClass:null,preset:[[zt,{enable:!1}],[St,{enable:!1},["rotate"]],[kt,{direction:6}],[Mt,{direction:6},["swipe"]],[Ot],[Ot,{event:"doubletap",taps:2},["tap"]],[Nt]],cssProps:{userSelect:"none",touchSelect:"none",touchCallout:"none",contentZooming:"none",userDrag:"none",tapHighlightColor:"rgba(0,0,0,0)"}};function Ut(t,e){var i;this.options=h({},Ht.defaults,e||{}),this.options.inputTarget=this.options.inputTarget||t,this.handlers={},this.session={},this.recognizers=[],this.oldCssProps={},this.element=t,this.input=new((i=this).options.inputClass||(z?rt:k?ut:N?dt:tt))(i,q),this.touchAction=new Wt(this,this.options.touchAction),Yt(this,!0),m(this.options.recognizers,function(t){var e=this.add(new t[0](t[1]));t[2]&&e.recognizeWith(t[2]),t[3]&&e.requireFailure(t[3])},this)}function Yt(t,e){var i,n=t.element;n.style&&(m(t.options.cssProps,function(s,r){i=P(n.style,r),e?(t.oldCssProps[i]=n.style[i],n.style[i]=s):n.style[i]=t.oldCssProps[i]||""}),e||(t.oldCssProps={}))}Ut.prototype={set:function(t){return h(this.options,t),t.touchAction&&this.touchAction.update(),t.inputTarget&&(this.input.destroy(),this.input.target=t.inputTarget,this.input.init()),this},stop:function(t){this.session.stopped=t?2:1},recognize:function(t){var e=this.session;if(!e.stopped){var i;this.touchAction.preventDefaults(t);var n=this.recognizers,s=e.curRecognizer;(!s||s&&8&s.state)&&(s=e.curRecognizer=null);for(var r=0;r<n.length;)i=n[r],2===e.stopped||s&&i!=s&&!i.canRecognizeWith(s)?i.reset():i.recognize(t),!s&&14&i.state&&(s=e.curRecognizer=i),r++}},get:function(t){if(t instanceof _t)return t;for(var e=this.recognizers,i=0;i<e.length;i++)if(e[i].options.event==t)return e[i];return null},add:function(t){if(v(t,"add",this))return this;var e=this.get(t.options.event);return e&&this.remove(e),this.recognizers.push(t),t.manager=this,this.touchAction.update(),t},remove:function(t){if(v(t,"remove",this))return this;if(t=this.get(t)){var e=this.recognizers,i=x(e,t);-1!==i&&(e.splice(i,1),this.touchAction.update())}return this},on:function(t,e){if(t!==o&&e!==o){var i=this.handlers;return m(_(t),function(t){i[t]=i[t]||[],i[t].push(e)}),this}},off:function(t,e){if(t!==o){var i=this.handlers;return m(_(t),function(t){e?i[t]&&i[t].splice(x(i[t],e),1):delete i[t]}),this}},emit:function(t,e){this.options.domEvents&&function(t,e){var i=r.createEvent("Event");i.initEvent(t,!0,!0),i.gesture=e,e.target.dispatchEvent(i)}(t,e);var i=this.handlers[t]&&this.handlers[t].slice();if(i&&i.length){e.type=t,e.preventDefault=function(){e.srcEvent.preventDefault()};for(var n=0;n<i.length;)i[n](e),n++}},destroy:function(){this.element&&Yt(this,!1),this.handlers={},this.session={},this.input.destroy(),this.element=null}},h(Ht,{INPUT_START:1,INPUT_MOVE:2,INPUT_END:4,INPUT_CANCEL:8,STATE_POSSIBLE:1,STATE_BEGAN:2,STATE_CHANGED:4,STATE_ENDED:8,STATE_RECOGNIZED:8,STATE_CANCELLED:16,STATE_FAILED:Lt,DIRECTION_NONE:1,DIRECTION_LEFT:2,DIRECTION_RIGHT:4,DIRECTION_UP:8,DIRECTION_DOWN:16,DIRECTION_HORIZONTAL:6,DIRECTION_VERTICAL:U,DIRECTION_ALL:30,Manager:Ut,Input:F,TouchAction:Wt,TouchInput:ut,MouseInput:tt,PointerEventInput:rt,TouchMouseInput:dt,SingleTouchInput:ot,Recognizer:_t,AttrRecognizer:Pt,Tap:Ot,Pan:Mt,Swipe:kt,Pinch:St,Rotate:zt,Press:Nt,on:I,off:w,each:m,merge:y,extend:T,assign:h,inherit:b,bindFn:E,prefixed:P}),(void 0!==s?s:"undefined"!=typeof self?self:{}).Hammer=Ht,(n=function(){return Ht}.call(e,i,e,t))===o||(t.exports=n)}(window,document)},9586(t,e,i){"use strict";var n=i(7168);class s{constructor(t){this.container=null,this.cards=null,this.cardHeight=0,this.breakPoint=1230,this.tabsInitiated=!1,this.navContainer=null,this.nav=null,this.navTabs=null,this.tabSections=null,this.navLeft=null,this.navRight=null,this.navContainerWidth=null,this.navWidth=null,this.containerDiff=null,this.cardWidth=286,this.travelDistance=null,this.isLeftMost=!0,this.isRightMost=!1,this.numTabs=null,this.activeTabIndex=0,this.container=t,this.cells=esriClientUtils.selectAll(".table-comparison_row-cell",this.container),this.columns=esriClientUtils.selectAll(".table-comparison_col",this.container),this.mobileHeaderWrap=esriClientUtils.select(".table-comparison_mobile-table-header",this.container),this.columnPanels=esriClientUtils.selectAll(".table-comparison_col",this.container),this.desktopColHeadWrap=esriClientUtils.select(".table-comparison_grid",this.container),this.columnInfoBlocks=esriClientUtils.selectAll(".table-comparison_col-information",this.container),this.navContainer=esriClientUtils.select(".table-comparison_mobile-tabs-wrapper",this.container),this.nav=this.container.querySelector(".table-comparison_mobile-tabs"),this.navTabs=esriClientUtils.selectAll(".table-comparison_mobile-tab",this.container),this.navLeft=this.container.querySelector(".table-comparison_mobile-tab-left"),this.navRight=this.container.querySelector(".table-comparison_mobile-tab-right"),this.navContainerWidth=Number.parseInt(this.navContainer.offsetWidth,10),this.navWidth=Number.parseInt(this.nav.offsetWidth,10),this.containerDiff=this.navContainerWidth-this.navWidth,this.travelDistance=2*this.cardWidth,this.numTabs=Number(this.navTabs.length),this.resize=this.resize.bind(this),this.showHidePrices=this.showHidePrices.bind(this),this.moveLeft=this.moveLeft.bind(this),this.moveRight=this.moveRight.bind(this),this.activateTab=this.activateTab.bind(this),this.showHidePrices(),this.navLeft.addEventListener("click",this.moveLeft),this.navRight.addEventListener("click",this.moveRight),this.resize(),window.addEventListener("resize",esriClientUtils.debounce(this.resize,10));let e=new n(this.navContainer);e.on("swipeleft",this.moveRight),e.on("swiperight",this.moveLeft)}resize(){this.draw(!0)}calculateWidths(){this.navContainerWidth=Number.parseInt(this.navContainer.offsetWidth,10),this.navWidth=Number.parseInt(this.nav.offsetWidth,10),this.containerDiff=this.navContainerWidth-this.navWidth}roundToTabMultiple(t){return t>0?Math.ceil(t/this.cardWidth.toFixed(1))*this.cardWidth:t<0?Math.floor(t/this.cardWidth.toFixed(1))*this.cardWidth:0}findActiveTab(){[].forEach.call(this.navTabs,(t,e)=>{t.classList.contains("is-active")&&(this.activeTabIndex=e)})}draw(t){if(this.calculateWidths(),this.findActiveTab(),window.innerWidth<=480){t?this.nav.style.left="".concat(-this.activeTabIndex*this.cardWidth,"px"):this.makeVisibleActive(),this.travelDistance=this.cardWidth;let e=(this.navContainerWidth-Number.parseInt(this.navTabs[0].offsetWidth,10))/2;this.nav.style.paddingLeft="".concat(Number.parseInt(e,10),"px"),this.nav.style.paddingRight="".concat(Number.parseInt(e,10),"px")}else window.innerWidth<=768?(this.travelDistance=this.cardWidth,this.nav.style.paddingLeft="0px",this.nav.style.paddingRight="0px"):(this.travelDistance=2*this.cardWidth,this.nav.style.paddingLeft="0px",this.nav.style.paddingRight="0px");if(this.calculateWidths(),this.containerDiff>=0)this.navLeft.classList.add("hide"),this.navRight.classList.add("hide"),this.nav.style.left="".concat(this.containerDiff/2,"px");else{this.navLeft.classList.remove("hide"),this.navRight.classList.remove("hide"),this.nav.style.left=this.nav.style.left||0,this.calculateWidths();let t=this.navContainerWidth-this.cardWidth*this.numTabs;window.innerWidth>480&&this.isRightMost&&t>Number.parseInt(this.nav.style.left,10)&&(this.nav.style.left="".concat(t,"px")),Number.parseInt(this.nav.style.left,10)>=0?(this.navLeft.classList.add("hide"),this.isLeftMost=!0):this.isLeftMost=!1,Number.parseInt(this.nav.style.left,10)<=t?(this.navRight.classList.add("hide"),this.isRightMost=!0):this.isRightMost=!1}window.innerWidth>=this.breakPoint?this.drawAsTable():this.drawAsList("Yes")}makeVisibleActive(){let t=this;setTimeout(()=>{t.nav.style.left=t.nav.style.left||0,t.nav.style.paddingLeft=t.nav.style.paddingLeft||0;let e,i=t.roundToTabMultiple(Number.parseInt(t.nav.style.left,10));[].forEach.call(t.navTabs,(n,s)=>{t.roundToTabMultiple(n.offsetLeft-Number.parseInt(t.nav.style.paddingLeft,10))==Math.abs(i)&&(e=s)}),e>-1&&t.activateTab(e)},350)}drawAsTable(){this.mobileHeaderWrap.setAttribute("aria-hidden","true"),this.navContainer.setAttribute("aria-hidden","true"),this.desktopColHeadWrap.setAttribute("aria-hidden","false"),this.columnInfoBlocks.forEach(t=>{t.setAttribute("aria-hidden","true")});let t=esriClientUtils.selectAll(".table-comparison_col:nth-child(1) .table-comparison_row-cell",this.container).length;for(let i=0;i<t;i++){let t=[];for(let e=0;e<this.columns.length;e++)t.push(esriClientUtils.select(".table-comparison_col:nth-child(".concat(e+1,") .table-comparison_row-cell:nth-child(").concat(i+1,")"),this.container));var e=0;t.forEach(t=>{t.style.height="auto",t.offsetHeight>e&&(e=t.offsetHeight)}),t.forEach(t=>{t.style.height="".concat(e,"px")})}this.bindTabs(!1)}drawAsList(){this.mobileHeaderWrap.setAttribute("aria-hidden","false"),this.navContainer.setAttribute("aria-hidden","false"),this.desktopColHeadWrap.setAttribute("aria-hidden","true"),this.columnInfoBlocks.forEach(t=>{t.setAttribute("aria-hidden","false")}),this.cells.forEach(t=>{t.style.height="auto"}),this.columns.forEach(t=>{esriClientUtils.selectAll("[data-row-type-checkicon],[data-row-type-custom]",t).forEach((t,e)=>{(e+1)%2&&t.classList.add("odd-list-item")})}),this.bindTabs(!0)}bindTabs(t){let e=this;if(t)if(e.tabsInitiated){let t=0;e.navTabs.forEach((e,i)=>{e.classList.contains("is-active")&&(t=i)}),e.activateTab(t)}else e.tabsInitiated=!0,e.navTabs.forEach((t,i)=>{t.addEventListener("click",()=>{e.activateTab(i)})}),e.activateTab(0);else e.navTabs.forEach((t,i)=>{t.removeEventListener("click",()=>{e.activateTab(i)})}),e.columnPanels.forEach(t=>{t.setAttribute("aria-hidden","false")})}activateTab(t){this.navTabs.forEach(t=>{t.classList.remove("is-active")}),this.navTabs[t].classList.add("is-active"),this.columnPanels.forEach(t=>{t.setAttribute("aria-hidden","true")}),this.columnPanels[t+1].setAttribute("aria-hidden","false")}moveLeft(){this.nav.style.left=this.nav.style.left||0,this.isRightMost=!1;let t=Number.parseInt(this.nav.style.left,10)+this.travelDistance;t>0&&(t=0,this.isLeftMost=!0),this.nav.style.left="".concat(t,"px"),this.draw()}moveRight(){this.nav.style.left=this.nav.style.left||0,this.isLeftMost=!1;let t=Number.parseInt(this.nav.style.left,10)-this.travelDistance;window.innerWidth>480&&t<this.navContainer.offsetWidth-this.cardWidth*this.numTabs&&(t=this.navContainer.offsetWidth-this.cardWidth*this.numTabs,this.isRightMost=!0),this.nav.style.left="".concat(t,"px"),this.draw()}showHidePrices(){"object"==typeof geoip2&&geoip2.country(t=>{if("US"==t.country.iso_code){esriClientUtils.selectAll(".table-comparison_geo-sensitive--US-show").forEach(t=>{t.style.display="block"})}},t=>{})}}document.addEventListener("DOMContentLoaded",t=>{esriClientUtils.selectAll(".table-comparison").forEach(t=>{new s(t)})})}},e={};function i(n){var s=e[n];if(void 0!==s)return s.exports;var r=e[n]={exports:{}};return t[n](r,r.exports,i),r.exports}(()=>{"use strict";i(9586)})()})();
(()=>{"use strict";function t(){new class{constructor(){if(this.rgbBoxShadow="",this.shadowboxOpacity="0.60",this.locNavNode=document.querySelector("#second-nav"),this.localNavigation=document.querySelector(".local-navigation"),this.codeLang="",this.currentURL=this.removePathExt(window.location.pathname),this.designThemeColor="0079c1",this.newNavObj={},this.localNavTheme="",this.langCodeAPI="/bin/esri/getlanguagecode",this.isVerDocParam=!1,this.versionNum="",this.chevron={},this.titleLoaded=!1,this.desktopBreakpoint=1024,this.maxContentWidth=0,this.locNavNode){const t=document.documentElement.getAttribute("lang")||"en",e=[].slice.call(document.querySelectorAll("nav"));this.localNavigation.classList.add("calcite-mode-dark"),this.localNavigation.classList.add("desktop-view"),e.length>0&&this.findVersionAttr(e),"en"!==t&&2==t.length?this.find4CharLangCode(t):(this.buildRequest(),this.titleOverlap(),window.addEventListener("resize",()=>{clearTimeout(this.resizeTimeout),this.resizeTimeout=setTimeout(()=>{this.titleOverlap()},300)})),this.locNavNode.hasAttribute("path")||(this.enableMobileBtn(),this.enableTeritaryBtn())}}determineMenuView(t){const e=document.querySelector(".local-navigation"),i=document.querySelector(".nav-menu").getBoundingClientRect().right,a=window.innerWidth-i,n=t.offsetWidth+30,r=document.querySelector(".nav-menu").offsetWidth,l=t.parentElement;this.maxContentWidth=n+r+a<this.maxContentWidth?this.maxContentWidth:n+r+a;const c=n+40>window.innerWidth;l.classList.toggle("ellipsis",c);const s=!(window.innerWidth<this.desktopBreakpoint||this.maxContentWidth>window.innerWidth);e.classList.toggle("desktop-view",s)}async titleOverlap(){const t=await async function(t){let e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:2e3,i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:200;const a=Date.now();for(;Date.now()-a<e;){const e=document.querySelector(t);if(e)return e;await new Promise(t=>setTimeout(t,i))}return null}(".nav-titlelink");t?(this.titleLoaded=!0,this.determineMenuView(t)):console.warn("titleOverlap: menu title not authored.")}createChevron(){const t=document.createElementNS("http://www.w3.org/2000/svg","svg");t.setAttribute("width","16"),t.setAttribute("height","16"),t.setAttribute("fill","#ffffff"),t.setAttribute("viewBox","0 0 16 16"),t.setAttribute("class","calcite-icon-chevron");const e=document.createElementNS("http://www.w3.org/2000/svg","path");return e.setAttribute("d","M8 11.207l-4-4V5.793l4 4 4-4v1.414z"),t.appendChild(e),t}enableMobileBtn(){const t=document.querySelector(".nav-menu-title"),e=document.querySelector(".nav-submenu");t&&e&&t.addEventListener("click",i=>{i.preventDefault();const a="false"===e.getAttribute("aria-hidden");t.setAttribute("data-local-expanded",String(!a)),e.setAttribute("aria-hidden",String(a))})}enableTeritaryBtn(){const t=document.querySelector(".third-nav-title"),e=document.querySelector(".third-nav-bar-container");null!==e&&e.setAttribute("aria-hidden","true"),t&&e&&t.addEventListener("click",i=>{const a="false"===e.getAttribute("aria-hidden");t.setAttribute("data-local-expanded",String(!a)),e.setAttribute("aria-hidden",String(a))})}findVersionAttr(t){t.forEach(t=>{const e=t.getAttribute("version");e&&(this.isVerDocParam=!0,this.versionNum=e)})}updateNavTypeAttr(t){let e=this.locNavNode.getAttribute("path"),i=new RegExp("(\\/[a-z][^n]\\/)"),a=null!=i.exec(e)?i.exec(e):null;e&&null!=e&&null!=a&&(e=e.replace(a[0],t),this.locNavNode.setAttribute("path",e))}buildRequest(){let t="",e="/bin/esri/localnavigation",i=this.locNavNode.getAttribute("domain")?this.locNavNode.getAttribute("domain"):"";if("newNavigation"==this.locNavNode.getAttribute("navtype")){let a=this.locNavNode.getAttribute("path");"browserPath"==this.locNavNode.getAttribute("path")||(this.newNavObj=this.configureNewNav()),t="".concat(i).concat(e,"?path=").concat(a)}else this.existingPath=this.removePathExt(this.locNavNode.getAttribute("path")),t="".concat(i).concat(e,"?path=").concat(this.existingPath);this.getJSONData(t)}find4CharLangCode(t){let e="",i="".concat(window.location.protocol,"//").concat(window.location.host),a="".concat("http://localhost:4502"==i?"https://aem-dev.esri.com/":"https://www.esri.com").concat(this.langCodeAPI,"?lang=").concat(t),n=new XMLHttpRequest;n.open("GET",a),n.responseType="text",n.send(),n.addEventListener("readystatechange",()=>{4==n.readyState&&200==n.status&&(e=n.response?n.response:"en",this.updateNavTypeAttr("/".concat(e,"/")),this.buildRequest())})}checkForColorTheme(){let t=!1;if([].slice.call(document.querySelectorAll("meta")).forEach(e=>{"color theme"===e.getAttribute("name")&&(this.localNavTheme=e.getAttribute("content"),""!=this.localNavTheme&&(t=!0))}),!t){let t="localColorTheme",e=document.head||document.getElementsByTagName("head")[0],i=document.getElementById(t)||document.createElement("style");i.id=t,i.hasChildNodes()||(i.innerHTML+=":root { --theme-color: #".concat(this.designThemeColor,"; } ")),e.appendChild(i)}}configureNewNav(){let t={};return t.titletext=this.locNavNode.getAttribute("titletext"),t.titlelink=this.locNavNode.getAttribute("titlelink"),t.triallabel=this.locNavNode.getAttribute("triallabel"),t.triallink=this.locNavNode.getAttribute("triallink"),t}drawTertiaryElmt(t,e,i){return(t=[].slice.call(t)).forEach(t=>{let a=document.createElement("li"),n=document.createElement("a");a.setAttribute("class","teritem"),n.setAttribute("href",t.pageLink),n.setAttribute("class","es-nav-terlink arrowStyleLocal"),n.setAttribute("aria-describedby",i),n.setAttribute("data-event","track-component"),n.setAttribute("data-component-name","Local Navigation"),n.setAttribute("data-component-link","".concat(i," - ").concat(t.pageTitle)),n.setAttribute("data-component-link-type","text"),n.innerHTML+=t.pageTitle,a.appendChild(n),e.appendChild(a)}),e}resetDropdowns(t){[].slice.call(t.querySelectorAll(".nav-subitem")).forEach(t=>{let e=t.childNodes[0],i=t.querySelector(".subitem-menu");t.querySelectorAll(".calcite-icon-chevron").forEach(t=>{t.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down")}),null!=i&&(e.setAttribute("data-local-expanded","false"),i.setAttribute("aria-hidden","true"))})}findParent(t){let e=t.split("/");return e.pop(),e.join("/")}drawSecondary(t){let e="",i=!1,a=document.createElement("li");a.setAttribute("class","nav-subitem"),a.setAttribute("id","item-".concat(t.pageTitle.replace(/ /g,"-")));let n=null==t.subnavItems||"true"===t.tertiary?document.createElement("a"):document.createElement("button");if(n.setAttribute("class","es-nav-sublink-local"),n.setAttribute("aria-controls","subitem-menu-".concat(t.pageTitle)),n.setAttribute("id",t.pageTitle.replace(/ /g,"-")),n.setAttribute("data-event","track-component"),n.setAttribute("data-component-name","Local Navigation"),n.setAttribute("data-component-link",t.pageTitle),t.tertiary?n.setAttribute("data-component-link-type","button"):n.setAttribute("data-component-link-type","text"),this.currentURL&&(e=this.findParent(this.currentURL)),null!=t.subnavItems){if("false"==t.tertiary)n.setAttribute("aria-label","".concat(t.pageTitle,"-Click to open sub navigation items")),n.setAttribute("aria-current","true"),n.setAttribute("data-local-expanded",t.tertiary);else if("true"==t.tertiary){n.removeAttribute("aria-controls"),t.subnavItems.forEach(t=>{(this.removePathExt(t.redirect)===this.currentURL||this.removePathExt(t.redirect).replace(/^\/content\/esri-sites/,"")===this.currentURL||this.currentURL.includes(t.wildcard)&&t.wildcard.length>0)&&(i=!0)}),n.setAttribute("href",t.pageLink),n.setAttribute("data-tertiary","true"),n.setAttribute("aria-current","true")}}else n.removeAttribute("aria-controls"),n.setAttribute("href",t.pageLink);if(this.removePathExt(t.pageLink)===e||i?(n.setAttribute("attr-parent","true"),n.setAttribute("aria-current","true")):n.removeAttribute("attr-parent"),this.removePathExt(t.pageLink)===this.currentURL)n.setAttribute("aria-current","true"),"true"==t.tertiary&&n.setAttribute("data-tertiary","true");else if(null!=t.tertiary){let e=[].slice.call(t.subnavItems),a=!1;e.forEach(t=>{let e=this.removePathExt(t.pageLink).split("/"),r=e[e.length-1],l=e[e.length-2],c=this.currentURL,s=c.split("/"),o=s[s.length-1],d=s[s.length-2];o!==r&&o!==this.removePathExt(r)||d!==l||(a=!0),c.includes(l)&&n.setAttribute("aria-current","true"),n.removeAttribute("aria-current"),(a||i)&&n.setAttribute("aria-current","true")})}else{n.removeAttribute("aria-current");let e=window.location.href;t.redirect===e&&n.setAttribute("aria-current","true")}if(n.innerHTML+=t.pageTitle,a.appendChild(n),t.tertiary&&"false"==t.tertiary&&(this.chevron=this.createChevron(),a.appendChild(this.chevron)),a.addEventListener("click",t=>{t.stopPropagation();let e=t.target,i=e.parentNode,a=e.parentNode.parentNode,n=i.querySelector(".subitem-menu");if(this.chevron=i.querySelector(".calcite-icon-chevron"),null!=n){t.preventDefault();let i=e.getAttribute("data-local-expanded"),r=e.getAttribute("id");"true"!=i&&this.resetDropdowns(a),"false"==e.getAttribute("data-local-expanded")?(e.setAttribute("is-active","true"),e.setAttribute("aria-label","".concat(r,"-Click to close sub navigation items")),e.setAttribute("data-local-expanded","true"),n.setAttribute("aria-hidden","false"),this.chevron.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-up")):(e.setAttribute("is-active","false"),e.setAttribute("aria-label","".concat(r,"-Click to open sub navigation items")),e.setAttribute("data-local-expanded","false"),n.setAttribute("aria-hidden","true"),this.chevron.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down"))}}),document.addEventListener("click",t=>{if(!a.contains(t.target)){let t=a.querySelector(".subitem-menu"),e=a.querySelector(".es-nav-sublink-local");t&&e&&(t.setAttribute("aria-hidden","true"),e.setAttribute("data-local-expanded","false"),e.setAttribute("is-active","false"),e.setAttribute("aria-label","".concat(e.getAttribute("id"),"-Click to open sub navigation items")))}}),t.subnavItems){let e=t.subnavItems;if("false"==t.tertiary){let i=document.createElement("div");i.setAttribute("class","subitem-menu"),i.setAttribute("aria-hidden","true"),i.setAttribute("role","region"),i.setAttribute("id","subitem-menu-".concat(t.pageTitle));let n=document.createElement("ul");n.setAttribute("class","terlist"),n.setAttribute("aria-labelledby",t.pageTitle);let r=this.drawTertiaryElmt(e,n,t.pageTitle);i.appendChild(r),a.appendChild(i)}}return a}wildCard(t){let e=!1,i=t.wildcard.split("/").filter(t=>""!==t.trim()).pop();return this.currentURL.replace(/\/[^/]*$/,"").indexOf(i)>0&&(e=!0),e}getLastFolder(t){return t.split("/").pop().replace(/\.[^/.]+$/,"")}isSameDomain(t,e){try{const i=new URL(t,window.location.origin);return i.hostname===e||i.hostname.endsWith(".".concat(e))}catch(e){return console.error("Invalid URL:",t),!1}}drawTertiaryDropDown(t){let e=document.createElement("li"),i=t.wildcard;0==t.wildcard.length&&(i=null);let a=!1,n=!1;a=this.removePathExt(t.redirect).includes(i),n=this.currentURL.includes(i),e.setAttribute("class","ternav-menu-item"),e.setAttribute("aria-current","false"),(this.removePathExt(t.pageLink)===this.currentURL||this.removePathExt(t.redirect)===this.currentURL&&a||this.currentURL.includes(i)||this.removePathExt(t.redirect).replace(/^\/content\/esri-sites/,"")===this.currentURL&&a)&&e.setAttribute("aria-current","true");let r=document.createElement("a");r.setAttribute("class","ternav-menu-item-link"),r.setAttribute("data-event","track-component"),r.setAttribute("data-component-name","Local Navigation"),r.setAttribute("data-component-link","Subnav - ".concat(t.pageTitle)),r.setAttribute("data-component-link-type","text");let l=this.wildCard(t);return""!=t.redirect?(this.isVerDocParam&&(t.redirect=t.redirect.replace("latest",this.versionNum)),r.setAttribute("href",t.redirect),this.getLastFolder(this.currentURL)===this.getLastFolder(t.redirect)&&r.setAttribute("aria-current","true")):r.setAttribute("href",t.pageLink),l?(r.setAttribute("is-Active","true"),e.setAttribute("is-Active","true")):(r.removeAttribute("is-Active"),e.removeAttribute("is-Active")),r.innerHTML+=t.pageTitle,e.appendChild(r),e}drawMainTitle(t,e,i){let a=this.createChevron(),n=document.createElement("div");n.setAttribute("class","nav-".concat(e)),n.setAttribute("id","nav-".concat(e));let r="";if("titlelink"==i?(r=document.createElement("a"),r.setAttribute("class","nav-".concat(i))):(r=document.createElement("a"),r.setAttribute("class","nav-".concat(i," esri-ui-button esri-ui-button--brand esri-ui-button--solid calcite-mode-light")),this.isSameDomain(t[i],window.location.hostname)||(r.setAttribute("rel","noopener noreferrer"),r.setAttribute("target","_blank"))),r.setAttribute("data-event","track-component"),r.setAttribute("data-component-name","Local Navigation"),r.setAttribute("data-component-link-type","text"),r.setAttribute("data-component-link",t.title),r.setAttribute("tabindex","0"),"#"==t?(r.innerHTML+="",r.setAttribute("href",t)):t[e]?(r.innerHTML+=t[e],r.setAttribute("href",t[i]),r.setAttribute("data-component-link-type","button"),r.setAttribute("data-component-link",t[e])):null!=t.titletext&&(r.innerHTML+=t.titletext,r.setAttribute("href",t.titlelink)),r.addEventListener("keyup",t=>{let e=t.target;this.resetDropdowns(e.parentNode.parentNode)}),n.appendChild(r),"titlelink"==i){let t=document.createElement("button");t.setAttribute("class","nav-menu-title"),t.setAttribute("href","#"),t.setAttribute("data-local-expanded","false");let e=document.createElement("span");e.setAttribute("class","assistText"),e.innerHTML+="Menu",t.appendChild(e),t.appendChild(a),n.appendChild(t),setTimeout(()=>{t.addEventListener("click",e=>{e.preventDefault();let i=document.querySelector(".nav-submenu"),n=document.querySelector(".third-nav-bar-container"),r=document.querySelector(".third-nav-title");n&&(r.querySelector(".calcite-icon-chevron").setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down"),n.setAttribute("aria-hidden","true"),r.setAttribute("data-local-expanded","false")),"false"===t.getAttribute("data-local-expanded")?(a.classList.toggle("calcite-icon-chevron-up"),t.setAttribute("data-local-expanded","true"),i.setAttribute("aria-hidden","false")):(a.classList.toggle("calcite-icon-chevron-up"),t.setAttribute("data-local-expanded","false"),i.setAttribute("aria-hidden","true"))},300)})}return n}resolveActiveTernaryNav(t){let e=[].slice.call(t);e.forEach((t,i)=>{let a=t.querySelector("a");e.length-1!==i&&(a.setAttribute("is-active","false"),a.setAttribute("aria-current","false"))})}appendNavToDOM(t){let e=document.getElementById("second-nav"),i=!1,a="",n={},r={},l={},c={},s={},o={hasMain:!1,mainPos:0,colorTheme:"",hasPageTitle:!1};for(let e=0;e<t.length;e++)for(const[i,a]of Object.entries(t[e]))"main"==="".concat(i)&&(o.mainPos=e,o.hasMain=!0),"designTheme"==="".concat(i)&&(o.colorTheme=""==o.colorTheme?a:o.colorTheme,this.designThemeColor=o.colorTheme),"pageTitle"==="".concat(i)&&(o.hasPageTitle=!0);if(e&&null!=e){if(r=document.createElement("nav"),r.setAttribute("class","second-nav calcite-mode-dark"),r.setAttribute("id","second-nav"),r.setAttribute("aria-label","Main"),r.setAttribute("data-local-expanded","false"),o.hasMain)if(null!=t[o.mainPos]){let e=t[o.mainPos].main,i=Object.keys(e);if(i.length>=2&&""==e.title&&""==e.titlelink){let t=this.drawMainTitle(this.newNavObj,"title","titlelink");r.appendChild(t)}else{let t=this.drawMainTitle(e,i[0],i[1]);r.appendChild(t)}let a=t[o.mainPos].main.triallink;"string"==typeof a&&(t[o.mainPos].main.triallink=a.replace(/^\.html/,""))}else if(null!=this.newNavObj.titletext&&null!=this.newNavObj.titlelink){let t=this.drawMainTitle(this.newNavObj,"title","titlelink");r.appendChild(t)}else{let t=this.drawMainTitle("#","title","titlelink");r.appendChild(t)}if(o.hasPageTitle){let e=document.createElement("div");e.setAttribute("class","nav-menu");let n=document.createElement("div");n.setAttribute("class","nav-submenu"),s=document.createElement("ul"),s.setAttribute("class","nav-sublist"),s.setAttribute("aria-labelledby","nav-title"),this.checkForColorTheme(),(t=[].slice.call(t)).forEach(t=>{if(null!=t.pageTitle){let e=this.drawSecondary(t);if("true"==t.tertiary){let e=[].slice.call(t.subnavItems);this.removePathExt(t.pageLink)===this.currentURL&&(i=!0,l=t.subnavItems,a=t.pageTitle),e.forEach(e=>{let n=!!this.currentURL.includes(e.wildcard)&&this.currentURL.includes(e.wildcard);null!=e.wildcard&&e.wildcard.length>0&&n&&(i=!0,a=e.pageTitle,l=t.subnavItems),this.removePathExt(e.redirect)===this.currentURL||this.removePathExt(e.redirect).replace(/^\/content\/esri-sites/,"")===this.currentURL?(i=!0,a=e.pageTitle,l=t.subnavItems):this.removePathExt(e.pageLink)===this.currentURL&&(i=!0,a=t.pageTitle,l=t.subnavItems)})}s.appendChild(e)}}),n.appendChild(s),e.appendChild(n),r.appendChild(e),document.addEventListener("click",t=>{let i=n.contains(t.target),a=e.contains(t.target),r=document.querySelector(".nav-menu-title"),l=null!==r&&r.contains(t.target),c=e.closest(".second-nav").querySelector("svg"),s=e.querySelectorAll(".nav-subitem");i||a||l||n&&e&&("false"==n.getAttribute("aria-hidden")&&(n.setAttribute("aria-hidden","true"),r.setAttribute("data-local-expanded","false")),s.forEach(t=>{let e=t.querySelector(".calcite-icon-chevron");e&&e.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down")}),c&&c.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down"),e.parentNode.setAttribute("data-local-expanded","false"))})}if(null!=t[0].main){let e=Object.keys(t[0].main);if(4==e.length&&""!=t[0].main.triallabel){let i=this.drawMainTitle(t[0].main,e[2],e[3]);n=this.drawMainTitle(t[0].main,e[2],e[3]),i.id="".concat(i.id,"-desktop"),r.appendChild(i),s.appendChild(n)}}else if(null!=this.newNavObj.triallabel&&null!=this.newNavObj.triallink&&""!=this.newNavObj.triallabel){let t=this.drawMainTitle(this.newNavObj,"triallabel","triallink");t.id="".concat(t.id,"-desktop"),n=this.drawMainTitle(this.newNavObj,"triallabel","triallink"),r.appendChild(t),s.appendChild(n)}}if(i){r.style.boxShadow="none",c=document.createElement("div"),c.setAttribute("class","third-nav-bar calcite-theme-dark calcite-mode-dark");let t=document.createElement("button");t.setAttribute("class","third-nav-title"),t.setAttribute("data-local-expanded","false"),t.setAttribute("href","#"),t.innerHTML+=a;let i=document.createElement("span");i.setAttribute("class","assistText"),i.innerHTML+="";let n=document.createElement("div");n.setAttribute("class","grid-container-small");let s=document.createElement("nav");s.setAttribute("class","third-nav-bar-container"),s.setAttribute("aria-hidden","true"),s.setAttribute("aria-label","Tertiary Navigation");let o=document.createElement("ul");o.setAttribute("class","third-nav-menu"),l=[].slice.call(l),l.forEach(t=>{if("false"==t.hideInTertiaryNav){let e=this.drawTertiaryDropDown(t);o.appendChild(e)}}),t.addEventListener("click",e=>{let i=t.querySelector(".calcite-icon-chevron");"false"==t.getAttribute("data-local-expanded")?(s.setAttribute("aria-hidden","false"),t.setAttribute("data-local-expanded","true"),i.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-up")):(s.setAttribute("aria-hidden","true"),t.setAttribute("data-local-expanded","false"),i.setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down"))}),document.addEventListener("click",e=>{let i=c.contains(e.target),a=t.contains(e.target);i||a||(s.setAttribute("aria-hidden","true"),t.setAttribute("data-local-expanded","false"),t.querySelector(".calcite-icon-chevron").setAttribute("class","calcite-icon-chevron calcite-icon-chevron-down"))}),s.appendChild(o),n.appendChild(s),this.chevron=this.createChevron(),t.appendChild(this.chevron),t.appendChild(i),c.appendChild(t),c.appendChild(n),e.insertAdjacentElement("afterEnd",c)}e.insertAdjacentElement("afterEnd",r),e.parentNode.removeChild(e)}getJSONData(t){let e={},i=new XMLHttpRequest;i.open("GET",t),i.responseType="text",i.send(),i.addEventListener("readystatechange",()=>{if(4==i.readyState&&200==i.status)if(e=JSON.parse(i.responseText),console.info("nav:",e),e.length>0){this.appendNavToDOM(e);const t=document.querySelectorAll(".ternav-menu-item[is-active]");t.length>1&&this.resolveActiveTernaryNav(t),[].slice.call(document.querySelectorAll(".es-nav-terlink")).forEach(t=>{this.applyThemeArrow(t)}),this.addCaratStyles()}else console.warn("Local Nav Component: Invalid Navigation Path")}),i.onerror=function(){console.warn("Invalid URL:",url)}}getHostName(t){let e=t.match(/:\/\/(www\d?\.)?(.[^/:]+)/i);return null!=e&&e.length>2&&"string"==typeof e[2]&&e[2].length>0?e[2]:null}removePathExt(t){if(null!==t){t.includes("html")&&(t=t.slice(0,-5)),"/"!=t[0]&&(t="/".concat(t));let e=this.getHostName(t);if(null!=e){return t.replace(/^\/?https?:\/\/w*\.?/,"").replace(e,"")}return t}}styleString(t,e,i,a){return"".concat(e).concat(t,"]:").concat(i," ").concat(a)}addCaratStyles(){const t=document.head||document.getElementsByTagName("head")[0],e=document.createElement("style");e.innerHTML+=":root {"+"--secondarytheme-color: #".concat(this.designThemeColor,";")+"--secondarycustom-theme-color: #".concat(this.designThemeColor,";")+"--secondarytheme-color60: #".concat(this.designThemeColor,"80;}"),t.appendChild(e)}hexToRgb(t){return t.replace(/^#?([a-f\d])([a-f\d])([a-f\d])$/i,(t,e,i,a)=>"#".concat(e).concat(e).concat(i).concat(i).concat(a).concat(a)).substring(1).match(/.{2}/g).map(t=>Number.parseInt(t,16))}dropdownArrow(t,e,i,a){const n="".concat(t,"Local"),r=document.head||document.getElementsByTagName("head")[0],l=document.getElementById(n)||document.createElement("style");l.id=n;let c="".concat(t,"Local");return this.className+=" ".concat(c),l.hasChildNodes()||(l.innerHTML+=" .".concat(c,":").concat(e,"{").concat(i,":").concat(a,"}")),r.appendChild(l),this}applyThemeArrow(){this.hexToRgb("#".concat(this.designThemeColor)),this.rgbBoxShadow="rgba(".concat(this.hexToRgb("#".concat(this.designThemeColor)),",").concat(this.shadowboxOpacity,")"),this.dropdownArrow("arrowStyle","after","content","url(\"data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 15 15' width='16' height='16'%3E%3Cpath fill='%23".concat(this.designThemeColor,"' d='M8 14.3l5.3-5.3H0V8h13.3L8 2.7V1.3L15.2 8.5 8 15.7V14.3z' /%3E%3C/svg%3E\");"))}}}"loading"!==document.readyState?t():document.addEventListener("DOMContentLoaded",()=>{t()})})();
(()=>{"use strict";var e={5038(){globalThis.addEventListener("DOMContentLoaded",()=>{document.querySelectorAll("#sraForm").forEach(e=>{e.addEventListener("submit",e=>{e.preventDefault();const t=e.target.querySelector("calcite-input"),i=e.target.dataset.searchPath;if(t.value){const e=new URLSearchParams("");e.append("q",t.value),globalThis.location.href="".concat(i,"?").concat(e)}})})})},2986(){class e{constructor(){this.imgSVGz=[].slice.call(document.querySelectorAll("img.svg")),this.convertToSVG(this.imgSVGz)}convertToSVG(e,t){e.forEach(e=>{let t=e.getAttribute("src"),i=e.getAttribute("alt"),a=e.getAttribute("data-theme");t.indexOf(".svg")>-1&&function(e,t){let i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:()=>{};const a=new XMLHttpRequest;a.addEventListener("readystatechange",()=>{4===a.readyState&&(200===a.status?t(a.responseText):i())}),a.open("GET",e),a.send()}("".concat(t),t=>{t=(t=t.replace(/id=\"icon-ui-svg\"/g,'id="icon-ui-svg" class="icon-ui-svg"')).replace(/id=\"icons-ui-svg\"/g,'id="icon-ui-svg" class="icon-ui-svg"');let s=(new DOMParser).parseFromString(t,"text/xml"),n=s.querySelector("svg");if(i&&null!==i){const e=document.createElement("title"),t=s.querySelector("svg").firstElementChild;""!==t.innerHTML?(t.innerHTML="".concat(i),t.setAttribute("role","image")):(e.setAttribute("stroke","none"),e.setAttribute("stroke-width","1px"),e.innerHTML="".concat(i),e.setAttribute("role","image"),n.insertBefore(e,t))}else n.setAttribute("aria-hidden","true");if(a&&null!==a){const e=s.querySelector("svg");e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base")&&null!=e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base")&&(e.querySelector(".icon-ui-svg--base, .icons-ui-svg--base").style.fill=a),e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary")&&null!=e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary")&&(e.querySelector(".icon-ui-svg--primary, .icons-ui-svg--primary").style.fill=a)}null!==e.parentNode&&e.parentNode.replaceChild(s.querySelector("svg"),e)})})}}document.addEventListener("DOMContentLoaded",()=>{class t{constructor(t){let i=t.querySelector(".cmp-image__image");if(null!==i){let a=t.querySelector(".cmp-image").getAttribute("data-asset");if(null!==a){if(".svg"===a.slice(-4)){let t=new e;i.setAttribute("src",a),t.convertToSVG([i])}}}}}setTimeout(function(){const e=esriClientUtils.selectAll(".esri-image");e.length>0&&e.forEach(e=>{new t(e)})},500)})}},t={};function i(a){var s=t[a];if(void 0!==s)return s.exports;var n=t[a]={exports:{}};return e[a](n,n.exports,i),n.exports}function a(e){for(var t=1;t<arguments.length;t++){var i=arguments[t];for(var a in i)e[a]=i[a]}return e}var s=function e(t,i){function s(e,s,n){if("undefined"!=typeof document){"number"==typeof(n=a({},i,n)).expires&&(n.expires=new Date(Date.now()+864e5*n.expires)),n.expires&&(n.expires=n.expires.toUTCString()),e=encodeURIComponent(e).replace(/%(2[346B]|5E|60|7C)/g,decodeURIComponent).replace(/[()]/g,escape);var r="";for(var l in n)n[l]&&(r+="; "+l,!0!==n[l]&&(r+="="+n[l].split(";")[0]));return document.cookie=e+"="+t.write(s,e)+r}}return Object.create({set:s,get:function(e){if("undefined"!=typeof document&&(!arguments.length||e)){for(var i=document.cookie?document.cookie.split("; "):[],a={},s=0;s<i.length;s++){var n=i[s].split("="),r=n.slice(1).join("=");try{var l=decodeURIComponent(n[0]);if(a[l]=t.read(r,l),e===l)break}catch(e){}}return e?a[e]:a}},remove:function(e,t){s(e,"",a({},t,{expires:-1}))},withAttributes:function(t){return e(this.converter,a({},this.attributes,t))},withConverter:function(t){return e(a({},this.converter,t),this.attributes)}},{attributes:{value:Object.freeze(i)},converter:{value:Object.freeze(t)}})}({read:function(e){return'"'===e[0]&&(e=e.slice(1,-1)),e.replace(/(%[\dA-F]{2})+/gi,decodeURIComponent)},write:function(e){return encodeURIComponent(e).replace(/%(2[346BF]|3[AC-F]|40|5[BDE]|60|7[BCD])/g,decodeURIComponent)}},{path:"/"});class n{constructor(){this.priceCaseInfo={},this.requiresVerification=!1,this.priceCaseResp,this.verificationId=null,this.verificationType=null,this.isDistributor=!1}async init(e){const t=e.getAttribute("data-componentid"),i=document.querySelector('[data-component-meta-id="'.concat(t,'"]'));if(this.requiresVerification=i.getAttribute("data-verification-needed")||!1,this.verificationType=i.getAttribute("data-verification-type")||null,"true"===this.requiresVerification){const e=new URLSearchParams(window.location.search.slice(1)).get("verificationId");e&&(this.verificationId=e)}return this.isDistributor=this.getDistributorConfig(),this.priceCaseInfo=this.getPriceCaseInfo(i),this.priceCaseResp=await this.getPriceMssg(),this.mapActivePriceCase(t)}mapActivePriceCase(e){const t=this.priceCaseInfo,i=this.priceCaseResp;return"ROW"===i.zone?{case:"ROW",componentId:e}:!i.errorType||i.errorType.length<2||"logIntoSeePriceMessage"==i.errorType||"selectCustomerNumberMessage"==i.errorType?{case:null,componentId:e}:{case:t[i.errorType],componentId:e}}getPriceCaseInfo(e){const t={};return[].slice.call(e.querySelectorAll("option")).forEach(e=>{const i=e.getAttribute("data-btnlabel"),a=e.getAttribute("data-linkmssg"),s=e.getAttribute("data-linkurl"),n=e.getAttribute("data-errormssg");t[n]={btnLabel:i,linkMssg:a,linkURL:s}}),t}getDistributorConfig(){const e=location.host;return"preview.esri.com/"!=="".concat(e,"/")}async getPriceMssg(){const e={country_code:await this.getCountryCode(),verificationNeeded:"false"},t=this.getSelectedZone();t&&(e.selectedZone=t),"true"==this.requiresVerification&&this.verificationType&&(e.verificationNeeded="true",e.verificationType=this.verificationType,this.verificationId&&(e.verificationId=this.verificationId)),this.isDistributor&&(e.distributor=this.isDistributor);try{const t=await fetch("/bin/esri/productpricing",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(e)});if(!t.ok)throw new Error("HTTP error! status: ".concat(t.status));return await t.json()}catch(e){return console.error({error:e}),{error:"catch API Error: [".concat(e,"]")}}}getSelectedZone(){const e=s.get("selectedZone");return e||null}async getCountryCode(){if(void 0!==s){const e=s.get("omniCoutryCode");if(e)return e}const e=await this.getLocalGeoIP();return e||"US"}async getLocalGeoIP(){let e=null;if("undefined"!=typeof geoip2){const t=new Promise((e,t)=>{geoip2.insights(t=>{e(t.country.iso_code)})});await t.then(t=>{e=t})}return e}}document.addEventListener("DOMContentLoaded",()=>{if([].slice.call(document.querySelectorAll(".search-results")).length){const e=[].slice.call(document.querySelectorAll("[data-buycard-app]"));e.length&&e.forEach(e=>{!async function(e){const t=new n,i=await t.init(e);window.esri_priceCase=i}(e)})}}),document.addEventListener("DOMContentLoaded",function(){if([].slice.call(document.querySelectorAll(".buy-card")).length){let e=[].slice.call(document.querySelectorAll("[data-buycard-app]"));e.length&&e.forEach(e=>{!async function(e){let t=new n,i=await t.init(e);!function(e){let t=document.querySelector('[data-componentid="'.concat(e.componentId,'"]'));if(t){let i=[].slice.call(t.querySelectorAll("[data-price-container]")),a=[].slice.call(t.querySelectorAll("[data-default-cta]"));if("ROW"==e.case)return void a.forEach(e=>{e.classList.remove("hidden")});if(null==e.case)return i.forEach(e=>{e.classList.remove("hidden")}),void a.forEach(e=>{e.classList.remove("hidden")});let s=e.case.btnLabel||"Please author button",n='<div class="calcite-button-wrapper" \n                        data-event="track-store-component" \n                        data-component-name="Buy Cards" \n                        data-product-link="Launch Now" \n                        data-component-link-type="button">\n                        <calcite-button  \n                        appearance="solid" \n                        href="'.concat(e.case.linkURL,'">').concat(s,"</calcite-button></div>"),r=t.querySelectorAll("[data-cta-wrapper]");r.length&&r.forEach(e=>{e.innerHTML=n})}}(i)}(e)})}let e=[].slice.call(document.querySelectorAll(".bc-block-group"));if(e.length){e.forEach(e=>{let t=[].slice.call(e.querySelectorAll(".card.block"));t.length&&t.forEach(e=>{let t=[].slice.call(e.querySelectorAll("[data-product-type]"));t.length&&t.forEach(e=>{let t=e.getAttribute("data-product-type").trim().split(","),i=[];t.length&&t.forEach(e=>{if(e.indexOf(":")>-1){let t=e.split(":")[1];if(t.indexOf("/")){let e=t.split("/");t=e[e.length-1];let i=/-/gi;t=t.replace(i," ")}i.push(t)}else i.push(e)});let a=i.length>1?"".concat(i[0],", ").concat(i[1]):i[0];e.setAttribute("data-product-type",a),e.innerHTML=a})})}),r(),window.addEventListener("resize",(t=r,i=150,function(){var e=this,n=arguments;clearTimeout(s),s=setTimeout(function(){s=null,a||t.apply(e,n)},i),a&&!s&&t.apply(e,n)}))}var t,i,a,s;function r(){let e=document.querySelector(".buy-cards").offsetWidth,t=[].slice.call(document.querySelectorAll(".bc-card"));if(t.length>0&&null!==t){let i=t[0].offsetWidth,a=Math.floor(e/i),s=1,n=1;t.forEach((e,t)=>{s=0!=t&&t%a===0?s+1:s,[].slice.call(e.querySelectorAll("[data-component-grid]")).forEach(e=>{e.setAttribute("data-component-grid","r".concat(s,"-c").concat(n))}),n=n==a?1:++n})}}});i(5038),i(2986);function r(e){return r="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e},r(e)}function l(e){var t=function(e,t){if("object"!=r(e)||!e)return e;var i=e[Symbol.toPrimitive];if(void 0!==i){var a=i.call(e,t||"default");if("object"!=r(a))return a;throw new TypeError("@@toPrimitive must return a primitive value.")}return("string"===t?String:Number)(e)}(e,"string");return"symbol"==r(t)?t:t+""}function o(e,t,i){return(t=l(t))in e?Object.defineProperty(e,t,{value:i,enumerable:!0,configurable:!0,writable:!0}):e[t]=i,e}window.KalturaAnalytics=class{constructor(){this.pauseTime=0,this.isPlaying=0,this.isSeeking=!1,this.isSeeked=!1,this.isPaused=!1,this.globalTime=0,this.PLAY_REACHED_25=!1,this.PLAY_REACHED_50=!1,this.PLAY_REACHED_75=!1,this.PLAY_REACHED_90=!1,this.PLAY_REACHED_100=!1}getMediaSegment(e){const t=parseFloat((e.currentTime/e.duration).toFixed(2)),i=[{max:.25,name:"M:0-25"},{max:.5,name:"M:25-50"},{max:.75,name:"M:50-75"},{max:.9,name:"M:75-90"},{max:1,name:"M:90-100"}].find(e=>t<=e.max);return i?i.name:""}async getVideo(e){const t=new Promise((t,i)=>{fetch("https://kaltura-api-proxy.esri.com/getVideo?entryId=".concat(e),{method:"GET",headers:{Accept:"application/json","Content-Type":"application/json;charset=UTF-8"}}).then(e=>{if(!e.ok)throw new Error("HTTP error! status: ".concat(e.status));return e.json()}).then(e=>{t(e)}).catch(e=>{i(e)})});return await t}findVideoContainer(e){const t=document.querySelector('.kaltura-container__single-player[id^="kaltura_player_id_'.concat(e,'"]'));if(!t)return void console.warn("No container found: ",e);const i=t.getAttribute("style"),a=t.querySelector(".kaltura-player-container");a&&a.setAttribute("style",i),t.removeAttribute("style")}removeKalturaCss(){let e=document.querySelector('link[href*="webapps-cdn.esri.com/CDN/components/kaltura/css/esri-kaltura-player-v7.1.css"]');e&&e.parentNode.removeChild(e)}resetMilestones(){this.PLAY_REACHED_25=!1,this.PLAY_REACHED_50=!1,this.PLAY_REACHED_75=!1,this.PLAY_REACHED_90=!1,this.PLAY_REACHED_100=!1}buildAnalyticsPlayer(e,t){this.resetAnalyticsState();try{let i=KalturaPlayer.setup({targetId:"kaltura_player_id_".concat(t),provider:{partnerId:e.partnerId,uiConfId:"52883622",ks:e.token.ks}});i.loadMedia({entryId:e.id}),this.setupAnalyticsEvents(i,e)}catch(e){console.warn("Error building analytics player",e)}}resetAnalyticsState(){this.pauseTime=0,this.isPlaying=0,this.isSeeking=!1,this.isSeeked=!1,this.isPaused=!1,this.globalTime=0,this.resetMilestones()}setupAnalyticsEvents(e,t){e.addEventListener(e.Event.Core.PLAYBACK_START,()=>{let i=0;const a=this.getMediaSegment(e);i=Math.round(parseFloat(e.duration)),window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaFirstStart",data:{mediaKey:"Kaltura|".concat(t.id,"|").concat(t.name),mediaContentDetails:"onpage",mediaSegment:"".concat(a),mediaTotalDuration:"".concat(i)}})}),e.addEventListener(e.Event.Core.ENDED,()=>{this.pauseTime=0,this.isPlaying=1,this.globalTime=0,this.isSeeking=!1,this.isSeeked=!1,this.PLAY_REACHED_25=!0,this.PLAY_REACHED_50=!0,this.PLAY_REACHED_75=!0,this.PLAY_REACHED_90=!0,this.PLAY_REACHED_100=!0}),e.addEventListener(e.Event.Core.PLAY,()=>{const i=this.getMediaSegment(e);this.isSeeked=!1,this.isSeeking=!1,this.isPaused=!1,window.adobeDataLayer&&(this.isPlaying++,this.isPlaying>1&&window.adobeDataLayer.push({event:"mediaStart",data:{mediaKey:"Kaltura|".concat(t.id,"|").concat(t.name),mediaContentDetails:"onpage",mediaSegment:"".concat(i)}}))}),e.addEventListener(e.Event.Core.PAUSE,()=>{const i=this.getMediaSegment(e),a=Math.round(parseFloat(e.currentTime-this.pauseTime));this.isSeeked=!1,this.isSeeking=!1,this.isPaused=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaPaused",data:{mediaKey:"Kaltura|".concat(t.id,"|").concat(t.name),mediaContentDetails:"onpage",mediaSegment:"".concat(i),mediaTime:"".concat(a)}}),this.pauseTime=Math.round(parseFloat(e.currentTime))}),e.addEventListener(e.Event.Core.SEEKING,()=>{const i=this.getMediaSegment(e);this.isSeeking=!0,this.resetMilestones(),window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaPause/Seek",data:{mediaKey:"Kaltura|".concat(t.id,"|").concat(t.name),mediaContentDetails:"onpage",mediaSegment:"".concat(i),mediaTime:"".concat(this.globalTime)}}),!this.isPaused&&window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaStart",data:{mediaKey:"Kaltura|".concat(t.id,"|").concat(t.name),mediaContentDetails:"onpage",mediaSegment:"".concat(i),mediaTime:"".concat(this.globalTime)}}),this.pauseTime=Math.round(parseFloat(e.currentTime))}),e.addEventListener(e.Event.Core.SEEKED,()=>{let t=parseFloat((e.currentTime/e.duration).toFixed(2));t>=.25&&(this.PLAY_REACHED_25=!0),t>=.5&&(this.PLAY_REACHED_50=!0),t>=.75&&(this.PLAY_REACHED_75=!0),t>=.9&&(this.PLAY_REACHED_90=!0),t>=.99&&(this.PLAY_REACHED_100=!0),this.isSeeking=!1}),e.addEventListener(e.Event.Core.TIME_UPDATE,()=>{const i=parseFloat((e.currentTime/e.duration).toFixed(2)),a=Math.round(parseFloat(e.currentTime-this.pauseTime));this.globalTime=Math.round(parseFloat(e.currentTime)),this.trackMilestones(i,a,t)})}trackMilestones(e,t,i){!this.PLAY_REACHED_25&&e>=.25&&!this.isSeeking&&(this.PLAY_REACHED_25=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaMilestone25",data:{mediaKey:"Kaltura|".concat(i.id,"|").concat(i.name),mediaContentDetails:"onpage",mediaSegment:"M:0-25",percentage:"".concat(e),mediaTime:"".concat(t)}})),!this.PLAY_REACHED_50&&e>=.5&&!this.isSeeking&&(this.PLAY_REACHED_50=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaMilestone50",data:{mediaKey:"Kaltura|".concat(i.id,"|").concat(i.name),mediaContentDetails:"onpage",mediaSegment:"M:25-50",percentage:"".concat(e),mediaTime:"".concat(t)}})),!this.PLAY_REACHED_75&&e>=.75&&!this.isSeeking&&(this.PLAY_REACHED_75=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaMilestone75",data:{mediaKey:"Kaltura|".concat(i.id,"|").concat(i.name),mediaContentDetails:"onpage",mediaSegment:"M:50-75",mediaTime:"".concat(t)}})),!this.PLAY_REACHED_90&&e>=.9&&!this.isSeeking&&(this.PLAY_REACHED_90=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaMilestone90",data:{mediaKey:"Kaltura|".concat(i.id,"|").concat(i.name),mediaContentDetails:"onpage",mediaSegment:"M:75-90",mediaTime:"".concat(t)}})),!this.PLAY_REACHED_100&&e>.99&&!this.isSeeking&&(this.PLAY_REACHED_100=!0,window.adobeDataLayer&&window.adobeDataLayer.push({event:"mediaMilestone100",data:{mediaKey:"Kaltura|".concat(i.id,"|").concat(i.name),mediaContentDetails:"onpage",mediaSegment:"M:90-100",mediaTime:"".concat(t)}}))}async enableAnalytics(e){if(e)try{const t=await this.getVideo(e);!0===t.success&&this.buildAnalyticsPlayer(t.media,e)}catch(e){console.warn("Unable to connect to Kaltura API for analytics",e)}}},document.addEventListener("DOMContentLoaded",function(){var e=esriClientUtils.selectAll("[data-replay-gif=true]");e&&e.length>-1&&e.forEach(e=>{let t=e.parentNode;e.addEventListener("click",function(e){e.preventDefault();let i=t.querySelector("img").getAttribute("data-img-src");i?t.querySelector("img").setAttribute("src","".concat(i,"?rand=")+crypto.getRandomValues(new Uint32Array(1))[0]):(i=t.querySelector("img").getAttribute("src"),!i||t.querySelector("img").setAttribute("src","".concat(i)))})})}),document.addEventListener("DOMContentLoaded",()=>{function e(e){let t=e.play();void 0!==t&&t.then(t=>{e.play()}).catch(()=>null)}document.querySelectorAll(".video-play-pause").forEach(t=>{var i;t.setAttribute("tabindex","0"),t.setAttribute("aria-label","Playing Animation Title");const a="http://www.w3.org/2000/svg",s=document.createElementNS(a,"svg");s.setAttribute("class","play-progress-circle"),s.setAttribute("viewBox","0 0 100 100");const n=document.createElementNS(a,"circle");n.setAttribute("class","progress-background"),n.setAttribute("cx","50"),n.setAttribute("cy","50"),n.setAttribute("r","45");const r=document.createElementNS(a,"circle");if(r.setAttribute("class","progress-circle"),r.setAttribute("cx","50"),r.setAttribute("cy","50"),r.setAttribute("r","41.25"),r.style.strokeDasharray="259.18",r.style.strokeDashoffset="259.000",s.append(n,r),t.append(s),null===(i=t.parentElement)||void 0===i||!i.classList.contains("video-button-container")){const e=document.createElement("div");e.className="video-button-container",t.parentNode.insertBefore(e,t),e.appendChild(t)}!function(t){if(!t)return;t.addEventListener("keypress",e=>{const i=e.which||e.keyCode,a=t.getAttribute("aria-label");13!==i&&32!==i||function(e,t){const i=e.closest(".video-container"),a=i?i.querySelector("video.video-src, video"):null;if(!a)return void(t&&e.setAttribute("aria-label",t));const s=a.getAttribute("aria-label")||t||"animation";a.paused?e.setAttribute("aria-label","Pausing animation ".concat(s)):e.setAttribute("aria-label","Playing animation ".concat(s))}(t,a)});const i=t.closest(".video-button-container"),a=i.parentNode,s=a?a.querySelector("video"):null;i&&s&&(s.load(),s.addEventListener("loadedmetadata",()=>{const t=s.duration;!function(t,i,a){const s=259.18,n=t.querySelector(".play-progress-circle").querySelector(".progress-circle");function r(e){const t=e.parentNode.querySelector(".video-button-container").querySelector(".video-play-pause"),i=e.getAttribute("aria-label");e.paused?(t.setAttribute("aria-label","Pausing animation ".concat(i)),t.classList.add("paused")):(t.setAttribute("aria-label","Playing animation ".concat(i)),t.classList.remove("paused"))}function l(){const t=i.currentTime/a*s;n.style.strokeDashoffset=s-t,"false"===i.getAttribute("data-loop-video")?requestAnimationFrame(l):t>=s?(n.style.strokeDashoffset=s,e(i)):requestAnimationFrame(l),t===s&&(n.style.strokeDashoffset=s)}n.style.strokeDasharray=s,n.style.strokeDashoffset=s,window.matchMedia("(prefers-reduced-motion: reduce)").matches||t.addEventListener("click",()=>{i.paused?(e(i),r(i)):(i.pause(),r(i)),l()}),i.addEventListener("timeupdate",l),i.addEventListener("ended",()=>{r(i)}),i.addEventListener("play",()=>{r(i)}),i.addEventListener("pause",()=>{r(i)})}(i,s,t)}))}(t)})}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e,t,i){1===i&&this.convertHdr2H2(i),this.addClickEvent(e,i,t)}removeURLParameter(e){const t={},i=e.split("?");if(t.urlPath=i,i.length>=2){const e=i[1].split(/[&;]/g);return t.param=e,t}return e}convertHdr2H2(){const e=this.getCalciteModal(),t=e.querySelector("div"),i=document.createElement("h2");i.setAttribute("slot","header"),e.removeChild(t),e.appendChild(i)}getCFragPathNParam(e,t){const i=this.removeURLParameter(e),a="image"===i.param[1]||"image"===i.param[2],s=window.location.host+i.urlPath[0];this.getContFragJSON(s).then(e=>{this.asyncInsertCF(e,t,a)})}async asyncInsertCF(e,t,i){const a=this.getCalciteModal();await this.injectModalContent(e,a,t,i)}injectModalContent(e,t,i,a){return new Promise(s=>{setTimeout(()=>{const n=e["jcr:content"].data.master.title,r=void 0===e["jcr:content"].data.master.description?"":e["jcr:content"].data.master.description,l=t.querySelector("h2"),o=t.querySelector("div");if(l.innerHTML=n,l.setAttribute("style","margin-bottom: 0; position: relative; top: 10px"),o.innerHTML=r,!0===a){const a=window.location.host.indexOf("localhost")>-1?":4502":"",s=window.location.protocol+"//"+window.location.hostname+a+e["jcr:content"].data.master.image,n=document.createElement("img");n.setAttribute("slot","content"),n.setAttribute("attr-indx",i),n.setAttribute("src",s),t.appendChild(n)}s(t),t.setAttribute("open","true")},200)})}async addClickEvent(e,t,i){const a=this.getCalciteModal(),s=await this.getCalciteClose();e.addEventListener("click",e=>{e.preventDefault(),this.getCFragPathNParam(i,t)}),s.addEventListener("click",e=>{e.preventDefault(),this.removeCalciteContent(a)})}removeCalciteContent(e){const t=e.querySelector("h2"),i=e.querySelector("div"),a=e.querySelector("img");null!==t&&(t.innerHTML=""),null!==i&&(i.innerHTML=""),null!==a&&e.removeChild(a)}getCalciteClose(){const e=document.querySelector("calcite-modal");return new Promise(t=>{setTimeout(()=>{const i=e.shadowRoot.querySelector("button");return t(i),i},800)})}getCalciteModal(){const e=document.querySelector("calcite-modal");if(null!==e)return e}getContFragJSON(e){const t=window.location.protocol,i="".concat(t,"//").concat(e).concat(".25.json");return new Promise(function(e,t){fetch(i,{method:"GET",headers:{Accept:"application/json","Content-Type":"application/json;charset=UTF-8"}}).then(e=>{if(!e.ok)throw new Error("HTTP error! status: ".concat(e.status));return e.json()}).then(t=>{e(t)}).catch(e=>{console.warn("Unable to retrieve content fragment data.",e),t(e)})})}}const t=document.querySelectorAll("calcite-button, calcite-link");if(null!==t){const i="modal=";t.forEach((t,a)=>{if(t.hasAttribute("href")){const s=t.getAttribute("href");s.match(i)&&new e(t,s,a)}})}}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e){const t=e.getAttribute("channelid");this.channelObj={},this.playlistsObj={},this.playlistsCount=0,this.videosMaxPage=15,this.videoEntries=[],this.defaultSortData=[],this.defaultVideoEntries=[],this.mediaStartIndexGrid=0,this.mediaEndIndexGrid=0,this.mediaStartIndexDetailed=0,this.mediaEndIndexDetailed=0,this.mediaStartIndexCollapsed=0,this.mediaEndIndexCollapsed=0,this.searchDuration=[],this.searchDate=[],this.kalturaObjInit=[],this.searchStrGlobal={},this.searchStrGlobal.value="",this.filterVAL={},this.filterVALUE="",this.productVersion=!1,this.searchYear=!1,this.searchYearValue="",this.searchProductValue="",this.afterDays=this.getLastDays("01/01/1901"),this.beforeDays=this.getLastDays(this.getCurrentDateMMDDYYYY());if(null===document.querySelector("head").querySelector("#kaltura-player-script")){const e=document.createElement("script");e.src="https://cdnapisec.kaltura.com/p/3057483/embedPlaykitJs/uiconf_id/55531032",e.setAttribute("id","kaltura-player-script"),document.getElementsByTagName("head")[0].appendChild(e)}null!==t&&this.consumeAPIforFrontend(e,t)}consumeAPIforFrontend(e,t){const i="Kaltura: Channel ID contains no entries. View payload https://kaltura-api-proxy.esri.com/getChannel?channelId=".concat(t);t&&this.getChannelObj(t).then(t=>{const a=t;!0===a.success&&t.media[0].hasOwnProperty("playlists")&&t.media[0].hasOwnProperty("entries")?this.createHomeAndMedia(e,t):!0===a.success&&!t.media[0].hasOwnProperty("playlists")&&t.media[0].hasOwnProperty("entries")?this.createOnlyMedia(e,t):(this.networkMsg(i),console.warn(i))}).catch(e=>{this.networkMsg(e),console.error(e)}),t&&this.getChannelObj(t).then(e=>{this.defaultSortData=e}).catch(e=>{this.networkMsg(e),console.error(e)})}initFilterView(e,t){const i=window.matchMedia("screen and (min-width: 480px)"),a=window.matchMedia("screen and (min-width: 767px)"),s=window.matchMedia("screen and (min-width: 1440px)"),n=esriClientUtils.select(".channel-playlist--clear-all-container-background"),r=esriClientUtils.select(".channel-playlist__button-filter"),l=esriClientUtils.select(".channel-playlist__button-sort"),o=r.getAttribute("aria-expanded"),c=esriClientUtils.select(".channel-playlist__more-filter-btn"),d=esriClientUtils.select(".channel-playlist__more-back-btn");s.matches?(d.setAttribute("disabled",""),c.setAttribute("disabled","")):c.removeAttribute("disabled"),a.matches&&"true"===o&&n.setAttribute("aria-hidden","false");const u=i.matches?"l":"m";l.setAttribute("scale",u),r.setAttribute("scale",u),a.matches?(e.classList.contains("desktop")||e.classList.add("desktop"),t.forEach(e=>{e.classList.contains("open")||e.classList.add("open")})):(e.classList.contains("desktop")&&e.classList.remove("desktop"),t.forEach(e=>{e.classList.contains("open")&&e.classList.remove("open")}))}initializeFilterDialog(){const e=esriClientUtils.selectAll(".channel__filter-column"),t=e[0].parentNode.parentNode;this.initFilterView(t,e),window.addEventListener("resize",()=>{esriClientUtils.debounce(this.initFilterView(t,e),200)},!0),this.searchDuration[0]=!0;for(let e=1;e<=3;e++)this.searchDuration[e]=!1;this.searchDate[0]=!0;for(let e=1;e<=3;e++)this.searchDate[e]=!1}createHomeAndMedia(e,t){this.createChannelNPlaylist(t),this.channelObj.totalChannelVideoCount=t.media[0].directEntriesCount,this.initializeFilterDialog(e),this.addClickToNav(e),this.createHomeView(e),this.createMediaViews(e,t.media[0].entries),this.videoEntries=t.media[0].entries,this.defaultVideoEntries=t.media[0].entries,this.addClickToViews(e),this.enableSearch(e),this.enableFilterSearch(e)}createOnlyMedia(e,t){const i=document.querySelector("[attr-home]"),a=document.querySelector("[attr-media]"),s=document.getElementsByClassName("channel-container-home")[0],n=document.getElementsByClassName("channel-container-media")[0];this.initializeFilterDialog(e),n.setAttribute("attr-visible",!0),a.classList.add("active"),s.remove(),i.remove(),this.channelObj.totalChannelVideoCount=t.media[0].directEntriesCount,this.addClickToNav(e),this.createMediaViews(e,t.media[0].entries),this.videoEntries=t.media[0].entries,this.defaultVideoEntries=t.media[0].entries,this.addClickToViews(e),this.enableSearch(e),this.enableFilterSearch(e)}convertTimeFormat(e){const t=~~(e/3600),i=~~(e%3600/60),a=~~e%60;let s="";return t>0&&(s+="".concat(t,":").concat(i<10?"0":"")),s+="".concat(i,":").concat(a<10?"0":""),s+="".concat(a),s}convertToMedia(e){const t=Object.values(e.split("/"));if(/\d{1}_[a-z0-9]{8}$/.test(t[9])){const e="https://mediaspace.esri.com/embed/secure/iframe/entryId/".concat(t[9],"/uiConfId/49806163"),i=e.includes("?")?"&":"?";return"".concat(e).concat(i,"co3=true")}return""}navigationMain(e,t,i,a,s){!0===e?(t.setAttribute("attr-visible",!0),i.setAttribute("attr-visible",!1),a.classList.add("active")):(a.classList.add("active"),s.classList.remove("active"),t.setAttribute("attr-visible",!0),i.setAttribute("attr-visible",!1))}viewAction(e,t){null!==e&&e.length>0&&e.forEach(e=>{t?e.removeAttribute("attr-visible"):e.setAttribute("attr-visible",t)})}descendingSort(){return this.videoEntries.sort((e,t)=>{const i=new Date(e.createdAt);return new Date(t.createdAt)-i})}ascendingSort(){return this.videoEntries.sort((e,t)=>new Date(e.createdAt)-new Date(t.createdAt))}azSort(){return this.videoEntries.sort((e,t)=>{const i=e.name.toLowerCase(),a=t.name.toLowerCase();return i.localeCompare(a)})}zaSort(){return this.videoEntries.sort((e,t)=>{const i=e.name.toLowerCase();return t.name.toLowerCase().localeCompare(i)})}clearMediaViewBeforeSort(){esriClientUtils.select(".channel-playlist--media-grid").textContent=""}hideRevelanceDropdown(){const e=esriClientUtils.select(".channel-playlist--drop-down"),t=esriClientUtils.select(".channel-playlist--sort-container"),i=esriClientUtils.select(".channel-playlist__button-sort"),a=i.getAttribute("aria-expanded");e.classList.toggle("close"),t.classList.toggle("open"),"true"===a&&i.setAttribute("aria-expanded","false");e.querySelectorAll("a[href]").forEach(e=>{e.setAttribute("tabindex","-1")});const s=i.shadowRoot;setTimeout(()=>{const e=s.querySelector("button");i.setAttribute("tabindex","0"),e.focus()},300)}expandFilterDialog(e){const t=window.matchMedia("screen and (min-width: 767px)"),i=e.parentNode,a=i.parentNode,s=e.parentNode.parentNode.parentNode.parentNode,n=s.querySelector(".channel-playlist--filter-container-background"),r=s.querySelector(".channel-playlist--filter-container");t.matches||(i.classList.toggle("open"),n.setAttribute("style","block-size: ".concat(a.clientHeight+40,"px")),n.setAttribute("attr-height","".concat(a.clientHeight+40)),r.setAttribute("style","block-size: ".concat(a.clientHeight+40,"px")),r.setAttribute("attr-height","".concat(a.clientHeight+40)))}sortRelevanceOrder(e,t,i){const a=window.matchMedia("screen and (min-width: 767px)");let s=[];"descending"===e&&(t.innerHTML=a.matches?"Creation Date - Descending":"Descending",s=this.descendingSort()),"ascending"===e&&(t.innerHTML=a.matches?"Creation Date - Ascending":"Ascending",s=this.ascendingSort()),"az"===e&&(t.innerHTML=a.matches?"Alphabetically - A to Z":"A - Z",s=this.azSort()),"za"===e&&(t.innerHTML=a.matches?"Alphabetically - Z to A":"Z - A",s=this.zaSort()),"relevance"===e&&(s=this.defaultSortData.media[0].entries),this.clearMediaViewBeforeSort(),this.createMediaViews(i,s)}addClickToViews(e){const t=e.parentNode,i=esriClientUtils.select(".channel-container-media",t),a=esriClientUtils.select(".channel-playlist--media-grid",i),s=esriClientUtils.select(".channel-playlist__button-sort"),n=esriClientUtils.select(".channel-playlist__button-filter"),r=esriClientUtils.select(".channel-playlist--filter-container"),l=esriClientUtils.select(".channel-playlist--clear-all-container"),o=esriClientUtils.select(".channel-playlist--filter-container-background"),c=esriClientUtils.select(".channel-playlist--clear-all-container-background"),d=esriClientUtils.select(".channel-playlist--sort-container"),u=esriClientUtils.select(".channel-playlist--drop-down"),h=esriClientUtils.select(".channel-playlist__relevance"),m=esriClientUtils.select(".channel-playlist__descending-button"),p=esriClientUtils.select(".channel-playlist__ascending-button"),y=esriClientUtils.select(".channel-playlist__AZ-button"),b=esriClientUtils.select(".channel-playlist__ZA-button"),g=esriClientUtils.select(".channel-playlist__view-grid",t),v=esriClientUtils.select(".channel-playlist__view-list",t),f=esriClientUtils.selectAll(".channel-playlist__button-filter-expand",t),A=esriClientUtils.selectAll(".channel__filter-column-title-desktop"),C=esriClientUtils.select(".channel-playlist__more-filter-btn"),E=esriClientUtils.select(".channel-playlist__more-back-btn"),w=esriClientUtils.select(".channel-playlist__filter-row"),D=d.getAttribute("attr-sort-order");["descending","ascending","az","za"].includes(D)&&this.sortRelevanceOrder(D,s,e),A.forEach(e=>{e.addEventListener("click",()=>{this.expandFilterDialog(e)})}),f.forEach(e=>{e.addEventListener("click",()=>{this.expandFilterDialog(e)})}),C.addEventListener("click",()=>{const e=window.matchMedia("screen and (min-width: 767px)"),t=window.matchMedia("screen and (min-width: 1075px)");e.matches&&w.setAttribute("style","transform: translateX(-103%); overflow: visible"),t.matches&&w.setAttribute("style","transform: translateX(-98%); overflow: visible"),E.removeAttribute("disabled"),C.setAttribute("disabled","")}),E.addEventListener("click",e=>{e.preventDefault(),w.setAttribute("style","transform: translateX(1%); overflow: hidden"),C.removeAttribute("disabled"),E.setAttribute("disabled","")}),n.addEventListener("click",t=>{t.preventDefault();const i=r.getAttribute("aria-hidden"),a=n.getAttribute("aria-expanded"),s=esriClientUtils.selectAll("calcite-input-text"),d=s[0].hasAttribute("disabled"),u=window.matchMedia("screen and (min-width: 767px)"),h=esriClientUtils.selectAll("calcite-label"),m=h[0].getAttribute("tabindex"),p=o.getAttribute("aria-hidden"),y=esriClientUtils.select(".channel-playlist--pill-container"),b=null===r.getAttribute("attr-height")?370:r.getAttribute("attr-height"),g=esriClientUtils.select(".channel-playlist__more-clear-filter");let v=c.getAttribute("aria-hidden");y.classList.add("enabled"),g.addEventListener("click",()=>{this.searchProductValue="",this.searchYearValue="",this.removeCalcitePill(!1,e)}),s.length>0&&s.forEach(e=>{e.toggleAttribute("disabled",!d)}),h.length>0&&h.forEach(e=>{e.setAttribute("tabindex","-1"===m?null:"-1")}),u.matches||(v="false"),"false"===a?(n.setAttribute("aria-expanded","true"),n.setAttribute("icon-end","chevron-up"),l.setAttribute("aria-hidden","false"),setTimeout(()=>{h[0].setAttribute("tabindex","0"),h[0].focus()},250)):(l.setAttribute("aria-hidden","true"),n.setAttribute("aria-expanded","false"),n.setAttribute("icon-end","chevron-down")),c.setAttribute("aria-hidden","true"===v?"false":"true"),"true"===p?(o.setAttribute("aria-hidden","false"),o.setAttribute("style","block-size: ".concat(b,"px"))):(o.setAttribute("aria-hidden","true"),o.setAttribute("style","block-size: 0")),"true"===i?(r.setAttribute("aria-hidden","false"),r.setAttribute("style","block-size: ".concat(b,"px"))):(r.setAttribute("aria-hidden","true"),r.setAttribute("style","block-size: 0"))}),h.addEventListener("click",t=>{t.preventDefault(),this.sortRelevanceOrder("relevance",s,e),s.innerHTML="Relevance",this.hideRevelanceDropdown()}),m.addEventListener("click",t=>{t.preventDefault(),this.sortRelevanceOrder("descending",s,e)}),p.addEventListener("click",t=>{t.preventDefault(),this.sortRelevanceOrder("ascending",s,e)}),y.addEventListener("click",t=>{t.preventDefault(),this.sortRelevanceOrder("az",s,e)}),b.addEventListener("click",t=>{t.preventDefault(),this.sortRelevanceOrder("za",s,e)}),s.addEventListener("click",()=>{this.sortDropdownToggle(u,d,s)}),document.addEventListener("click",e=>{e.target!==s&&d.classList.contains("open")&&this.hideRevelanceDropdown()}),g.addEventListener("click",e=>{e.preventDefault();const t=esriClientUtils.selectAll(".channel-playlist--column",a),i=esriClientUtils.selectAll(".channel-playlist--media-row",a);esriClientUtils.select(".channel-playlist--media-grid").setAttribute("attr-view-type","grid"),this.viewAction(t,!0),this.viewAction(i,!1)}),v.addEventListener("click",e=>{e.preventDefault();const t=esriClientUtils.selectAll(".channel-playlist--column",a),i=esriClientUtils.selectAll(".channel-playlist--media-row",a);esriClientUtils.select(".channel-playlist--media-grid").setAttribute("attr-view-type","detailed"),this.viewAction(t,!1),this.viewAction(i,!0)})}sortDropdownToggle(e,t,i){e.classList.toggle("close"),t.classList.toggle("open");"true"===i.getAttribute("aria-expanded")?(i.setAttribute("aria-expanded","false"),i.setAttribute("appearance","transparent")):(i.setAttribute("aria-expanded","true"),i.setAttribute("appearance","solid"));e.querySelectorAll("a[href]").forEach(e=>{const t=e.getAttribute("tabindex");e.setAttribute("tabindex","-1"===t?"0":"-1")})}addCountTotalToMedia(e,t){const i=esriClientUtils.select(".channel-container",t),a=esriClientUtils.select("[attr-media]",i),s=esriClientUtils.select("#home-description"),n=esriClientUtils.select("#media-description");a.innerHTML="".concat(e," Videos"),s.innerHTML="Playlist View ".concat(this.playlistsCount," Playlists"),n.innerHTML="Media View ".concat(e," Videos")}addClickToNav(e){const t=esriClientUtils.select(".channel-container",e),i=esriClientUtils.select(".channel-container-home",t),a=esriClientUtils.select(".channel-container-media",t),s=esriClientUtils.select("[attr-home]",t),n=esriClientUtils.select("[attr-media]",t);this.addCountTotalToMedia(this.channelObj.totalChannelVideoCount,e),s&&s.addEventListener("click",e=>{e.preventDefault();const t=i.getAttribute("attr-visible");this.navigationMain(t,i,a,s,n)}),n&&n.addEventListener("click",e=>{e.preventDefault();const t=a.getAttribute("attr-visible");this.navigationMain(t,a,i,n,s)})}createChannelNPlaylist(e){const t={},i=e.media[0];this.playlistsCount=e.media[0].playlists.length,e.success&&(t.channelID=i.id,this.channelObj=t,this.playlistsObj=e.media[0].playlists)}keyboardSensor(e){const t=this.createElmt("div",{classes:["channel-playlist--keyboard-sensor"],attributes:{tabindex:"0","aria-label":"Load more videos","aria-live":"polite","aria-atomic":"true","aria-relevant":"additions",role:"alert"}});e.appendChild(t),t.addEventListener("focus",()=>{const t=esriClientUtils.selectAll(".channel-playlist--column",e),i=this.videoEntries,a=esriClientUtils.select(".channel-playlist--container-loader"),s=this.mediaEndIndexGrid;this.buildMediaContent(t,i,a,!0),setTimeout(()=>{esriClientUtils.selectAll(".channel-playlist--column",e).forEach(e=>{const t=e.querySelector("a");s==t.getAttribute("attr-video-id")&&(t.setAttribute("tabindex","0"),t.focus())})},100)})}attachedLoader(e){const t=this.createElmt("div",{classes:["channel-playlist--container-loader"]}),i=this.createElmt("calcite-loader",{attributes:{text:"Loading...","aria-label":"Loading",active:"true",type:"indeterminate",scale:"m"}});t.appendChild(i),e.appendChild(t)}buildMediaContent(e,t,i,a){this.mediaEndIndexGrid=this.mediaStartIndexGrid+this.videosMaxPage,this.mediaEndIndexGrid=this.mediaEndIndexGrid>t.length?t.length:this.mediaEndIndexGrid;const s=esriClientUtils.select(".channel-playlist--media-grid"),n=s.getAttribute("attr-view-type"),r="grid"===n,l="detailed"===n;if(t.length>0){for(let a=this.mediaStartIndexGrid;a<this.mediaEndIndexGrid;a++){const s="".concat(t[a].thumbnailUrl,"/width/536/height/301"),n=t[a].dataUrl,o=t[a].hasOwnProperty("duration")?t[a].duration:"00",c=t[a].hasOwnProperty("name")?t[a].name:"N/A",d=t[a].hasOwnProperty("description")?t[a].description:"",u=t[a].hasOwnProperty("userId")?t[a].userId:"",h=t[a].hasOwnProperty("createdAt")?t[a].createdAt:"YYYYY-MM-DD",m=this.createElmt("div",{classes:["channel-playlist--column"],attributes:{"attr-visible":r,"data-animation":"calcite-animate__in-up"}}),p=this.createElmt("a",{classes:["channel-playlist__videoURL"],attributes:{href:this.convertToMedia(n),"data-entryId":this.getEntryId(n),role:"button","aria-label":c,tabindex:"0","attr-video-id":a,"data-modal":"true","data-url":this.convertToMedia(n)}}),y=this.createElmt("img",{classes:["channel-playlist__img"],attributes:{loading:"lazy",alt:c,src:s}});p.appendChild(y);const b=this.createElmt("div",{classes:["channel-playlist--metadata"],attributes:{tabindex:"-1"}}),g=this.createElmt("div",{classes:["channel-playlist__meta-title"],attributes:{tabindex:"-1"}});g.innerHTML=c;const v=this.createElmt("div",{classes:["channel-playlist__meta-duration"],attributes:{tabindex:"-1"}});v.innerHTML=this.convertTimeFormat(o),b.appendChild(g),b.appendChild(v),p.appendChild(b);const f=this.createElmt("div",{classes:["channel-playlist__media-row-thumbnail"]}),A=this.createElmt("div",{classes:["channel-playlist--media-row","calcite-animate","calcite-animate__in-up"],attributes:{"attr-visible":l,"data-animation":"calcite-animate__in-up"}}),C=this.createElmt("a",{classes:["channel-playlist__media-videoURL"],attributes:{href:this.convertToMedia(n),"data-entryId":this.getEntryId(n),"aria-label":c,"data-modal":"true","attr-video-id":a,"data-url":this.convertToMedia(n)}}),E=this.createElmt("img",{classes:["channel-playlist__img"],attributes:{loading:"lazy",alt:c,src:s}}),w=this.createElmt("div",{classes:["channel-playlist__media-description"]});w.innerHTML=d.match(/^[^\.!?]*[\.!?]/);const D=this.createElmt("div",{classes:["channel-playlist__media-duration"]});D.innerHTML=this.convertTimeFormat(o),C.appendChild(E),C.appendChild(D),f.appendChild(C),A.appendChild(f);const L=this.createElmt("div",{classes:["channel-playlist__media-row-content"]}),k=this.createElmt("a",{classes:["channel-playlist__media-title"],attributes:{href:this.convertToMedia(n),"data-entryId":this.getEntryId(n),"aria-label":c,"data-modal":"true","data-url":this.convertToMedia(n)}});k.innerHTML=c;const S=this.createElmt("div",{classes:["channel-playlist__media-from"]});if(S.innerHTML="From ".concat(u," ").concat(h.split(" ")[0]),L.appendChild(k),L.appendChild(S),L.appendChild(w),A.appendChild(L),m.appendChild(p),void 0!==i){const e=esriClientUtils.select(".channel-playlist--keyboard-sensor");e.before(m),e.before(A)}else e.appendChild(m),e.appendChild(A)}if(this.mediaStartIndexGrid=this.mediaEndIndexGrid,this.mediaEndIndexGrid>=t.length&&t.length>this.videosMaxPage){const e=esriClientUtils.select(".channel-playlist--container-loader",s),t=esriClientUtils.select(".channel-playlist--keyboard-sensor",s),i=esriClientUtils.select(".channel-playlist__result-msg",s);null!==i&&i.parentNode.removeChild(i),null!==e&&(e.parentNode.removeChild(e),t.parentNode.removeChild(t))}}}resetIndex(){this.mediaEndIndexGrid=0,this.mediaStartIndexGrid=0}createMediaViews(e,t){void 0===e&&(e=esriClientUtils.select(".channel-container-home"));const i=t,a=esriClientUtils.select(".channel-container-media"),s=esriClientUtils.select(".channel-playlist--media-grid",a);if(this.resetIndex(),this.resetDateBoxes(),this.mediaEndIndexGrid=i.length>this.videosMaxPage?this.videosMaxPage:i.length,this.buildMediaContent(s,i),a.appendChild(s),i.length>this.videosMaxPage){this.keyboardSensor(s),this.attachedLoader(s);const e=esriClientUtils.select("calcite-loader",s),t=e.parentNode;new IntersectionObserver(e=>{e.forEach(e=>{e.isIntersecting&&this.buildMediaContent(s,i,t,!0)})},{root:null,threshold:.8}).observe(e)}}createElmt(e){let t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{};const i=document.createElement(e);return t.classes&&i.classList.add(...t.classes),t.attributes&&Object.entries(t.attributes).forEach(e=>{let[t,a]=e;i.setAttribute(t,a)}),i}getEntryId(e){const t=Object.values(e.split("/"));return/\d{1}_[a-z0-9]{8}$/.test(t[9])?t[9]:""}createHomeView(e){const t=e,i=esriClientUtils.select(".channel-playlist--container",t),a=esriClientUtils.select("calcite-loader");null!==a&&i.removeChild(a);for(let e=0;e<this.playlistsCount;e++)if(!this.playlistsObj[e].isEmpty){const i=this.playlistsObj[e].media,a=this.playlistsObj[e].media.length,s=this.playlistsObj[e].name,n=this.createElmt("div",{classes:["channel-playlist--content"]}),r=this.createElmt("div",{classes:["channel-playlist--container"]}),l=this.createElmt("div",{classes:["channel-playlist__title"]});l.innerHTML=s,r.appendChild(l);const o=this.createElmt("div",{classes:["channel-playlist--slider-box"],attributes:{"data-slider-count":a,style:"transform: translateX(1%);"}});for(let e=0;e<a;e++){const t="".concat(i[e].thumbnailUrl,"/width/536/height/301"),a=i[e].dataUrl,s=i[e].duration,n=i[e].name,r=this.createElmt("div",{classes:["channel-playlist--column"]}),l=this.createElmt("a",{classes:["channel-playlist__videoURL"],attributes:{href:this.convertToMedia(a),"data-entryId":this.getEntryId(a),role:"button","aria-label":n,tabindex:"0","data-modal":"true","data-url":this.convertToMedia(a)}}),c=this.createElmt("img",{classes:["channel-playlist__img"],attributes:{loading:"lazy",alt:n,src:t}});l.appendChild(c);const d=this.createElmt("div",{classes:["channel-playlist--metadata"],attributes:{tabindex:"-1"}}),u=this.createElmt("div",{classes:["channel-playlist__meta-title"],attributes:{tabindex:"-1"}});u.innerHTML=n;const h=this.createElmt("div",{classes:["channel-playlist__meta-duration"],attributes:{tabindex:"-1"}});h.innerHTML=this.convertTimeFormat(s),d.appendChild(u),d.appendChild(h),l.appendChild(d),r.appendChild(l),o.appendChild(r)}r.appendChild(o);const c=this.createElmt("calcite-icon",{classes:["channel-playlist__prev"],attributes:{"aria-label":"previous",scale:"l",appearance:"transparent",icon:"chevron-left"}}),d=this.createElmt("calcite-icon",{classes:["channel-playlist__next"],attributes:{"aria-label":"next",scale:"l",appearance:"transparent",icon:"chevron-right"}});n.appendChild(r),n.appendChild(c),n.appendChild(d),t.appendChild(n),this.buildSwitcherControls(n)}}buildSwitcherControls(e){let t=!0,i=!!window.matchMedia("(min-width:720px)").matches,a=!!window.matchMedia("(min-width:1040px)").matches;if(void 0!==e){const s=esriClientUtils.select(".channel-playlist--slider-box",e),n=esriClientUtils.select(".channel-playlist__prev",e),r=esriClientUtils.select(".channel-playlist__next",e),l=s.getAttribute("data-slider-count");let o=0,c=0,d=0;a?(o=33.33,c=3):i?(o=50,c=2):(o=100,c=1),0===d&&n.setAttribute("style","display: none"),a.matches&&l<=3&&(n.setAttribute("style","display: none"),r.setAttribute("style","display: none")),i.matches&&l<=2&&n.setAttribute("style","display: none"),t.matches&&l<=1&&n.setAttribute("style","display: none"),n.addEventListener("click",()=>{d>0&&(d--,s.style.transform="translateX(-".concat(d*o,"%)"),r.removeAttribute("style")),0===d&&n.setAttribute("style","display: none")}),r.addEventListener("click",()=>{d<l-c&&(d++,s.style.transform="translateX(-".concat(d*o,"%)"),n.removeAttribute("style")),d===l-c&&r.setAttribute("style","display: none")})}}clearThumbs(){esriClientUtils.select(".channel-playlist--media-grid").innerHTML=""}zeroMsgResult(e){const t=esriClientUtils.select(".channel-playlist--media-grid");let i=null;e?(i=this.createElmt("div",{classes:["channel-playlist__result-msg"]}),i.innerHTML="no results available",t.appendChild(i)):(i=esriClientUtils.select(".channel-playlist__result-msg",t),null!==i&&i.parentNode.removeChild(i))}resetDurationBoxes(){const e=esriClientUtils.selectAll("input[attr-duration-value]");!1===this.searchDuration[1]&&!1===this.searchDuration[2]&&!1===this.searchDuration[3]&&(this.searchDuration[0]=!0,e[0].checked=!0,e[1].checked=!1,e[2].checked=!1,e[3].checked=!1)}resetAnyDate(e,t){e.classList.add("hidden"),this.searchDate[0]=!0,this.searchDate[1]=!1,this.searchDate[2]=!1,this.searchDate[3]=!1,t[0].checked=!0,t[1].checked=!1,t[2].checked=!1,t[3].checked=!1}resetDateBoxes(e){const t=esriClientUtils.selectAll("input[attr-date-value]"),i=esriClientUtils.select("input#rangePickerAfter"),a=esriClientUtils.select("input#rangePickerBefore"),s=esriClientUtils.select(".channel__datepicker-container"),n=document.querySelector(".channel-playlist--pill-container"),r=n.querySelectorAll("calcite-chip");"after"===e&&""===a.value&&(r.forEach(e=>{e.hasAttribute("attr-after")&&(i.value="",n.removeChild(e))}),this.resetAnyDate(s,t)),"before"===e&&""===i.value&&(r.forEach(e=>{e.hasAttribute("attr-before")&&(a.value="",n.removeChild(e))}),this.resetAnyDate(s,t)),!1===this.searchDate[1]&&!1===this.searchDate[2]&&!1===this.searchDate[3]&&(this.searchDate[0]=!0,t[0].checked=!0,t[1].checked=!1,t[2].checked=!1,t[3].checked=!1)}removeCalcitePill(e){const t=document.querySelector(".channel-playlist--pill-container");t.querySelectorAll("calcite-chip").forEach(i=>{i.getAttribute("value")===e&&t.removeChild(i)})}uncheckFilterBoxALL(e,t){document.querySelector(".channel-playlist--pill-container").querySelectorAll("calcite-chip");if("expert"===t){esriClientUtils.select("input[attr-expert-value='".concat(e,"'")).checked=!1,this.removeCalcitePill(e)}if("term"===t){esriClientUtils.select("input[attr-term-value='".concat(e,"'")).checked=!1,this.removeCalcitePill(e)}if("duration"===t){const t=esriClientUtils.select("input[attr-duration-value='".concat(e,"'"));t.checked=!1,this.searchDuration[t.getAttribute("attr-duration")]=!1,this.resetDurationBoxes(),this.removeCalcitePill(e)}if("date"===t){const t=esriClientUtils.select("input[attr-date-value='".concat(e,"'"));t.checked=!1,this.searchDate[t.getAttribute("attr-date")]=!1,this.resetDateBoxes(),this.removeCalcitePill(e)}"after"===t&&this.resetDateBoxes("after"),"before"===t&&this.resetDateBoxes("before")}normalizeFilterStringDEL(e){return this.filterVALUE=this.filterVALUE.replace(e,""),this.filterVALUE=this.filterVALUE.trim().replaceAll(" +"," "),this.filterVALUE}normalizeFilterStringADD(e){return this.filterVALUE+=" ".concat(e),this.filterVALUE=this.filterVALUE.trim().replaceAll(" +"," "),this.filterVALUE}activateCloseChip(e,t){const i=esriClientUtils.select("calcite-input-text#year"),a=esriClientUtils.select("calcite-input-text#product"),s=esriClientUtils.select("input#rangePickerAfter"),n=esriClientUtils.select("input#rangePickerBefore");e.addEventListener("click",r=>{const l=e.getAttribute("value");"expert"===r.target.getAttribute("attr-type")&&this.uncheckFilterBoxALL(l,"expert"),"term"===e.getAttribute("attr-type")&&this.uncheckFilterBoxALL(l,"term"),"duration"===e.getAttribute("attr-type")&&this.uncheckFilterBoxALL(l,"duration"),"date"===e.getAttribute("attr-type")&&this.uncheckFilterBoxALL(l,"date"),e.hasAttribute("attr-after")&&(s.value="",this.uncheckFilterBoxALL(l,"after")),e.hasAttribute("attr-before")&&(n.value="",this.uncheckFilterBoxALL(l,"before")),e.hasAttribute("attr-product")&&(this.searchProductValue="",a.value="",this.productVersion=!1),e.hasAttribute("attr-year")&&(this.searchYearValue="",i.value="",this.searchYear=!1),this.filterVALUE=this.normalizeFilterStringDEL(l),this.searchStrGlobal.value=this.filterVALUE,this.displaySearchResults(t)})}uncheckAllBoxes(e){const t=esriClientUtils.selectAll("input[attr-expert-value]"),i=esriClientUtils.selectAll("input[attr-term-value"),a=esriClientUtils.selectAll("input[attr-duration-value]"),s=esriClientUtils.selectAll("input[attr-date-value]"),n=esriClientUtils.select(".channel__datepicker-container"),r=esriClientUtils.select("calcite-input-text#year"),l=esriClientUtils.select("calcite-input-text#product");t.forEach(e=>{e.checked=!1}),i.forEach(e=>{e.checked=!1}),a.forEach(e=>{e.checked=!1}),s.forEach(e=>{e.checked=!1}),a[0].checked=!0,this.searchDuration[0]=!0,this.searchDuration[1]=!1,this.searchDuration[2]=!1,this.searchDuration[3]=!1,s[0].checked=!0,this.searchDate[0]=!0,this.searchDate[1]=!1,this.searchDate[2]=!1,this.searchDate[3]=!1,n.classList.add("hidden"),r.value="",l.value="",this.filterVALUE="",this.searchStrGlobal.value="",this.displaySearchResults(e)}removeCalcitePill(e,t){const i=document.querySelector(".channel-playlist--pill-container");i.querySelectorAll("calcite-chip").forEach(t=>{const a=t.getAttribute("value");a===e&&i.removeChild(t),e||i.removeChild(t),"all durations"===e&&-1!==a.indexOf("min")&&i.removeChild(t),"all dates"===e&&-1!==a.indexOf("last")&&i.removeChild(t),"after"===e&&-1!==a.indexOf("Created After")&&i.removeChild(t),"before"===e&&-1!==a.indexOf("Created Before")&&i.removeChild(t)}),e||(this.clearThumbs(),this.uncheckAllBoxes(t))}sanitizeInput(e){const t=document.createElement("div");return t.textContent=e,t.innerHTML}appendChip(e,t,i){const a=document.querySelector(".channel-playlist--pill-container"),s=a.querySelectorAll("calcite-chip"),n=document.createElement("calcite-chip");let r=!1;s.length>0&&s.forEach(t=>{t.getAttribute("value")===e&&(r=!0)}),r||(n.innerHTML=this.sanitizeInput(e),this.setAttributes(n,{"attr-type":t,value:e,closable:"",appearance:"solid",kind:"neutral",scale:"m","calcite-hydrated":""}),a.appendChild(n),this.activateCloseChip(n,i),["attr-after","attr-before"].includes(t)&&this.displaySearchResults(i))}addCalcitePillSearchProduct(e,t){this.productVersion||(this.productVersion=!0,this.appendChip(e,"attr-product",t))}addCalcitePillSearchYear(e,t){this.searchYear||(this.searchYear=!0,this.appendChip(e,"attr-year",t))}addCalcitePill(e,t,i){"expert"===t&&this.appendChip(e,"expert",i),"duration"===t&&this.appendChip(e,"duration",i),"term"===t&&this.appendChip(e,"term",i),"date"===t&&this.appendChip(e,"date",i)}convertDateFormat(e){const t=e.split("-"),i=t[0],a=t[1],s=t[2];return"".concat(a,"/").concat(s,"/").concat(i)}getCurrentDateMMDDYYYY(){const e=new Date,t=(e.getMonth()+1).toString().padStart(2,"0"),i=e.getDate().toString().padStart(2,"0"),a=e.getFullYear();return"".concat(t,"/").concat(i,"/").concat(a)}getLastDays(e){const t=new Date(e),i=new Date;if(t.getTime()>i.getTime())return 0;const a=i.getTime()-t.getTime();return Math.floor(a/864e5)}getDatesFromPicker(e){const t=document.querySelector("#rangePickerAfter"),i=document.querySelector("#rangePickerBefore");t.value="",i.value="",t.addEventListener("change",t=>{this.searchDate[4]=!0,this.removeCalcitePill("after",e);const i=this.convertDateFormat(t.target.value);0!==i.indexOf("undefined")?(this.afterDays=this.getLastDays(i),this.appendChip("Created After: ".concat(i),"attr-after",e)):(this.afterDays=this.getLastDays("01/01/1901"),this.displaySearchResults(e))}),i.addEventListener("change",t=>{this.searchDate[4]=!0,this.removeCalcitePill("before",e);const i=this.convertDateFormat(t.target.value);0!==i.indexOf("undefined")?(this.beforeDays=this.getLastDays(i),this.appendChip("Created Before: ".concat(i),"attr-before",e)):(this.beforeDays=this.getLastDays(this.getCurrentDateMMDDYYYY()),this.displaySearchResults(e))})}customDatePicker(e,t){const i=document.querySelector(".channel__datepicker-container");e?(i.classList.remove("hidden"),this.getDatesFromPicker(t)):i.classList.add("hidden")}enableFilterSearch(e){const t=esriClientUtils.selectAll("input[attr-expert-value]"),i=esriClientUtils.selectAll("input[attr-duration-value]"),a=esriClientUtils.selectAll("input[attr-term-value]"),s=esriClientUtils.select("calcite-input-text#product"),n=esriClientUtils.select("calcite-input-text#year"),r=document.querySelectorAll("input[attr-date-value]");s.addEventListener("keyup",t=>{this.addCalcitePillSearchProduct("Product Version",e),"Enter"===t.key&&(this.searchProductValue=s.value.toLowerCase(),this.displaySearchResults(e))}),n.addEventListener("keyup",t=>{this.addCalcitePillSearchYear("Search Year",e),"Enter"===t.key&&(this.searchYearValue=n.value,this.displaySearchResults(e))}),r[0].checked=!0,r.forEach((t,i)=>{t.addEventListener("click",()=>{const a=t.getAttribute("attr-date-value");!1===this.searchDate[i]?(this.searchDate[i]=!0,this.searchDate[0]=!1,this.searchDate[3]=!1,r[0].checked=!1,r[3].checked=!1,0!==i&&3!==i&&(this.addCalcitePill(a,"date",e),this.customDatePicker(!1,e),this.removeCalcitePill("after",e),this.removeCalcitePill("before",e))):(this.searchDate[i]=!1,this.removeCalcitePill(a,e)),0===i&&(this.searchDate[0]=!0,this.searchDate[1]=!1,this.searchDate[2]=!1,this.searchDate[3]=!1,r[0].checked=!0,r[1].checked=!1,r[2].checked=!1,r[3].checked=!1,this.removeCalcitePill("all dates",e),this.removeCalcitePill("after",e),this.removeCalcitePill("before",e),this.customDatePicker(!1,e)),3===i&&(this.searchDate[0]=!1,this.searchDate[1]=!1,this.searchDate[2]=!1,this.searchDate[3]=!0,r[0].checked=!1,r[1].checked=!1,r[2].checked=!1,r[3].checked=!0,this.afterDays=this.getLastDays("01/01/1901"),this.beforeDays=this.getLastDays(this.getCurrentDateMMDDYYYY()),this.removeCalcitePill("all dates",e),this.customDatePicker(!0)),this.searchStrGlobal.value=this.filterVALUE,this.displaySearchResults(e)})}),i[0].checked=!0,i.forEach((t,a)=>{t.addEventListener("click",()=>{const s=t.getAttribute("attr-duration-value");!1===this.searchDuration[a]?(this.searchDuration[a]=!0,this.searchDuration[0]=!1,0!==a&&this.addCalcitePill(s,"duration",e),i[0].checked=!1):(this.searchDuration[a]=!1,this.removeCalcitePill(s,e)),0===a&&(this.searchDuration[0]=!0,this.searchDuration[1]=!1,this.searchDuration[2]=!1,this.searchDuration[3]=!1,i[0].checked=!0,i[1].checked=!1,i[2].checked=!1,i[3].checked=!1,this.removeCalcitePill("all durations",e)),!1===this.searchDuration[1]&&!1===this.searchDuration[2]&&!1===this.searchDuration[3]&&(this.searchDuration[0]=!0,i[0].checked=!0),this.searchStrGlobal.value=this.filterVALUE,this.displaySearchResults(e)})}),t.forEach((t,i)=>{t.addEventListener("click",()=>{const i=t.getAttribute("attr-expert-value");!0===t.checked?this.addCalcitePill(i,"expert",e):this.removeCalcitePill(i,e)})}),a.forEach(t=>{t.addEventListener("click",()=>{const i=t.getAttribute("attr-term-value");!0===t.checked?(this.filterVALUE=this.normalizeFilterStringADD(i),this.addCalcitePill(i,"term",e)):(this.filterVALUE=this.normalizeFilterStringDEL(i),this.removeCalcitePill(i,e)),this.searchStrGlobal.value=this.filterVALUE,this.displaySearchResults(e)})})}removeExtraSpacing(e){return e.replace(/\s\s+/g," ")}getCurrentDate(){return(new Date).toLocaleDateString("en-US",{month:"short",day:"2-digit",year:"numeric"})}getDateDaysAgo(e){const t=new Date,i=new Date(t);i.setDate(t.getDate()-e);return i.toLocaleDateString("en-US")}isPlaylistDateRecent(e,t){const i=e.split("/"),a=t.split("-"),s=new Date(i[2],i[0]-1,i[1]);return new Date(a[0],a[1]-1,a[2])>s}isPlaylistDateOlder(e,t){const i=e.split("/"),a=t.split("-"),s=new Date(i[2],i[0]-1,i[1]);return new Date(a[0],a[1]-1,a[2])<=s}initializePlaylist(e){e.length>0&&e.forEach(e=>{e.matchesQuery=!1})}displaySearchResults(e){void 0===e&&(e=esriClientUtils.select(".channel-container-home"));const t=this.search(this.searchStrGlobal);this.clearThumbs(),t.length<=0&&this.zeroMsgResult(!0),this.addCountTotalToMedia(t.length,e),this.createMediaViews(e,t)}search(e){let t="01/01/1901",i=this.getLastDays(this.getCurrentDateMMDDYYYY()),a=!1,s=!1;const n=/\. |, |! |,| |  /,r=this.videoEntries;this.initializePlaylist(r);const l=this.removeExtraSpacing(e.value.toLowerCase());r.forEach((e,t)=>{const i=e.searchText.split(n);l.split(n).filter(e=>{i.filter(i=>{i.toLowerCase().includes(e)&&(r[t].matchesQuery=!0)})})}),r.forEach(e=>{e.matchesQuery&&this.searchDuration[1]&&this.searchDuration[2]&&!this.searchDuration[3]&&e.duration<=3600&&e.duration>0&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDuration[1]&&this.searchDuration[2]&&!this.searchDuration[3]&&e.duration>3600&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDuration[2]&&this.searchDuration[3]&&!this.searchDuration[1]&&e.duration>1800&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDuration[2]&&this.searchDuration[3]&&!this.searchDuration[1]&&e.duration<=1800&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDuration[1]&&!this.searchDuration[2]&&!this.searchDuration[3]&&e.duration<=1800&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDuration[1]&&!this.searchDuration[2]&&!this.searchDuration[3]&&e.duration>1800&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDuration[2]&&!this.searchDuration[1]&&!this.searchDuration[3]&&e.duration<=3600&&e.duration>1800&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDuration[2]&&!this.searchDuration[1]&&!this.searchDuration[3]&&e.duration<1800&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDuration[2]&&!this.searchDuration[1]&&!this.searchDuration[3]&&e.duration>3600&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDuration[3]&&!this.searchDuration[1]&&!this.searchDuration[2]&&e.duration>3600&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDuration[3]&&!this.searchDuration[1]&&!this.searchDuration[2]&&e.duration<=3600&&(e.matchesQuery=!1);const n=e.createdAt.split(" ")[0];this.searchDate[1]&&(t=this.getDateDaysAgo(7)),this.searchDate[2]&&(t=this.getDateDaysAgo(30)),this.searchDate[3]&&(t=this.getDateDaysAgo(this.afterDays),i=this.getDateDaysAgo(this.beforeDays),s=this.isPlaylistDateOlder(i,n)),a=this.isPlaylistDateRecent(t,n),e.matchesQuery&&this.searchDate[1]&&a&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDate[1]&&!a&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDate[2]&&a&&(e.matchesQuery=!0),e.matchesQuery&&this.searchDate[2]&&!a&&(e.matchesQuery=!1),e.matchesQuery&&this.searchDate[3]&&a&&s&&(e.matchesQuery=!0),!e.matchesQuery||!this.searchDate[3]||a&&s||(e.matchesQuery=!1)}),this.searchProductValue.length>0&&r.forEach(e=>{const t=e.tags;e.matchesQuery&&-1!==t.indexOf(this.searchProductValue)&&(e.matchesQuery=!0),e.matchesQuery&&-1===t.indexOf(this.searchProductValue)&&(e.matchesQuery=!1)}),this.searchYearValue.length>0&&r.forEach(e=>{const t=e.createdAt.split("-");e.matchesQuery&&-1!==t[0].indexOf(this.searchYearValue)&&(e.matchesQuery=!0),e.matchesQuery&&-1===t[0].indexOf(this.searchYearValue)&&(e.matchesQuery=!1)});return r.filter(e=>!0===e.matchesQuery)}enableSearch(e){let t=[];const i=esriClientUtils.select("#channel-playlist__searchInput"),a=esriClientUtils.select(".channel-playlist__search-icon"),s=esriClientUtils.select(".channel-playlist__search-close"),n=esriClientUtils.select(".channel-playlist__close-container");s.addEventListener("click",t=>{t.preventDefault();const a=this.videoEntries;i.value="",this.addCountTotalToMedia(a.length,e),this.createMediaViews(e,a),this.zeroMsgResult(!0)}),n.addEventListener("keyup",t=>{t.preventDefault();const a=this.videoEntries;i.value="",this.addCountTotalToMedia(a.length,e),this.zeroMsgResult(!0)}),a.addEventListener("click",()=>{t=this.search(i),this.clearThumbs(),t.length<=0&&this.zeroMsgResult(!1),this.addCountTotalToMedia(t.length,e),this.createMediaViews(e,t)}),i.addEventListener("keyup",a=>{"Enter"===a.key&&(t=this.search(i),this.clearThumbs(),t.length<=0&&this.zeroMsgResult(!1),this.addCountTotalToMedia(t.length,e),this.createMediaViews(e,t))})}networkMsg(e){const t=esriClientUtils.select(".channel-container"),i=this.createElmt("div",{classes:["channel-playlist--column"]});i.innerHTML=e,t.appendChild(i);const a=esriClientUtils.select(".channel-playlist--container"),s=esriClientUtils.select("calcite-loader",a);null!==s&&null!==a&&a.removeChild(s)}getCurrentEnv(e){const t=window.location.host;let i="";return[{regex:/localhost/,strg:"dev"},{regex:/-e698666/,strg:"dev"},{regex:/-e698665/,strg:"stg"},{regex:/uat\./,strg:"stg"}].forEach(a=>{a.regex.test(t)&&e&&(i="dev"===a.strg?"dev":a.strg),a.regex.test(t)&&!e&&(i="dev"===a.strg?"stg":a.strg)}),i}setAttributes(e,t){for(var i in t)e.setAttribute(i,t[i])}getChannelObj(e){return new Promise((t,i)=>{const a="https://kaltura-api-proxy.esri.com/getChannel?channelId=".concat(e);fetch(a,{method:"GET",headers:{Accept:"application/json","Content-Type":"application/json;charset=UTF-8","Accept-Encoding":"gzip, deflate, br"}}).then(e=>{if(!e.ok)throw new Error("Network response was not ok");return e.json()}).then(e=>{t(e)}).catch(e=>{console.warn("Unable to retrieve Kaltura data. Check VPN connection or",e),i(e)})})}}document.querySelectorAll(".channel-container-home").forEach(t=>{new e(t)})}),document.addEventListener("DOMContentLoaded",function(){class e{constructor(){this.isFullScrn=this.isDocType=!1,this.bodyNode=document.querySelector("body");const e=esriClientUtils.selectAll('[data-modal="true"]');this.bodyTag=document.getElementsByTagName("body")[0],this.htmlTag=document.getElementsByTagName("html")[0],"undefined"!=typeof KalturaAnalytics?this.analytics=new KalturaAnalytics:(this.analytics=null,console.warn("KalturaAnalytics is not defined. Analytics will be disabled for this modal.")),this.returnElement=null,this.playBtn=null,this.identifyTriggers(),e&&e.length&&this.createModal(e)}observeModal(e){new MutationObserver((t,i)=>{const a=e.querySelector("button.playkit-pre-playback-play-button");a&&(a.click(),i.disconnect())}).observe(e,{childList:!0,subtree:!0})}closeModal(e,t,i){const a=window.matchMedia("(min-inline-size: 1024px)"),s=this;if(s.bodyNode.contains(e)){const i=document.querySelector(".co3-modal");new Promise(t=>{setTimeout(()=>{null!==e&&i.parentNode&&i.parentNode.removeChild(i),t()},200)}).then(()=>{s.bodyNode.setAttribute("aria-hidden","false"),s.bodyNode.setAttribute("tabindex",0);const e=s.bodyNode.querySelector("[data-return-btn]");t&&null!==e&&(e.removeAttribute("data-return-btn"),e.setAttribute("tabindex",-1),t.setAttribute("tabindex",-1),t.setAttribute("aria-hidden","true"),t.focus())}).catch(e=>{console.warn("Error Close Modal",e)})}null!==i&&i.length>0&&i.forEach(e=>{const t=esriClientUtils.select("video",e);if(null!==t&&t.hasAttribute("data-video-src")&&(t.src=this.sanitizeInput(t.getAttribute("data-video-src")),t.addEventListener("loadeddata",()=>{a.matches&&t.play()})),null!==e.lastElementChild){const t=esriClientUtils.select("video",e.lastElementChild);null!==t&&t.hasAttribute("data-video-src")&&(t.src=this.sanitizeInput(t.getAttribute("data-video-src")),t.addEventListener("loadeddata",()=>{a.matches&&t.play()}))}}),this.bodyTag=document.getElementsByTagName("body")[0],this.htmlTag=document.getElementsByTagName("html")[0],this.bodyTag.removeAttribute("style"),this.htmlTag.removeAttribute("style"),this.bodyTag.removeAttribute("aria-hidden","true"),this.bodyTag.removeAttribute("tabindex","0")}sanitizeInput(e){const t=document.createElement("div");return t.textContent=e,t.innerHTML}identifyTriggers(){const e=esriClientUtils.selectAll("[data-modal='true']");e&&e.length&&e.forEach(e=>{const t=e.getAttribute("href"),i=new RegExp("co3=true"),a=new RegExp("modal=true"),s=new RegExp("lang=true"),n=new RegExp(/\?/);if(!(i.test(t)||a.test(t)||s.test(t)||e.hasAttribute("data-href"))){const i=n.test(t)?"&":"?",a="".concat(t).concat(i,"co3=true");e.setAttribute("href",a)}if(e.hasAttribute("data-href")){const t=e.getAttribute("data-href"),i=n.test(t)?"&":"?",a="".concat(t).concat(i,"co3=true");e.setAttribute("data-href",a)}});const t=esriClientUtils.selectAll("a.esri-ui-button[data-href*='co3=true'],a.esri-text-button[data-href*='co3=true'],a[href*='co3=true'],a[href*='co3=full'],a[href*='co3'],.calcite-button-wrapper[data-href*='co3'],a[href*='modal=true'],.calcite-button-wrapper[data-href*='modal'],calcite-link[href*='co3'], calcite-button[href*='co3']"),i=esriClientUtils.selectAll("a[href*='lang=true']");i&&i.length&&i.forEach(e=>{const t=e.getAttribute("href");if(t){const i=/lang=true/i.test(t),a=t.indexOf("?");if(i){e.addEventListener("click",e=>{e.preventDefault()});const i=t.replace(/lang=true/i,"").replace(/modal=true/i,"").replace("?&","?").replace("&&","&").replace(/\&$/,""),s=window.location.host;let n="";s.indexOf("localhost")>-1?n=window.location.pathname.split("/")[4]:this.isUrlAllowedByType(s,allowedAuthorHosts)?n=window.location.pathname.split("/")[3]:this.isUrlAllowedByType(s,allowedPublishHosts)&&(n=window.location.pathname.split("/")[1]),e.setAttribute("data-modal","true"),a>-1?e.setAttribute("data-url","".concat(i,"&locale=").concat(n)):e.setAttribute("data-url","".concat(i,"?locale=").concat(n))}}}),t&&t.length&&t.forEach(e=>{const t=e.getAttribute("href")||e.getAttribute("data-href"),i=t.indexOf("form=true");if(t){let a;e.addEventListener("click",e=>{e.preventDefault()}),t.match(/videoid=.*/)?a=t.replace(/esri.com\/videos\/watch\?videoid=/,"esri.com/videos/iframe?videoid="):t.match(/youtube.com\/embed/)?a=t.replace(/youtube.com\/embed\?v=/,"esri.com/videos/iframe?videoid="):t.match(/youtube.com\/watch/)?a=t.replace(/youtube.com\/watch\?v=/,"esri.com/videos/iframe?videoid="):t.match(/youtu.be/)?a=t.replace(/youtu.be\//,"www.esri.com/videos/iframe?videoid="):t.match(/(https:\/\/vimeo\.com)/)?(a=t.replace(/(https:\/\/vimeo\.com)/,"https://player.vimeo.com/video"),a=a.concat("&transparent=0")):a=t.match(/(https:\/\/player\.vimeo\.com)/)?t.concat("&transparent=0"):t.match(/(https:\/\/mediaspace\.esri\.com\/media\/t)/)?t.replace(/(https:\/\/mediaspace\.esri\.com\/media\/t)/,"https://mediaspace.esri.com/embed/secure/iframe/entryId"):t,a=a.replace(/co3=true/i,"").replace(/form=true/i,"").replace("?&","?").replace("&&","&").replace(/\&$/,"").replace(/\?$/,""),e.setAttribute("data-modal","true"),e.setAttribute("data-url","".concat(a)),i>-1&&e.setAttribute("data-form","true"),e.style.cursor="pointer",t.match(/co3=full/)&&(this.isFullScrn=!0),t.match(/co3$/)&&(this.isDocType=!0),a.match(/videoid=.*/)&&!a.includes("autoplay=1")&&(a+=a+"?autoplay=1",e.setAttribute("data-url",a))}})}preventBgroundScroll(){this.bodyTag.setAttribute("style","overflow: hidden;"),this.htmlTag.setAttribute("style","overflow-y: unset;"),this.bodyTag.setAttribute("aria-hidden","true"),this.bodyTag.setAttribute("tabindex","-1")}preventExit(){const e=document.getElementsByClassName("co3-modal")[0],t=document.getElementsByClassName("modaltransition")[0],i=document.getElementsByClassName("icon-ui-close")[0];t.addEventListener("focus",t=>{t.preventDefault(),e.focus()}),e.addEventListener("keyup",e=>{e.shiftKey&&"Tab"===e.key&&i.focus()})}isUrlAllowedByType(e,t){try{const i=new URL(e);return t.includes(i.hostname)}catch(e){console.warn("Invalid URL or unaccepted host:",e)}}removeKalturaCss(){return this.analytics.removeKalturaCss()}enableAnalytics(e){return this.analytics.enableAnalytics(e)}async getVideo(e){return this.analytics.getVideo(e)}buildAnalyticsPlayer(e,t){return this.analytics.buildAnalyticsPlayer(e,t)}getMediaSegment(e){return this.analytics?this.analytics.getMediaSegment(e):(console.warn("KalturaAnalytics is not defined. getMediaSegment will return null."),null)}buildCloseButton(e,t,i){const a=e.closest(".co3-modal");if(!a)return;const s=a.querySelector(".iframe-container");s&&a.addEventListener("click",a=>{s&&s.contains(a.target)||a.target===e||null!==a.target.closest(".playkit-icon-play")||null!==a.target.closest(".playkit-pre-playback-play-overlay")||this.closeModal(e,t,i)}),e.parentNode&&(e.addEventListener("keypress",a=>{"Enter"===a.key&&(a.preventDefault(),this.closeModal(e,t,i))}),e.addEventListener("keyup",a=>{"Escape"===a.key&&(a.preventDefault(),this.closeModal(e,t,i))}),e.addEventListener("click",a=>{a.preventDefault(),this.closeModal(e,t,i)}))}createModal(t){t.forEach(t=>{t.addEventListener("click",i=>{let a=null,s=null;const n=i.target.getAttribute("data-entryId"),r=[].slice.call(document.querySelectorAll(".hbg-container")).filter(e=>0==e.classList.contains("has-background"));this.preventBgroundScroll(),null!==r&&r.length>0&&r.forEach(e=>{s=esriClientUtils.select("video",e),null!==e.lastElementChild&&(a=esriClientUtils.select("video",e.lastElementChild),null!==a&&(a.src=a.hasAttribute("data-video-src")?this.sanitizeInput(a.getAttribute("data-video-src")):" ",a.hasVid=!0,a.pause())),null!==s&&(s.src=this.sanitizeInput(s.getAttribute("data-video-src")),s.pause())}),this.playBtn=t.querySelector("button")?t.querySelector("button"):i.target,this.returnElement=i.target.parentNode,this.returnElement.setAttribute("data-return-btn",""),this.bodyNode.setAttribute("aria-hidden","true"),this.bodyNode.setAttribute("tabindex",-1),this.playBtn&&this.playBtn.setAttribute("tabindex",-1),i.preventDefault();const l=t.getAttribute("data-modal"),o=t.getAttribute("data-form");let c=t.getAttribute("data-url");if("true"===l&&c&&null!=c){if(this.isUrlAllowedByType(c,e.allowedPardotHost)||o)var d=!0;if(c.includes("youtube.com/watch")){const e=new URL(c).searchParams.get("v");c="https://www.youtube.com/embed/".concat(e)}else if(c.includes("youtu.be/")){const e=c.split("youtu.be/")[1];c="https://www.youtube.com/embed/".concat(e)}else if(c.includes("mediaspace.esri.com/media")){c.split("/media/")[1];c="https://mediaspace.esri.com/embed/secure/iframe/entryId/".concat(n,"/uiConfId/49806163")}const t=document.createElement("div"),i=document.createElement("calcite-loader"),a=document.createElement("div"),s=document.createElement("div"),l=document.createElement("div"),u=document.createElement("iframe"),h=document.createElement("div");h.setAttribute("class","modaltransition"),h.setAttribute("tabindex","0"),i.setAttribute("active","true"),i.setAttribute("style","block-size: 100%;"),i.setAttribute("z-index","-1"),l.setAttribute("class","iframe-container"),null!=n&&""!==n&&l.setAttribute("id","kaltura_player_id_".concat(n)),l.setAttribute("src","".concat(c)),l.setAttribute("tabindex","-1"),t.setAttribute("class","co3-modal calcite-mode-dark"),t.setAttribute("tabindex","0"),t.setAttribute("aria-label","This is a dialog window which contains video and overlays the main content of the page. Tab through to find play button."),t.setAttribute("data-tabbable-attr","modal"),t.setAttribute("aria-hidden","false"),this.isDocType&&t.setAttribute("data-modal-type","doctype"),u.setAttribute("src","".concat(c)),d?(a.setAttribute("class","co3-modal-box pardot-form"),u.style.overflowX="hidden"):(a.setAttribute("class","co3-modal-box"),a.setAttribute("tabindex","-1"),u.setAttribute("scrolling","no"),u.setAttribute("sandbox","allow-forms allow-same-origin allow-scripts allow-top-navigation allow-pointer-lock allow-popups allow-modals allow-orientation-lock allow-popups-to-escape-sandbox allow-presentation allow-top-navigation-by-user-activation"),u.setAttribute("allow","autoplay *; fullscreen *; encrypted-media *")),this.isFullScrn&&(t.setAttribute("data-modal-type","fullscreen"),t.classList.add("animate-full","fadeUpFull")),s.setAttribute("class","modal-close-button"),s.innerHTML="<calcite-icon icon='x' aria-label='modal close' role='button' tabindex='0' scale='l' alignment='center' aria-hidden='true'></calcite-icon>",a.appendChild(i),a.appendChild(l),t.appendChild(a);null==esriClientUtils.select(".co3-modal")&&document.body.appendChild(t),a.appendChild(s),a.appendChild(h),t.focus(),this.preventExit();const m=esriClientUtils.select(".co3-modal-box iframe");m&&null!=m&&m.addEventListener("load",()=>{a.removeChild(i)}),n?(this.removeKalturaCss(),this.enableAnalytics(n)):l.appendChild(u),this.buildCloseButton(s,this.playBtn,r),this.observeModal(l)}})})}}function t(t){let i=arguments.length>1&&void 0!==arguments[1]?arguments[1]:15e3;return new Promise((a,s)=>{const n=Date.now(),r=setInterval(()=>{if(Date.now()-n>i)clearInterval(r),function(){const e=document.querySelector(".channel-playlist--container"),t=e&&esriClientUtils.select("calcite-loader",e);null!==t&&t.parentNode.remove()}(),s("");else{const i=document.querySelector(t);if(i){const t=i.closest(".cmp-embed"),s=esriClientUtils.select(".channel-playlist__search-icon",t),n=esriClientUtils.select("#channel-playlist__searchInput",t),l=esriClientUtils.select(".channel-playlist__search-close",t),o=esriClientUtils.select(".channel-playlist__relevance",t),c=esriClientUtils.select(".channel-playlist__descending-button",t),d=esriClientUtils.select(".channel-playlist__ascending-button",t),u=esriClientUtils.select(".channel-playlist__AZ-button",t),h=esriClientUtils.select(".channel-playlist__ZA-button",t),m=300;let p;[o,c,d,u,h,s,l].forEach(t=>{t.addEventListener("click",()=>{setTimeout(()=>{new e},360)})}),window.addEventListener("scroll",()=>{clearTimeout(p),p=setTimeout(function(){new e},m)}),n.addEventListener("keypress",t=>{"Enter"===t.key&&setTimeout(()=>{new e},600)}),clearInterval(r),a(i),new e}}},330)})}o(e,"allowedHosts",["go.esri.com","downloads.esri.com","www.arcgis.com","mediaspace.esri.com","www.esri","esri.com","storymaps.arcgis","storymapsstg.arcgis.com","doc.arcgis.com","arcgis.com","www.youtube.com","youtube.com"]),o(e,"allowedPublishHosts",["publish-p80902-e698666.adobeaemcloud.com","publish-p80902-e698665.adobeaemcloud.com","publish-p80902-e698709.adobeaemcloud.com","esri.com","www.esri"]),o(e,"allowedAuthorHosts",["author-p80902-e698666.adobeaemcloud.com",'author-p80902-e698665.adobeaemcloud.com"',"author-p80902-e698709.adobeaemcloud.com"]),o(e,"allowedPardotHost",["go.esri.com","go.pardot.com"]),async function(){try{await t(".channel-playlist--column",45e3)}catch(e){}}(),setTimeout(()=>{new e},1e3)}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e,t){null!==e&&("teaser"==t&&this.getTeaserLink(e),"list"==t&&this.getListLink(e),"title"==t&&this.getTitleLink(e),"text"==t&&this.getTextLink(e),"carousel"==t&&this.getCarouselLink(e))}getTextLink(e){if(e.hasChildNodes()){const t=e.querySelectorAll("[href]");t&&t.length>0&&t.forEach((e,t)=>{const i=e.innerHTML;this.addDataAttr(e,i,"","Core Text",t)})}}getTitleLink(e){if(e.hasChildNodes()){const t=e.querySelectorAll(".cmp-title__link"),i=e.querySelector(".cmp-title__text"),a=!!i.querySelector(".cmp-title__link");let s="";if(t&&t.length>0&&a){const e=i.querySelector(".cmp-title__link"),a=e.getAttribute("aria-label");s=e.innerHTML,t.forEach((e,t)=>{this.addDataAttr(e,s,a,"Core Title",t)})}}}getListLink(e){if(e.hasChildNodes()){const t=e.querySelectorAll(".cmp-list__item-link"),i=e.parentNode.parentNode.querySelector(".title");let a="";null!==i&&(a=i.querySelector("h1").innerHTML),t&&t.length>0&&t.forEach((e,t)=>{const i=e.querySelector(".cmp-list__item-title");this.addDataAttr(e,a,i.innerHTML,"Core List",t)})}}getTeaserLink(e){if(e.hasChildNodes()){const t=e.querySelectorAll(".cmp-teaser__action-link"),i=e.parentNode.querySelector(".cmp-teaser__title");let a="",s="";null!==i&&(a=i.querySelector(".cmp-teaser__title-link")),null!==i&&null!==a&&(s=a.innerHTML,this.addDataAttr(a,"",s,"Core Teaser",0)),t&&t.length>0&&t.forEach((e,t)=>{this.addDataAttr(e,s,e.innerHTML,"Core Teaser",t)})}}getCarouselLink(e){if(e.hasChildNodes()){const t=e.querySelectorAll("button.cmp-carousel__action"),i=e.querySelectorAll("li.cmp-carousel__indicator");i.length>0&&i.forEach((e,t)=>{const i=e.querySelector("button.indicator-title");null!==i&&this.addDataAttr(e,i.innerHTML,"","Core Carousel",t,"button")}),t&&t.length>0&&t.forEach((e,t)=>{const i=e.getAttribute("aria-label");this.addDataAttr(e,i,"","Core Carousel",t,"button")})}}addDataAttr(e,t,i,a,s,n){this.dataObj={},this.dataObj.eventName=a,this.dataObj.eventLink="".concat(t," - ").concat(i),this.dataObj.eventLinkUrl=e.getAttribute("href"),this.dataObj.domain=window.location.hostname.split(".")[0],this.dataObj.index=s,this.dataObj.dataLinkType="text",this.dataObj.dataEvent="track-component",this.dataObj.linkType=this.getTypeOfLink(this.dataObj.eventLinkUrl,this.dataObj.domain),e.setAttribute("data-component-name",a),null!==i&&(t.length>0&&i.length>0&&e.setAttribute("data-component-link","".concat(t," - ").concat(i)),t.length>0&&0==i.length&&e.setAttribute("data-component-link","".concat(t)),0==t.length&&i.length>0&&e.setAttribute("data-component-link","".concat(i))),"string"==typeof n?e.setAttribute("data-component-link-type",n):e.setAttribute("data-component-link-type",this.dataObj.linkType),"external"==this.dataObj.linkType&&(this.dataObj.target="_blank",this.dataObj.rel="noopener",e.setAttribute("target","_blank"),e.setAttribute("rel","noopener"),e.setAttribute("aria-describedby","new-window")),e.setAttribute("data-event","track-component"),e.setAttribute("data-component-link-placement",s),"undefined"!=typeof adobeDataLayer&&e.addEventListener("click",()=>{adobeDataLayer.push({event:"eventCustomLink",data:{eventName:a,eventLink:i,eventLinkType:this.getTypeOfLink(this.dataObj.eventLinkUrl,this.dataObj.domain)}})})}getTypeOfLink(e,t){const i=/(youtube\.com\/[\w\/\?\&\=]*list=[\w\&\=]*$)|(youtube\.com\/c\/)/gm,a=/(https:\/\/vimeo)|(https:\/\/player\.vimeo\.com)/gm,s=/(https:\/\/mediaspace\.esri\.com\/media\/t)|(https:\/\/mediaspace\.esri\.com\/embed)/;e||(e="");let n="";if(e&&t){const r=t.replace("www.","");if(!i.test(e)&&(e.indexOf("video")>-1||e.indexOf("watch?")>-1||e.indexOf("videoid")>-1||e.indexOf("youtu.be")>-1||e.indexOf("youtube")>-1||e.indexOf("mp4")>-1)||a.test(e)||s.test(e))n="video";else if(e.indexOf(".pdf")>-1||e.indexOf(".doc")>-1||e.indexOf(".docx")>-1||e.indexOf(".ppt")>-1||e.indexOf(".xls")>-1||e.indexOf(".xlsx")>-1||e.indexOf(".csv")>-1||e.indexOf(".txt")>-1)n="document";else if(e.indexOf("//")>-1)if("esri.com"==r||"aem-author-dev"==r||"aem-author-dev.esri.com"==r||"aem-author-stg"==r||"aem-author-stg.esri.com"==r||"aem-author-prd"==r||"aem-author-dev.esri.com"==r||"aem-author-stg"==r||"aem-author-stg.esri.com"==r||"aem-author-prd.esri.com"==r||"uat.esri.com"==r||"aem-dev.esri.com"==r||"aem-prd1.esri.com"==r||"aem-prd2.esri.com"==r||"preview.esri.com"==r||r.indexOf("localhost")>-1){n="external";const t=["www.esri","esri.com","storymaps.arcgis","storymapsstg.arcgis.com","doc.arcgis.com","arcgis.com"];for(let i=0;i<t.length;i++){const a=t[i];e.indexOf(a)>-1&&(n="internal")}}else e.indexOf(r)<0||function(){try{return new URL(e).hostname.endsWith(".esri.com")}catch(e){return!1}}()||i.test(e)?n="external":e.indexOf("".concat(r,"/"))>-1&&(n="internal");else(0==e.indexOf("#")||e.indexOf("/content/esri-sites")>-1||e.indexOf("/content/distributor-sites")>-1||e.indexOf("/content/support")>-1)&&(n="internal")}return n}}const t=[].slice.call(document.querySelectorAll(".cmp-teaser__content"));null!==t&&t.forEach(t=>{new e(t,"teaser")});const i=[].slice.call(document.querySelectorAll("ul.cmp-list"));null!==i&&i.forEach(t=>{new e(t,"list")});const a=[].slice.call(document.querySelectorAll("div.cmp-title"));null!==a&&a.forEach(t=>{new e(t,"title")});const s=[].slice.call(document.querySelectorAll("div.cmp-text"));null!==s&&s.forEach(t=>{new e(t,"text")});const n=[].slice.call(document.querySelectorAll("div.cmp-carousel"));null!==n&&n.forEach(t=>{new e(t,"carousel")})});const c=new class{debounce(e,t){let i;return function(){clearTimeout(i),i=setTimeout(()=>e.apply(this,arguments),t)}}throttle(e,t){let i,a;return()=>{const s=this,n=arguments;a?(clearTimeout(i),i=setTimeout(()=>{Date.now()-a>=t&&(e.apply(s,n),a=Date.now())},t-(Date.now()-a))):(e.apply(s,n),a=Date.now())}}triggerEvent(e,t,i){let a,s=i;s||(s={detail:{message:"Custom Event",time:new Date},bubbles:!0,cancelable:!0}),window.CustomEvent&&"function"==typeof window.CustomEvent?a=new window.CustomEvent(t,s):(a=document.createEvent("CustomEvent"),a.initCustomEvent(t,!0,!0,s)),e.dispatchEvent(a)}select(e,t){if("string"==typeof e||e instanceof String){let i;return i=t?t.querySelector(e):document.querySelector(e),i}return console.error("Problem: esriClientUtils.select() requires that the sizzle value passed in is a string."),null}selectAll(e,t){if("string"==typeof e||e instanceof String){let i;return i=t?t.querySelectorAll(e):document.querySelectorAll(e),[].slice.call(i)}return console.error("Problem: esriClientUtils.selectAll() requires that the sizzle value passed in is a string."),[]}getOffset(e){const t=e.getBoundingClientRect(),i=window.pageXOffset||document.documentElement.scrollLeft,a=window.pageYOffset||document.documentElement.scrollTop;return{top:t.top+a,left:t.left+i}}fadeOut(e,t){let i=1;const a=window.setInterval(()=>{i<=.1&&(window.clearInterval(a),e.style.display="none",t&&t()),e.style.opacity=i,e.style.filter="alpha(opacity=".concat(100*i,")"),i-=.1*i},10)}};var d;window.esriClientUtils=c,d={previousWidth:0,throttle:function(e,t){var i=Date.now();return function(){i+t-Date.now()<0&&(e(),i=Date.now())}},resizeImage:function(e,t){var i=e.getAttribute("data-img-resize");i=i.split(","),"IMG"==e.nodeName?"narrow"==t?e.setAttribute("src",i[0]):"medium"==t?e.setAttribute("src",i[1]):"wide"==t&&e.setAttribute("src",i[2]):""!==e.style.backgroundImage&&("narrow"==t?e.style.backgroundImage="url("+i[0]+")":"medium"==t?e.style.backgroundImage="url("+i[1]+")":"wide"==t&&(e.style.backgroundImage="url("+i[2]+")"))},resizeAllImages:function(){var e=window.innerWidth,t=480,i=860;[].slice.call(document.querySelectorAll("*[data-img-resize]")).forEach(function(a,s){var n=a.getAttribute("data-img-resize-breakpoints");null!=n&&(n=n.split(","),t=Number(n[0]),i=Number(n[1])),e<=t&&d.previousWidth>t?d.resizeImage(a,"narrow"):e>t&&e<=i&&(d.previousWidth<=t||d.previousWidth>i)?d.resizeImage(a,"medium"):e>i&&d.previousWidth<=i&&d.resizeImage(a,"wide")}),d.previousWidth=window.innerWidth},init:function(){d.resizeAllImages(),window.addEventListener("resize",d.throttle(d.resizeAllImages,200))}},document.addEventListener("DOMContentLoaded",function(){d.init()}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e){this.elements=e,this.elements.forEach((e,t)=>{const i=e.offsetHeight,a=window.innerHeight;let s=.2;i>=a&&(s=.2*a/i);new IntersectionObserver(function(e,t){e.forEach(e=>{const t=e.target.getAttribute("data-animation");e.isIntersecting&&!e.target.classList.contains(t)&&e.target.classList.add(t)})},{root:null,rootMargin:"0px",threshold:s}).observe(e)})}}function t(e){const t=[].slice.call(document.querySelectorAll(e));null!==t&&t.length>0&&t.forEach(e=>{e.classList.add("calcite-animate"),e.setAttribute("data-animation","calcite-animate__in-up")})}t(".esri-accordion"),t(".esri-tabs");const i=[].slice.call(document.querySelectorAll(".calcite-animate[data-animation]"));i&&i.length>0&&new e(i)}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e){const t=e.querySelector(".sales-contact-link"),i=e.querySelector("#cta-different-location");if(t&&(this.removeHrefAttribute(t),this.extractModalAttr(t),this.init(t)),i){const e=i.querySelector("a");if(!e)return void console.error("CTA link not found in #cta-different-location container. Ensure the container includes an <a> element.");i.classList.add("sales-contact-link"),this.extractModalAttr(e),this.init(e)}}extractModalAttr(e){this.modalTitleText=e.getAttribute("data-modal-title")||"Change your location",this.modalSubtitleText=e.getAttribute("data-modal-subtitle")||"Select your country or region to see the appropriate content for your location.",this.modalPlaceholderText=e.getAttribute("data-modal-placeholder")||"Search for your country or region ",this.modalButtonText=e.getAttribute("data-modal-button")||"Change location"}init(e){e.addEventListener("click",e=>{e.preventDefault();const t=this.getCalciteModal();t.classList.add("calcite-mode-dark"),t.classList.add("dynamic-contact-location-modal");const i=document.createTextNode(this.modalTitleText),a=t.querySelector('[slot="header"]');a.textContent="",a.appendChild(i);const s=t.querySelector('[slot="content"]'),n=document.createTextNode(this.modalSubtitleText),r=document.createElement("calcite-label");r.setAttribute("id","search-locations-input-label");const l=document.createElement("calcite-input-message");l.setAttribute("status","valid"),l.setAttribute("id","search-locations-input-message"),l.classList.add("search-locations-input-message"),r.appendChild(l);const o=document.createElement("calcite-input");o.setAttribute("placeholder",this.modalPlaceholderText),o.setAttribute("icon","search"),o.setAttribute("id","search-locations-input"),o.classList.add("search-locations-input"),r.appendChild(o);const c=document.createElement("div");c.classList.add("country-search-suggestions-container"),c.setAttribute("id","country-search-suggestions-container"),r.appendChild(c);const d=document.createElement("calcite-button");d.setAttribute("appearance","solid"),d.setAttribute("color","blue"),d.setAttribute("scale","l"),d.setAttribute("id","search-locations-submit-button"),d.setAttribute("disabled",""),d.textContent=this.modalButtonText,d.onclick=()=>{window.adobeDataLayer&&adobeDataLayer.push({event:"componentEvent",data:{name:"CTA Container - Location Modal",linkType:"button",linkText:"Change location"}})},s.textContent="",s.appendChild(n),s.appendChild(r),s.appendChild(d);const u=document.createElement("link");u.setAttribute("rel","stylesheet"),u.setAttribute("href","https://webapps-cdn.esri.com/CDN/components/contact-us/css/contact-us-esri-product-pages-type-ahead-search.css"),s.appendChild(u);const h=document.createElement("script");h.setAttribute("type","module");let m=this.generateRandomCharacters(10);h.setAttribute("src","https://webapps-cdn.esri.com/CDN/components/contact-us/js/contact-us-esri-product-pages-type-ahead-search.js?"+m),s.appendChild(h),t.open=!0}),document.addEventListener("calciteModalClose",()=>{const e=this.getCalciteModal();e.classList.remove("calcite-mode-dark"),e.classList.remove("dynamic-contact-location-modal"),window.adobeDataLayer&&adobeDataLayer.push({event:"componentEvent",data:{name:"CTA Container - Location Modal",linkType:"button",linkText:"close"}})}),document.addEventListener("calciteModalOpen",()=>{let e=document.getElementById("search-locations-input");null!==e&&e.setFocus()})}removeHrefAttribute(e){e.removeAttribute("href")}getCalciteModal(){const e=document.querySelector("calcite-modal");if(null!==e)return e}generateRandomCharacters(e){let t="";const i="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";for(let a=0;a<e;a++)t+=i.charAt(Math.floor(62*Math.random()));return t}}const t=document.querySelectorAll(".dynamic-contact-content");null!==t&&t.forEach(t=>{new e(t)})}),document.addEventListener("DOMContentLoaded",()=>{const e=[].slice.call(document.querySelectorAll(".esri-text__title"));for(let t=0;t<e.length;t++){const i=e[t].innerHTML.split(""),a={"*":["<em>","</em>",null],"^":["<sup>","</sup>",null],"~":["<sub>","</sub>",null]};for(let e=0;e<i.length;e++){const t=i[e];a[t]&&(null===a[t][2]?a[t][2]=e:(i[a[t][2]]=a[t][0],i[e]=a[t][1],a[t][2]=null))}e[t].innerHTML=i.join("")}});const u=new class{constructor(){this.motionContainer=null,this.playButtonContainers=null,this.playButton=null,this._progressRaf=null}init(e,t,i,a,s,n,r,l,o){const c={componentSelector:e,videoSelector:t||"video",playButtonSelector:i||".video-play-pause",playButtonContainerSelector:a||".video-button-container",totalFrames:s||259.18,initDelay:n||1e3,multipleVideos:r||!1,disablePlayButtonStroke:l||!1,onePlayButtonControlsAllVideos:o||!1};document.querySelectorAll("".concat(e)).forEach(e=>{const t=e.querySelector("".concat(c.playButtonContainerSelector));if(!t)return;const i=t.querySelector("".concat(c.playButtonSelector));i&&setTimeout(()=>{this.bindFeatures(e,i,c)},c.initDelay)})}bindFeatures(e,t,i){const a=e.querySelectorAll(i.videoSelector);if(a&&0!==a.length)if(i.multipleVideos)i.onePlayButtonControlsAllVideos?(a[0].load(),a[0].addEventListener("loadedmetadata",()=>{const e=a[0].duration;this.setupVideoControl(a,t,a[0],e,i)})):a.forEach(e=>{e.load(),e.addEventListener("loadedmetadata",()=>{const s=e.duration;this.setupVideoControl(a,t,e,s,i)})});else{const e=a[0];if(e.readyState>=1){const a=e.duration;this.setupVideoControl(e,t,e,a,i)}else e.addEventListener("loadedmetadata",()=>{const a=e.duration;this.setupVideoControl(e,t,e,a,i)})}}setupVideoControl(e,t,i,a,s){const n=t.querySelector(".play-progress-circle");if(!n)return;const r=n.querySelector(".progress-circle");r.style.strokeDasharray=s.totalFrames,r.style.strokeDashoffset=s.totalFrames;const l={progressRaf:null};window.matchMedia("(prefers-reduced-motion: reduce)").matches||t.addEventListener("click",()=>{const a=i.paused?"play":"pause";if(s.multipleVideos){const a=i.paused;e.forEach(e=>{e.paused&&a?(this.playPromises(e),e.removeAttribute("data-user-paused")):(e.setAttribute("data-user-paused","true"),e.pause()),this.togglePlayButton(e,t)})}else i.paused?this.playPromises(i):i.pause(),this.togglePlayButton(i,t);this.trackPlayPauseEvent(a,s.componentSelector)}),i.addEventListener("play",()=>{this.togglePlayButton(i,t),s.disablePlayButtonStroke||this._startProgressLoop(i,r,a,s,l)}),i.addEventListener("pause",()=>{this.togglePlayButton(i,t),s.disablePlayButtonStroke||(this._stopProgressLoop(l),this.updateDashOffset(i,r,a,s))}),i.addEventListener("ended",()=>{this.togglePlayButton(i,t),s.disablePlayButtonStroke||(this._stopProgressLoop(l),r.style.strokeDashoffset=s.totalFrames)}),s.disablePlayButtonStroke||i.paused||this._startProgressLoop(i,r,a,s,l)}togglePlayButton(e,t){t&&(e.paused?(t.setAttribute("aria-label","Play animation"),t.classList.add("paused")):(t.setAttribute("aria-label","Pause animation"),t.classList.remove("paused")))}updateDashOffset(e,t,i,a){const s=e.currentTime/i*a.totalFrames;t.style.strokeDashoffset=a.totalFrames-s;const n="false"!==e.getAttribute("data-loop-video");e.paused||e.ended?s>=a.totalFrames&&(t.style.strokeDashoffset=a.totalFrames):s>=a.totalFrames&&(t.style.strokeDashoffset=a.totalFrames,n&&this.playPromises(e))}_startProgressLoop(e,t,i,a,s){cancelAnimationFrame(s.progressRaf);const n=()=>{const r=a.totalFrames-e.currentTime/i*a.totalFrames;t.style.strokeDashoffset=Math.max(0,r),s.progressRaf=requestAnimationFrame(n)};s.progressRaf=requestAnimationFrame(n)}_stopProgressLoop(e){cancelAnimationFrame(e.progressRaf),e.progressRaf=null}trackPlayPauseEvent(e,t){window.adobeDataLayer&&window.adobeDataLayer.push({event:"videoPlayPause",data:{eventName:t,eventAction:e}})}isUserPaused(e){return"true"===e.getAttribute("data-user-paused")}isContainerUserPaused(){const e=document.querySelector(this.containerSelector);return e&&"true"===e.dataset.userPaused}setContainerUserPaused(e){const t=document.querySelector(this.containerSelector);t&&(t.dataset.userPaused=e?"true":"false")}playPromises(e){const t=e.play();void 0!==t&&t.catch(e=>{console.warn("Video playback failed:",e)})}};window.VideoPlayButton=u;new class{constructor(){const e=new URLSearchParams(window.location.search),t=e.get("sf_id");t&&(this.deleteCookie("epn_id"),document.cookie="sf_id=".concat(t,";path=/;").concat("https:"===window.location.protocol?"secure;":""));const i=e.get("epn_id");i&&(this.deleteCookie("sf_id"),document.cookie="epn_id=".concat(i,";path=/;").concat("https:"===window.location.protocol?"secure;":""))}deleteCookie(e){document.cookie="".concat(e,"=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;")}},document.addEventListener("DOMContentLoaded",()=>{new class{constructor(){const e=esriClientUtils.selectAll("[data-lazy-image]");e&&e.length&&this.createresponsiveBgs(e);const t=esriClientUtils.selectAll("[data-lazy-image]");t&&t.length&&this.setupObserver(t)}setupObserver(e){if("IntersectionObserver"in window){const t=new IntersectionObserver((e,i)=>{e.forEach(e=>{e.isIntersecting&&(this.loadLazy(e),t.unobserve(e.target))})},{rootMargin:"200px"});e.forEach(e=>{t.observe(e)})}else e.forEach(e=>{this.loadLazy(e)})}setupObserver(e){if("IntersectionObserver"in window){const t=new IntersectionObserver((e,i)=>{e.forEach(e=>{e.isIntersecting&&(this.loadLazy(e),t.unobserve(e.target))})},{rootMargin:"200px"});e.forEach(e=>{t.observe(e)})}else e.forEach(e=>{this.loadLazy(e)})}loadLazy(e){e.target&&(e=e.target);if(!!e.hasAttribute("data-lazy-image")){let t=e.getAttribute("data-lazy-image");e.style.backgroundImage='url("'+t+'")'}}update(e){let{responsiveBg:t}=e;const i=void 0!==t.container?t.container.currentSrc:t.container.src;t.src!==i&&(t.src=i,"undefined"==(t.src,!1)&&(t.container.style.backgroundImage='url("'+t.src+'")'))}createresponsiveBgs(e){e.forEach(e=>{const t={container:e.hasAttribute("data-lazy-image")?e:null,src:""};null!==t.container&&this.update({responsiveBg:t})})}}}),document.addEventListener("DOMContentLoaded",function(){var e=[].slice.call(document.querySelectorAll("video.lazy--video"));if("IntersectionObserver"in window){var t=new IntersectionObserver(function(e,i){e.forEach(function(e){if(e.isIntersecting){for(var i in e.target.children){var a=e.target.children[i];"string"==typeof a.tagName&&"SOURCE"===a.tagName&&a.src!=a.dataset.src&&(a.src=a.dataset.src,e.target.play())}e.target.classList.remove("lazy"),t.unobserve(e.target)}})});e.forEach(function(e){t.observe(e)})}}),document.addEventListener("DOMContentLoaded",()=>{class e{constructor(e){this.videoElems=e,this.activateVidSrc(this.videoElems),this.addLoopPlay(this.videoElems);const t=window.matchMedia("(prefers-reduced-motion: reduce)").matches,i=new IntersectionObserver(function(e,i){e.forEach(e=>{const i=[].slice.call(document.querySelectorAll(".co3-modal"));!t&&e.isIntersecting&&i.length<1?e.target.querySelector("[data-loop-video]").play():e.target.querySelector("[data-loop-video]").pause()})},{root:null,rootMargin:"0px",threshold:.5});this.videoElems.forEach(e=>{i.observe(e.parentElement)})}createSrcTag(e,t){const i=e,a=t,s=i.firstElementChild;if(null!==s){i.removeChild(s);const e=document.createElement("source");return e.setAttribute("data-src",a),e.setAttribute("src",a),e.setAttribute("type","video/mp4"),e}}activateVidSrc(e){const t=window.matchMedia("(min-width: 768px)");e.forEach(e=>{const i=e.getAttribute("data-video-src"),a=e.getAttribute("data-video-type");null!==i&&i.length>0&&("foreground"==a&&e.appendChild(this.createSrcTag(e,i)),"background"==a&&t.matches&&e.appendChild(this.createSrcTag(e,i)))})}addLoopPlay(e){e.forEach(e=>{"true"==e.getAttribute("data-loop-video")&&e.setAttribute("loop","")})}}const t=[].slice.call(document.querySelectorAll("[data-loop-video]"));t&&t.length>0&&setTimeout(function(){new e(t)},500)})})();