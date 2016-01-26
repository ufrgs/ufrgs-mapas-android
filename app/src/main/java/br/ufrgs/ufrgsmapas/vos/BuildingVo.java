package br.ufrgs.ufrgsmapas.vos;

/**
 * Holds information about one building
 * @author alan
 */
public class BuildingVo {
    public int id;
    public double latitude;
    public double longitude;

    public String name;
    public int image;
    public String ufrgsBuildingCode;
    public String buildingAddress;
    public String buildingAddressNumber;
    public String zipCode;
    public String neighborhood;
    public String city;
    public String state;

    public int campusCode;
    public String isExternalBuilding;
    public String description;
    public String phone;
    public String isHistorical;
    public String locationUrl;

    public boolean isStarred;
    /** Check if the image is the image of a building. If false, probably it is a placeholder */
    public boolean isABuildingImage;


    public BuildingVo(int id, double latitude, double longitude){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;

        this.isStarred = false;
    }

}