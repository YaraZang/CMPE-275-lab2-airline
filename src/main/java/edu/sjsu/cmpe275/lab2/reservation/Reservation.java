package edu.sjsu.cmpe275.lab2.reservation;

import java.util.List;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.lab2.View;
import edu.sjsu.cmpe275.lab2.flight.Flight;
import edu.sjsu.cmpe275.lab2.passenger.Passenger;


@Entity
//@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Reservation.class)
public class Reservation {
	
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@JsonView({View.Passenger.class,View.Reservation.class} )
	private String reservationNumber;
	
	@JsonView({View.Passenger.class,View.Reservation.class} )
    private double price; // sum of each flight’s price.
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="passenger_id" )
	@JsonView(View.Reservation.class)
    private Passenger passenger;

    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
    	    name="reservation_flights",  	    		
    	    joinColumns= {@JoinColumn(name="reservation_reservation_number",referencedColumnName="reservationNumber")},
    	    inverseJoinColumns= {@JoinColumn(name="flights_flight_number",referencedColumnName="flightNumber")}
    	    )
    @JsonView({View.Passenger.class,View.Reservation.class} )
    private List<Flight> flights;
    
    
    public Reservation() {
	}
    
	public Reservation(Passenger passenger, List<Flight> flights) {
		super();
		this.passenger = passenger;
		this.flights = flights;
		this.price = 0;
		for(Flight flight:flights) {
			this.price += flight.getPrice();
		}
	}

	public String getReservationNumber() {
		return reservationNumber;
	}

	public Passenger getPassenger() {
		return passenger;
	}

	public void setPassenger(Passenger passenger) {
		this.passenger = passenger;
	}

	public double getPrice() {
		
		return this.price;
	}
	
	public void setPrice() {
		this.price = 0;
		for(Flight flight:flights) {
			this.price += flight.getPrice();
		}
	}


	public List<Flight> getFlights() {
		return flights;
	}

	public void setFlights(List<Flight> flights) {
		this.flights = flights;
	}
	
	public void updateFlights(List<Flight> flightsAdd, List<Flight> flightsRm) {
		this.flights.removeAll(flightsRm);
		this.flights.addAll(flightsAdd);
		this.setPrice();
		for(Flight flight:flightsAdd) {
			flight.addPassenger(this.passenger);
		}
		for(Flight flight:flightsRm) {
			flight.deletePassenger(this.passenger);
		}
	}
}
