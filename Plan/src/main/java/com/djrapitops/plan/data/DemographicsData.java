package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.api.Gender;

/**
 *
 * @author Rsl1122
 */
public class DemographicsData {

    private int age;
    private Gender gender;
    private String geoLocation;

    /**
     * Creates demographics data object from existing data.
     *
     * @param age
     * @param gender
     * @param geoLocation
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
        this(-1, Gender.UNKNOWN, Phrase.DEM_UNKNOWN+"");
    }

    /**
     * @return Age of the player, -1 if not known
     */
    public int getAge() {
        return age;
    }

    /**
     * @return Gender Enum of the Player. UNKNOWN if not known
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * @return Geolocation string of the player "Not known" if not known.
     */
    public String getGeoLocation() {
        return geoLocation;
    }

    /**
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @param gender
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * @param geoLocation
     */
    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String toString() {
        return "{" + "age:" + age + "|gender:" + gender + "|geoLocation:" + geoLocation + '}';
    }

    
}
