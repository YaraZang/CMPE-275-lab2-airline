package edu.sjsu.cmpe275.lab2.flight;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.lab2.View;

@Embeddable
public class Plane {
	
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
	private int capacity;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String model;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String manufacturer;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private int year;
    
    public Plane() {
	}
    
    
	public Plane(int capacity, String model, String manufacturer, int year) {
		super();
		this.capacity = capacity;
		this.model = model;
		this.manufacturer = manufacturer;
		this.year = year;
	}


	public int getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	public String getModel() {
		return model;
	}


	public void setModel(String model) {
		this.model = model;
	}


	public String getManufacturer() {
		return manufacturer;
	}


	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}

	

}
