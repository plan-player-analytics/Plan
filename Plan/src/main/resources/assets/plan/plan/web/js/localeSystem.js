/**
 * A locale system for localizing the website.
 */
const localeSystem = {
    /**
     * @function
     * Localizes an element.
     * @param {Element} element Element to localize
     * @param {Object} [options] Options
     */
    localize: {},

    /**
     * The current default language reported by the server.
     * @type {string}
     * @readonly
     */
    defaultLanguage: "",

    /**
     * The current available languages reported by the server.
     * @type {Object.<string, string>}
     * @readonly
     */
    availableLanguages: {},
    clientLocale: "",

    /**
     * Initializes the locale system. Gets the default & available languages from `/v1/locale`, and initializes i18next.
     */
    init: function () {
        jsonRequest("./v1/locale", (json, error) => {
            if (error) {
                throw "Error occurred when downloading language information: " + error;
            }

            this.defaultLanguage = json.defaultLanguage;
            this.availableLanguages = json.languages;

            this.clientLocale = window.localStorage.getItem("locale");
            if (!this.clientLocale) {
                this.clientLocale = this.defaultLanguage;
            }

            i18next.use(i18nextChainedBackend)
                .init({
                    lng: this.clientLocale,
                    fallbackLng: false,
                    supportedLngs: Object.keys(this.availableLanguages),
                    backend: {
                        backends: [
                            i18nextLocalStorageBackend,
                            i18nextHttpBackend
                        ],
                        backendOptions: [{
                            expirationTime: 7 * 24 * 60 * 60 * 1000 // 7 days
                        }, {
                            loadPath: './v1/locale/{{lng}}'
                        }]
                    },
                }, () => {
                    this.loadSelector();
                    this.initLoci18next();
                });
        })
    },

    initLoci18next: function () {
        this.localize = locI18next.init(i18next, {
            selectorAttr: 'data-i18n', // selector for translating elements
            targetAttr: 'i18n-target',
            optionsAttr: 'i18n-options',
            useOptionsAttr: false,
            parseDefaultValueFromContent: false // show key as fallback if locale fails to load
        });
        this.localize("title");
        this.localize("body");
    },

    /**
     * Loads a locale and translates the page.
     *
     * @param {string} langCode The two-character code for the language to be loaded, e.g. EN
     * @throws Error if an invalid langCode is given
     * @see /v1/language endpoint for available language codes
     */
    loadLocale: function (langCode) {
        if (i18next.language === langCode) {
            return;
        }
        if (!(langCode in this.availableLanguages)) {
            throw `The locale ${langCode} isn't available!`;
        }

        window.localStorage.setItem("locale", langCode);
        i18next.changeLanguage(langCode, () => {
            this.localize("title");
            this.localize("body");
        })
    },

    loadSelector: function () {
        const selector = document.getElementById("langSelector");

        let languages = Object.fromEntries(Object.entries(this.availableLanguages).sort());
        if ('CUSTOM' in languages) {
            // Move "Custom" to first in list
            delete languages["CUSTOM"]
            languages = Object.assign({"CUSTOM": "Custom"}, languages);
        }

        for (let lang in languages) {
            let option = document.createElement("option");
            option.value = lang;
            option.innerText = languages[lang];
            if (this.clientLocale === lang) {
                option.setAttribute("selected", "")
            }
            selector.append(option);
        }

        selector.addEventListener('change', event => {
            this.loadLocale(event.target.value);
        })
    }
}
