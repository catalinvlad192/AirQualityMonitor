package com.example.airqualitymonitor.db.model;

public class DummyDbEntry
{
    public String id_;
    public String deviceName_;
    public String latitude_;
    public String longitude_;
    public String temperature_;
    public String humidity_;
    public String pressure_;
    public String co2_;
    public String altitude_;
    public String lastUpdate_;
    //public String cc = "";

    public String toString()
    {
        return id_ + " - Device: " + deviceName_
                +" Lat: " + latitude_
                + " Long: " + longitude_
                + " Temp: " + temperature_
                + " Humidity: " + humidity_
                + " Pressure: " + pressure_
                + " CO2: " + co2_
                + " Altitude: " + altitude_
                + "LastUpdate: " + lastUpdate_;
    }
}
