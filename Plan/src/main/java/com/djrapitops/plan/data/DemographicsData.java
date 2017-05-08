package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;

/**
 * This class is used to store Demographics data inside the UserData.
 *
 * Originally these data points were created by Plade (Player Demographics).
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class DemographicsData {

    private int age;
    private Gender gender;
    private String geoLocation;

    /**
     * Creates demographics data object from existing data.
     *
     * @param age Age, -1 if unknown
     * @param gender Gender Enum.
     * @param geoLocation Name of the geolocation. Phrase.DEM_UNKNOWN if not
     * known.
     */
    public DemographicsData(int age, Gender gender, String geoLocation) {
        this.age = age;
        this.gender = gender;
        this.geoLocation = geoLocation;
    }

    /**
     * Creates new demographics data object with default parameters.
     */
    public DemographicsData() {
        this(-1, Gender.UNKNOWN, Phrase.DEM_UNKNOWN + "");
    }

    /**
     * Get the age of the player.
     *
     * @return 1 to 99, -1 if not known
     */
    public int getAge() {
        return age;
    }

    /**
     * Get the gender of the player.
     *
     * @return Gender Enum of the Player. UNKNOWN if not known
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Get the geolocation string.
     *
     * @return Geolocation string of the player Phrase.DEM_UNKNOWN if not known.
     */
    public String getGeoLocation() {
        return geoLocation;
    }

    /**
     * Set the age of the player.
     *
     * @param age 0 to 99, -1 if not known.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Set the gender of the player.
     *
     * @param gender Gender Enum. UNKNOWN if not known.
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Set the Geolocation of the player.
     *
     * LoginHandling.updateGeolocation() is used to get the geolocation
     * information.
     *
     * @param geoLocation Country name, eg. Republic of Kongo, the
     * @see LoginHandling
     */
    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String toString() {
        return "{" + "age:" + age + "|gender:" + gender + "|geoLocation:" + geoLocation + '}';
    }
}
