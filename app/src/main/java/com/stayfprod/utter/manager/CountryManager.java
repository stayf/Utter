package com.stayfprod.utter.manager;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.model.Country;

import java.util.Locale;

public class CountryManager {

    private static final String LOG = CountryManager.class.getSimpleName();

    public static final Country[] COUNTRIES = new Country[]
            {
                    new Country(93, "Afghanistan", "AF", "A"),
                    new Country(355, "Albania", "AL"),
                    new Country(213, "Algeria", "DZ"),
                    new Country(1684, "American Samoa", "AS"),
                    new Country(376, "Andorra", "AD"),
                    new Country(244, "Angola", "AO"),
                    new Country(1264, "Anguilla", "AI"),
                    new Country(1268, "Antigua & Barbuda", "AG"),
                    new Country(54, "Argentina", "AR"),
                    new Country(374, "Armenia", "AM"),
                    new Country(297, "Aruba", "AW"),
                    new Country(61, "Australia", "AU"),
                    new Country(43, "Austria", "AT"),
                    new Country(994, "Azerbaijan", "AZ"),
                    new Country(1242, "Bahamas", "BS", "B"),
                    new Country(973, "Bahrain", "BH"),
                    new Country(880, "Bangladesh", "BD"),
                    new Country(1246, "Barbados", "BB"),
                    new Country(375, "Belarus", "BY"),
                    new Country(32, "Belgium", "BE"),
                    new Country(501, "Belize", "BZ"),
                    new Country(229, "Benin", "BJ"),
                    new Country(1441, "Bermuda", "BM"),
                    new Country(975, "Bhutan", "BT"),
                    new Country(591, "Bolivia", "BO"),
                    new Country(599, "Bonaire, Sint Eustatius & Saba", "BQ"),
                    new Country(387, "Bosnia & Herzegovina", "BA"),
                    new Country(267, "Botswana", "BW"),
                    new Country(55, "Brazil", "BR"),
                    new Country(1284, "British Virgin Islands", "VG"),
                    new Country(673, "Brunei Darussalam", "BN"),
                    new Country(359, "Bulgaria", "BG"),
                    new Country(226, "Burkina Faso", "BF"),
                    new Country(257, "Burundi", "BI"),
                    new Country(855, "Cambodia", "KH", "C"),
                    new Country(237, "Cameroon", "CM"),
                    new Country(1, "Canada", "CA"),
                    new Country(238, "Cape Verde", "CV"),
                    new Country(1345, "Cayman Islands", "KY"),
                    new Country(236, "Central African Rep.", "CF"),
                    new Country(235, "Chad", "TD"),
                    new Country(56, "Chile", "CL"),
                    new Country(86, "China", "CN"),
                    new Country(57, "Colombia", "CO"),
                    new Country(269, "Comoros", "KM"),
                    new Country(243, "Congo (Dem. Rep.)", "CD"),
                    new Country(242, "Congo (Rep.)", "CG"),
                    new Country(682, "Cook Islands", "CK"),
                    new Country(506, "Costa Rica", "CR"),
                    new Country(225, "Cote d`Ivoire", "CI"),
                    new Country(385, "Croatia", "HR"),
                    new Country(53, "Cuba", "CU"),
                    new Country(599, "Curacao", "CW"),
                    new Country(357, "Cyprus", "CY"),
                    new Country(420, "Czech Republic", "CZ"),
                    new Country(45, "Denmark", "DK", "D"),
                    new Country(246, "Diego Garcia", "IO"),
                    new Country(253, "Djibouti", "DJ"),
                    new Country(1767, "Dominica", "DM"),
                    new Country(1, "Dominican Rep.", "DO"),
                    new Country(593, "Ecuador", "EC", "E"),
                    new Country(20, "Egypt", "EG"),
                    new Country(503, "El Salvador", "SV"),
                    new Country(240, "Equatorial Guinea", "GQ"),
                    new Country(291, "Eritrea", "ER"),
                    new Country(372, "Estonia", "EE"),
                    new Country(251, "Ethiopia", "ET"),
                    new Country(500, "Falkland Islands", "FK", "F"),
                    new Country(298, "Faroe Islands", "FO"),
                    new Country(679, "Fiji", "FJ"),
                    new Country(358, "Finland", "FI"),
                    new Country(33, "France", "FR"),
                    new Country(594, "French Guiana", "GF"),
                    new Country(689, "French Polynesia", "PF"),
                    new Country(241, "Gabon", "GA", "G"),
                    new Country(220, "Gambia", "GM"),
                    new Country(995, "Georgia", "GE"),
                    new Country(49, "Germany", "DE"),
                    new Country(233, "Ghana", "GH"),
                    new Country(350, "Gibraltar", "GI"),
                    new Country(30, "Greece", "GR"),
                    new Country(299, "Greenland", "GL"),
                    new Country(1473, "Grenada", "GD"),
                    new Country(590, "Guadeloupe", "GP"),
                    new Country(1671, "Guam", "GU"),
                    new Country(502, "Guatemala", "GT"),
                    new Country(224, "Guinea", "GN"),
                    new Country(245, "Guinea-Bissau", "GW"),
                    new Country(592, "Guyana", "GY"),
                    new Country(509, "Haiti", "HT", "H"),
                    new Country(504, "Honduras", "HN"),
                    new Country(852, "Hong Kong", "HK"),
                    new Country(36, "Hungary", "HU"),
                    new Country(354, "Iceland", "IS", "I"),
                    new Country(91, "India", "IN"),
                    new Country(62, "Indonesia", "ID"),
                    new Country(98, "Iran", "IR"),
                    new Country(964, "Iraq", "IQ"),
                    new Country(353, "Ireland", "IE"),
                    new Country(972, "Israel", "IL"),
                    new Country(39, "Italy", "IT"),
                    new Country(1876, "Jamaica", "JM", "J"),
                    new Country(81, "Japan", "JP"),
                    new Country(962, "Jordan", "JO"),
                    new Country(7, "Kazakhstan", "KZ", "K"),
                    new Country(254, "Kenya", "KE"),
                    new Country(686, "Kiribati", "KI"),
                    new Country(965, "Kuwait", "KW"),
                    new Country(996, "Kyrgyzstan", "KG"),
                    new Country(856, "Laos", "LA", "L"),
                    new Country(371, "Latvia", "LV"),
                    new Country(961, "Lebanon", "LB"),
                    new Country(266, "Lesotho", "LS"),
                    new Country(231, "Liberia", "LR"),
                    new Country(218, "Libya", "LY"),
                    new Country(423, "Liechtenstein", "LI"),
                    new Country(370, "Lithuania", "LT"),
                    new Country(352, "Luxembourg", "LU"),
                    new Country(853, "Macau", "MO", "M"),
                    new Country(389, "Macedonia", "MK"),
                    new Country(261, "Madagascar", "MG"),
                    new Country(265, "Malawi", "MW"),
                    new Country(60, "Malaysia", "MY"),
                    new Country(960, "Maldives", "MV"),
                    new Country(223, "Mali", "ML"),
                    new Country(356, "Malta", "MT"),
                    new Country(692, "Marshall Islands", "MH"),
                    new Country(596, "Martinique", "MQ"),
                    new Country(222, "Mauritania", "MR"),
                    new Country(230, "Mauritius", "MU"),
                    new Country(52, "Mexico", "MX"),
                    new Country(691, "Micronesia", "FM"),
                    new Country(373, "Moldova", "MD"),
                    new Country(377, "Monaco", "MC"),
                    new Country(976, "Mongolia", "MN"),
                    new Country(382, "Montenegro", "ME"),
                    new Country(1664, "Montserrat", "MS"),
                    new Country(212, "Morocco", "MA"),
                    new Country(258, "Mozambique", "MZ"),
                    new Country(95, "Myanmar", "MM"),
                    new Country(264, "Namibia", "NA", "N"),
                    new Country(674, "Nauru", "NR"),
                    new Country(977, "Nepal", "NP"),
                    new Country(31, "Netherlands", "NL"),
                    new Country(687, "New Caledonia", "NC"),
                    new Country(64, "New Zealand", "NZ"),
                    new Country(505, "Nicaragua", "NI"),
                    new Country(227, "Niger", "NE"),
                    new Country(234, "Nigeria", "NG"),
                    new Country(683, "Niue", "NU"),
                    new Country(672, "Norfolk Island", "NF"),
                    new Country(850, "North Korea", "KP"),
                    new Country(1670, "Northern Mariana Islands", "MP"),
                    new Country(47, "Norway", "NO"),
                    new Country(968, "Oman", "OM", "O"),
                    new Country(92, "Pakistan", "PK", "P"),
                    new Country(680, "Palau", "PW"),
                    new Country(970, "Palestine", "PS"),
                    new Country(507, "Panama", "PA"),
                    new Country(675, "Papua New Guinea", "PG"),
                    new Country(595, "Paraguay", "PY"),
                    new Country(51, "Peru", "PE"),
                    new Country(63, "Philippines", "PH"),
                    new Country(48, "Poland", "PL"),
                    new Country(351, "Portugal", "PT"),
                    new Country(1, "Puerto Rico", "PR"),
                    new Country(974, "Qatar", "QA", "Q"),
                    new Country(262, "Reunion", "RE", "R"),
                    new Country(40, "Romania", "RO"),
                    new Country(7, "Russian Federation", "RU"),
                    new Country(250, "Rwanda", "RW"),
                    new Country(290, "Saint Helena", "SH", "S"),
                    new Country(247, "Saint Helena", "SH"),
                    new Country(1869, "Saint Kitts & Nevis", "KN"),
                    new Country(1758, "Saint Lucia", "LC"),
                    new Country(508, "Saint Pierre & Miquelon", "PM"),
                    new Country(1784, "Saint Vincent & the Grenadines", "VC"),
                    new Country(685, "Samoa", "WS"),
                    new Country(378, "San Marino", "SM"),
                    new Country(239, "Sao Tome & Principe", "ST"),
                    new Country(966, "Saudi Arabia", "SA"),
                    new Country(221, "Senegal", "SN"),
                    new Country(381, "Serbia", "RS"),
                    new Country(248, "Seychelles", "SC"),
                    new Country(232, "Sierra Leone", "SL"),
                    new Country(65, "Singapore", "SG"),
                    new Country(1721, "Sint Maarten", "SX"),
                    new Country(421, "Slovakia", "SK"),
                    new Country(386, "Slovenia", "SI"),
                    new Country(677, "Solomon Islands", "SB"),
                    new Country(252, "Somalia", "SO"),
                    new Country(27, "South Africa", "ZA"),
                    new Country(82, "South Korea", "KR"),
                    new Country(211, "South Sudan", "SS"),
                    new Country(34, "Spain", "ES"),
                    new Country(94, "Sri Lanka", "LK"),
                    new Country(249, "Sudan", "SD"),
                    new Country(597, "Suriname", "SR"),
                    new Country(268, "Swaziland", "SZ"),
                    new Country(46, "Sweden", "SE"),
                    new Country(41, "Switzerland", "CH"),
                    new Country(963, "Syrian Arab Republic", "SY"),
                    new Country(886, "Taiwan", "TW", "T"),
                    new Country(992, "Tajikistan", "TJ"),
                    new Country(255, "Tanzania", "TZ"),
                    new Country(66, "Thailand", "TH"),
                    new Country(670, "Timor-Leste", "TL"),
                    new Country(228, "Togo", "TG"),
                    new Country(690, "Tokelau", "TK"),
                    new Country(676, "Tonga", "TO"),
                    new Country(1868, "Trinidad & Tobago", "TT"),
                    new Country(216, "Tunisia", "TN"),
                    new Country(90, "Turkey", "TR"),
                    new Country(993, "Turkmenistan", "TM"),
                    new Country(1649, "Turks & Caicos Islands", "TC"),
                    new Country(688, "Tuvalu", "TV"),
                    new Country(1340, "US Virgin Islands", "VI", "U"),
                    new Country(1, "USA", "US"),
                    new Country(256, "Uganda", "UG"),
                    new Country(380, "Ukraine", "UA"),
                    new Country(971, "United Arab Emirates", "AE"),
                    new Country(44, "United Kingdom", "GB"),
                    new Country(598, "Uruguay", "UY"),
                    new Country(998, "Uzbekistan", "UZ"),
                    new Country(678, "Vanuatu", "VU", "V"),
                    new Country(58, "Venezuela", "VE"),
                    new Country(84, "Vietnam", "VN"),
                    new Country(681, "Wallis & Futuna", "WF", "W"),
                    new Country(42, "Y-land", "YL", "Y"),
                    new Country(967, "Yemen", "YE"),
                    new Country(260, "Zambia", "ZM", "Z"),
                    new Country(263, "Zimbabwe", "ZW")
            };

    /**
     * alpha-2
     */
    private static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            } else {
                return context.getResources().getConfiguration().locale.getCountry();
            }
        } catch (Exception e) {
            Log.e(LOG, "getUserCountry", e);
            Crashlytics.logException(e);
        }
        return null;
    }

    public static Country defineCountry(Context context) {
        String alpha2 = getUserCountry(context);

        if (alpha2 != null) {
            for (int i = 0; i < COUNTRIES.length; i++) {
                Country country = COUNTRIES[i];
                if (country.alpha2.equalsIgnoreCase(alpha2)) {
                    return country;
                }
            }
        }
        return COUNTRIES[231];
    }
}
