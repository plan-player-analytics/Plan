package main.java.com.djrapitops.plan.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rsl1122
 */
public class RawAnalysisData {

    private long gmZero;
    private long gmOne;
    private long gmTwo;
    private long gmThree;
    private long totalLoginTimes;
    private long totalPlaytime;
    private int totalBanned;
    private int active;
    private int joinleaver;
    private int inactive;
    private long totalKills;
    private long totalMobKills;
    private long totalDeaths;
    private int ops;
    private List<Integer> ages;
    private Map<String, Long> latestLogins;
    private Map<String, Long> playtimes;
    private List<SessionData> sessiondata;
    private Map<String, Integer> commandUse;
    private Map<String, Integer> geolocations;
    private Map<String, String> geocodes;
    private List<Long> registered;
    private int[] genders;

    /**
     *
     */
    public RawAnalysisData() {
        gmZero = 0;
        gmOne = 0;
        gmTwo = 0;
        gmThree = 0;
        totalLoginTimes = 0;
        totalPlaytime = 0;
        totalBanned = 0;
        active = 0;
        joinleaver = 0;
        inactive = 0;
        totalKills = 0;
        totalMobKills = 0;
        ops = 0;
        ages = new ArrayList<>();
        latestLogins = new HashMap<>();
        playtimes = new HashMap<>();
        sessiondata = new ArrayList<>();
        commandUse = new HashMap<>();
        geolocations = new HashMap<>();
        geocodes = new HashMap<>();
        registered = new ArrayList<>();
        genders = new int[]{0, 0, 0};
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
     * @param gmZero
     */
    public void addToGmZero(long gmZero) {
        this.gmZero += gmZero;
    }

    /**
     *
     * @param gmOne
     */
    public void addToGmOne(long gmOne) {
        this.gmOne += gmOne;
    }

    /**
     *
     * @param gmTwo
     */
    public void addToGmTwo(long gmTwo) {
        this.gmTwo += gmTwo;
    }

    /**
     *
     * @param gmThree
     */
    public void addGmThree(long gmThree) {
        this.gmThree += gmThree;
    }

    /**
     *
     * @param totalLoginTimes
     */
    public void addTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes += totalLoginTimes;
    }

    /**
     *
     * @param totalPlaytime
     */
    public void addTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime += totalPlaytime;
    }

    /**
     *
     * @param totalBanned
     */
    public void addTotalBanned(int totalBanned) {
        this.totalBanned += totalBanned;
    }

    /**
     *
     * @param active
     */
    public void addActive(int active) {
        this.active += active;
    }

    /**
     *
     * @param joinleaver
     */
    public void addJoinleaver(int joinleaver) {
        this.joinleaver += joinleaver;
    }

    /**
     *
     * @param inactive
     */
    public void addInactive(int inactive) {
        this.inactive += inactive;
    }

    /**
     *
     * @param totalKills
     */
    public void addTotalKills(long totalKills) {
        this.totalKills += totalKills;
    }

    /**
     *
     * @param totalMobKills
     */
    public void addTotalMobKills(long totalMobKills) {
        this.totalMobKills += totalMobKills;
    }

    /**
     *
     * @param totalDeaths
     */
    public void addTotalDeaths(long totalDeaths) {
        this.totalDeaths += totalDeaths;
    }

    /**
     *
     * @param ops
     */
    public void addOps(int ops) {
        this.ops += ops;
    }

    /**
     *
     * @return
     */
    public long getGmZero() {
        return gmZero;
    }

    /**
     *
     * @return
     */
    public long getGmOne() {
        return gmOne;
    }

    /**
     *
     * @return
     */
    public long getGmTwo() {
        return gmTwo;
    }

    /**
     *
     * @return
     */
    public long getGmThree() {
        return gmThree;
    }

    /**
     *
     * @return
     */
    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    /**
     *
     * @return
     */
    public long getTotalPlaytime() {
        return totalPlaytime;
    }

    /**
     *
     * @return
     */
    public int getTotalBanned() {
        return totalBanned;
    }

    /**
     *
     * @return
     */
    public int getActive() {
        return active;
    }

    /**
     *
     * @return
     */
    public int getJoinleaver() {
        return joinleaver;
    }

    /**
     *
     * @return
     */
    public int getInactive() {
        return inactive;
    }

    /**
     *
     * @return
     */
    public long getTotalKills() {
        return totalKills;
    }

    /**
     *
     * @return
     */
    public long getTotalMobKills() {
        return totalMobKills;
    }

    /**
     *
     * @return
     */
    public long getTotalDeaths() {
        return totalDeaths;
    }

    /**
     *
     * @return
     */
    public int getOps() {
        return ops;
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
     * @param commandUse
     */
    public void setCommandUse(HashMap<String, Integer> commandUse) {
        this.commandUse = commandUse;
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

    /**
     *
     * @return
     */
    public int[] getGenders() {
        return genders;
    }

    /**
     *
     * @param gender
     */
    public void setGenders(int[] gender) {
        this.genders = gender;
    }

    /**
     *
     * @param i
     * @param amount
     */
    public void addToGender(int i, int amount) {
        this.genders[i] = this.genders[i] + amount;
    }
}
