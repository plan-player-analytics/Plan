package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.store.mutators.PlayersMutator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * World Map that uses iso-a3 specification of Country codes.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class WorldMap implements HighChart {

    private final List<String> geoLocations;

    public WorldMap(List<String> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public WorldMap(PlayersMutator mutator) {
        this(mutator.getGeolocations());
    }

    private static Map<String, String> getGeoCodes(Map<String, Integer> geoCodeCounts) {
        Map<String, String> geoCodes = new HashMap<>();
        // Countries & Codes have been copied from a iso-a3 specification file.
        // Each index corresponds to each code.
        String[] countries = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas, The", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Democratic Republic of the", "Congo, Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Islas Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia", "Gabon", "Gambia, The", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guam", "Guatemala", "Guernsey", "Guinea-Bissau", "Guinea", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "republic of Korea", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia, Federated States of", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Namibia", "Nepal", "Netherlands", "New Caledonia", "New Zealand", "Nicaragua", "Nigeria", "Niger", "Niue", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Puerto Rico", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands", "West Bank", "Yemen", "Zambia", "Zimbabwe"};
        String[] codes = new String[]{"AFG", "ALB", "DZA", "ASM", "AND", "AGO", "AIA", "ATG", "ARG", "ARM", "ABW", "AUS", "AUT", "AZE", "BHM", "BHR", "BGD", "BRB", "BLR", "BEL", "BLZ", "BEN", "BMU", "BTN", "BOL", "BIH", "BWA", "BRA", "VGB", "BRN", "BGR", "BFA", "MMR", "BDI", "CPV", "KHM", "CMR", "CAN", "CYM", "CAF", "TCD", "CHL", "CHN", "COL", "COM", "COD", "COG", "COK", "CRI", "CIV", "HRV", "CUB", "CUW", "CYP", "CZE", "DNK", "DJI", "DMA", "DOM", "ECU", "EGY", "SLV", "GNQ", "ERI", "EST", "ETH", "FLK", "FRO", "FJI", "FIN", "FRA", "PYF", "GAB", "GMB", "GEO", "DEU", "GHA", "GIB", "GRC", "GRL", "GRD", "GUM", "GTM", "GGY", "GNB", "GIN", "GUY", "HTI", "HND", "HKG", "HUN", "ISL", "IND", "IDN", "IRN", "IRQ", "IRL", "IMN", "ISR", "ITA", "JAM", "JPN", "JEY", "JOR", "KAZ", "KEN", "KIR", "KOR", "PRK", "KSV", "KWT", "KGZ", "LAO", "LVA", "LBN", "LSO", "LBR", "LBY", "LIE", "LTU", "LUX", "MAC", "MKD", "MDG", "MWI", "MYS", "MDV", "MLI", "MLT", "MHL", "MRT", "MUS", "MEX", "FSM", "MDA", "MCO", "MNG", "MNE", "MAR", "MOZ", "NAM", "NPL", "NLD", "NCL", "NZL", "NIC", "NGA", "NER", "NIU", "MNP", "NOR", "OMN", "PAK", "PLW", "PAN", "PNG", "PRY", "PER", "PHL", "POL", "PRT", "PRI", "QAT", "ROU", "RUS", "RWA", "KNA", "LCA", "MAF", "SPM", "VCT", "WSM", "SMR", "STP", "SAU", "SEN", "SRB", "SYC", "SLE", "SGP", "SXM", "SVK", "SVN", "SLB", "SOM", "ZAF", "SSD", "ESP", "LKA", "SDN", "SUR", "SWZ", "SWE", "CHE", "SYR", "TWN", "TJK", "TZA", "THA", "TLS", "TGO", "TON", "TTO", "TUN", "TUR", "TKM", "TUV", "UGA", "UKR", "ARE", "GBR", "USA", "URY", "UZB", "VUT", "VEN", "VNM", "VGB", "WBG", "YEM", "ZMB", "ZWE"};
        for (int i = 0; i < countries.length; i++) {
            String country = countries[i];
            String countryCode = codes[i];

            geoCodes.put(country.toLowerCase(), countryCode);
            geoCodeCounts.put(countryCode, 0);
        }
        return geoCodes;
    }

    @Override
    public String toHighChartsSeries() {
        Map<String, Integer> geoCodeCounts = new HashMap<>();
        Map<String, String> geoCodes = getGeoCodes(geoCodeCounts);

        for (String geoLocation : geoLocations) {
            String countryCode = geoCodes.get(geoLocation.toLowerCase());
            if (countryCode != null) {
                geoCodeCounts.computeIfPresent(countryCode, (computedCountry, amount) -> amount + 1);
            }
        }

        StringBuilder dataBuilder = new StringBuilder("[");

        int i = 0;
        int size = geoCodeCounts.size();
        for (Map.Entry<String, Integer> entry : geoCodeCounts.entrySet()) {
            String geoCode = entry.getKey();
            Integer players = entry.getValue();

            if (players != 0) {
                dataBuilder.append("{'code':'").append(geoCode).append("','value':").append(players).append("}");
                if (i < size - 1) {
                    dataBuilder.append(",");
                }
            }

            i++;
        }

        dataBuilder.append("]");
        return dataBuilder.toString();
    }
}
