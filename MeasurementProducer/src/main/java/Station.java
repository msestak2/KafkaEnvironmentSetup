public class Station {
    private String stationId;

    private String latitude;

    private String longitude;

    private String stationLocation;

    public Station(String stationId, String latitude, String longitude, String stationLocation) {
        this.stationId = stationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stationLocation = stationLocation;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStationLocation() {
        return stationLocation;
    }

    public void setStationLocation(String stationLocation) {
        this.stationLocation = stationLocation;
    }
}
