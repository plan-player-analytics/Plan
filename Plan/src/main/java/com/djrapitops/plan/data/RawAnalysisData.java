package main.java.com.djrapitops.plan.data;

import com.djrapitops.javaplugin.utilities.Verify;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;

/**
 * This class is used for storing combined data of several UserData objects
 * during Analysis and thus is not documented.
 *
 * @author Rsl1122
 * @since 2.6.0
 * @see Analysis
 */
public class RawAnalysisData {

    private final Map<RawData, Long> longValues;
    private final Map<RawData, Integer> intValues;

    private final List<Integer> ages;
    private final List<Long> registered;
    private final Map<String, Long> latestLogins;
    private final Map<String, Long> playtimes;
    private final List<SessionData> sessiondata;
    private final Map<UUID, List<SessionData>> sortedSessionData;
    private final Map<String, Integer> commandUse;
    private final Map<String, Integer> geolocations;
    private final Map<String, String> geocodes;

    /**
     * Constructor for a new empty dataset.
     */
    public RawAnalysisData() {
        longValues = new HashMap<>();
        intValues = new HashMap<>();
        placeDefaultValues();

        ages = new ArrayList<>();
        latestLogins = new HashMap<>();
        playtimes = new HashMap<>();
        sessiondata = new ArrayList<>();
        sortedSessionData = new HashMap<>();
        commandUse = new HashMap<>();
        geolocations = new HashMap<>();
        geocodes = new HashMap<>();
        registered = new ArrayList<>();
    }

    private void placeDefaultValues() {
        longValues.put(RawData.TIME_GM0, 0L);
        longValues.put(RawData.TIME_GM1, 0L);
        longValues.put(RawData.TIME_GM2, 0L);
        longValues.put(RawData.TIME_GM3, 0L);
        longValues.put(RawData.LOGINTIMES, 0L);
        longValues.put(RawData.PLAYTIME, 0L);
        longValues.put(RawData.KILLS, 0L);
        longValues.put(RawData.MOBKILLS, 0L);
        longValues.put(RawData.DEATHS, 0L);
        intValues.put(RawData.AMOUNT_ACTIVE, 0);
        intValues.put(RawData.AMOUNT_BANNED, 0);
        intValues.put(RawData.AMOUNT_INACTIVE, 0);
        intValues.put(RawData.AMOUNT_UNKNOWN, 0);
        intValues.put(RawData.AMOUNT_OPS, 0);
    }

    public long getLong(RawData key) {
        return Verify.nullCheck(longValues.get(key));
    }

    public int getInt(RawData key) {
        return Verify.nullCheck(intValues.get(key));
    }

    @Deprecated
    private void add(RawData key, long amount) {
        addTo(key, amount);
    }

    public void addTo(RawData key, long amount) {
        Verify.nullCheck(key);
        Long l = longValues.get(key);
        Integer i = intValues.get(key);
        if (Verify.notNull(l)) {
            longValues.replace(key, l + amount);
        } else if (Verify.notNull(i)) {
            intValues.replace(key, i + (int) amount);
        } else {
            throw new IllegalArgumentException("Incorrect key: " + key.name());
        }
    }

    /**
     *
     * @param country
     */
    public void addGeoloc(String country) {
        if (geolocations.get(country) == null) {
            return;
        }
        geolocations.put(country, geolocations.get(country) + 1);
    }

    /**
     *
     */
    public void fillGeolocations() {
        String[] countries = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas, The", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Democratic Republic of the", "Congo, Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Islas Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia", "Gabon", "Gambia, The", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guam", "Guatemala", "Guernsey", "Guinea-Bissau", "Guinea", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea, South", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia, Federated States of", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Namibia", "Nepal", "Netherlands", "New Caledonia", "New Zealand", "Nicaragua", "Nigeria", "Niger", "Niue", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Puerto Rico", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands", "West Bank", "Yemen", "Zambia", "Zimbabwe"};
        String[] codes = new String[]{"AFG", "ALB", "DZA", "ASM", "AND", "AGO", "AIA", "ATG", "ARG", "ARM", "ABW", "AUS", "AUT", "AZE", "BHM", "BHR", "BGD", "BRB", "BLR", "BEL", "BLZ", "BEN", "BMU", "BTN", "BOL", "BIH", "BWA", "BRA", "VGB", "BRN", "BGR", "BFA", "MMR", "BDI", "CPV", "KHM", "CMR", "CAN", "CYM", "CAF", "TCD", "CHL", "CHN", "COL", "COM", "COD", "COG", "COK", "CRI", "CIV", "HRV", "CUB", "CUW", "CYP", "CZE", "DNK", "DJI", "DMA", "DOM", "ECU", "EGY", "SLV", "GNQ", "ERI", "EST", "ETH", "FLK", "FRO", "FJI", "FIN", "FRA", "PYF", "GAB", "GMB", "GEO", "DEU", "GHA", "GIB", "GRC", "GRL", "GRD", "GUM", "GTM", "GGY", "GNB", "GIN", "GUY", "HTI", "HND", "HKG", "HUN", "ISL", "IND", "IDN", "IRN", "IRQ", "IRL", "IMN", "ISR", "ITA", "JAM", "JPN", "JEY", "JOR", "KAZ", "KEN", "KIR", "KOR", "PRK", "KSV", "KWT", "KGZ", "LAO", "LVA", "LBN", "LSO", "LBR", "LBY", "LIE", "LTU", "LUX", "MAC", "MKD", "MDG", "MWI", "MYS", "MDV", "MLI", "MLT", "MHL", "MRT", "MUS", "MEX", "FSM", "MDA", "MCO", "MNG", "MNE", "MAR", "MOZ", "NAM", "NPL", "NLD", "NCL", "NZL", "NIC", "NGA", "NER", "NIU", "MNP", "NOR", "OMN", "PAK", "PLW", "PAN", "PNG", "PRY", "PER", "PHL", "POL", "PRT", "PRI", "QAT", "ROU", "RUS", "RWA", "KNA", "LCA", "MAF", "SPM", "VCT", "WSM", "SMR", "STP", "SAU", "SEN", "SRB", "SYC", "SLE", "SGP", "SXM", "SVK", "SVN", "SLB", "SOM", "ZAF", "SSD", "ESP", "LKA", "SDN", "SUR", "SWZ", "SWE", "CHE", "SYR", "TWN", "TJK", "TZA", "THA", "TLS", "TGO", "TON", "TTO", "TUN", "TUR", "TKM", "TUV", "UGA", "UKR", "ARE", "GBR", "USA", "URY", "UZB", "VUT", "VEN", "VNM", "VGB", "WBG", "YEM", "ZMB", "ZWE"};
        for (int i = 0; i < countries.length; i++) {
            String country = countries[i];
            if (geolocations.get(country) == null) {
                geolocations.put(country, 0);
            }
            if (geocodes.get(country) == null) {
                geocodes.put(country, codes[i]);
            }
        }
    }

    /**
     *
     * @return
     */
    public Map<String, Integer> getGeolocations() {
        return geolocations;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getGeocodes() {
        return geocodes;
    }

    /**
     *
     * @return
     */
    public List<Integer> getAges() {
        return ages;
    }

    /**
     *
     * @return
     */
    public Map<String, Long> getLatestLogins() {
        return latestLogins;
    }

    /**
     *
     * @return
     */
    public Map<String, Long> getPlaytimes() {
        return playtimes;
    }

    /**
     *
     * @return
     */
    public List<SessionData> getSessiondata() {
        return sessiondata;
    }

    /**
     *
     * @return
     */
    public Map<UUID, List<SessionData>> getSortedSessionData() {
        return sortedSessionData;
    }

    /**
     *
     * @param uuid
     * @param sessions
     */
    public void addSessions(UUID uuid, List<SessionData> sessions) {
        sessiondata.addAll(sessions);
        sortedSessionData.put(uuid, sessions);
    }

    /**
     *
     * @param commandUse
     */
    public void setCommandUse(Map<String, Integer> commandUse) {
        this.commandUse.putAll(commandUse);
    }

    /**
     *
     * @return
     */
    public Map<String, Integer> getCommandUse() {
        return commandUse;
    }

    /**
     *
     * @return
     */
    public List<Long> getRegistered() {
        return registered;
    }

}
