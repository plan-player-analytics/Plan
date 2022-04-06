import i18next from "i18next";
import I18NextChainedBackend from "i18next-chained-backend";
import I18NextLocalStorageBackend from "i18next-localstorage-backend";
import I18NextHttpBackend from 'i18next-http-backend';
import {initReactI18next} from 'react-i18next';
import {fetchAvailableLocales} from "./metadataService";

/**
 * A locale system for localizing the website.
 */
export const localeService = {
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
    init: async function () {
        try {
            const {data} = await fetchAvailableLocales();

            this.defaultLanguage = data.defaultLanguage;
            this.availableLanguages = data.languages;
            this.languageVersions = data.languageVersions;

            this.clientLocale = window.localStorage.getItem("locale");
            if (!this.clientLocale) {
                this.clientLocale = this.defaultLanguage;
            }

            await i18next
                .use(I18NextChainedBackend)
                .use(initReactI18next)
                .init({
                    debug: false,
                    lng: this.clientLocale,
                    fallbackLng: false,
                    supportedLngs: Object.keys(this.availableLanguages),
                    backend: {
                        backends: [
                            I18NextLocalStorageBackend,
                            I18NextHttpBackend
                        ],
                        backendOptions: [{
                            expirationTime: 7 * 24 * 60 * 60 * 1000, // 7 days
                            versions: this.languageVersions
                        }, {
                            loadPath: '/v1/locale/{{lng}}'
                        }]
                    },
                }, () => {/* No need to initialize anything */
                });
        } catch (e) {
            console.error(e);
        }
    },

    /**
     * Loads a locale and translates the page.
     *
     * @param {string} langCode The two-character code for the language to be loaded, e.g. EN
     * @throws Error if an invalid langCode is given
     * @see /v1/language endpoint for available language codes
     */
    loadLocale: async function (langCode) {
        if (i18next.language === langCode) {
            return;
        }
        if (!(langCode in this.availableLanguages)) {
            throw Error(`The locale ${langCode} isn't available!`);
        }

        window.localStorage.setItem("locale", langCode);
        await i18next.changeLanguage(langCode)
    },

    getLanguages: function () {
        let languages = Object.fromEntries(Object.entries(this.availableLanguages).sort());
        if ('CUSTOM' in languages) {
            // Move "Custom" to first in list
            delete languages["CUSTOM"]
            languages = Object.assign({"CUSTOM": "Custom"}, languages);
        }

        return Object.entries(languages)
            .map(entry => {
                return {name: entry[0], displayName: entry[1]}
            });
    }
}
