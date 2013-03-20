package com.example.gps;

public class GPSPoint {
	private String lat;
	private String lon;
	private String alt;
	private String acc;
	private String provider;
	private String id;
	private String date;
	private String time;
	private String city;
	private String street;
	private String country;

	public GPSPoint(String lat, String lon, String alt, String acc, String provider, String id, String date, String time, String city,
			String street, String country) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
		this.acc = acc;
		this.provider = provider;
		this.id = id;
		this.date = date;
		this.time = time;
		this.city = city;
		this.street = street;
		this.country = country;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}
	
	public String getAlt(){
		return alt;
	}
	
	public String getAcc() {
		return acc;
	}
	
	public String getProvider() {
		return provider;
	}

	public String getId() {
		return id;
	}

	public String getDate() {
		return date;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getCountry() {
		return country;
	}
}
