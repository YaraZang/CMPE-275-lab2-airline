package edu.sjsu.cmpe275.lab2.flight;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import edu.sjsu.cmpe275.lab2.View;
import edu.sjsu.cmpe275.lab2.passenger.Passenger;
import edu.sjsu.cmpe275.lab2.reservation.Reservation;



@Entity
// @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Flight.class)
public class Flight {
	
	@Id
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
	private String flightNumber; // Each flight has a unique flight number.
	
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private double price;
	
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String origin;
	
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String destination;

    /*  Date format: yy-mm-dd-hh, do not include minutes and seconds.
    ** Example: 2018-03-22-19
    *The system only needs to supports PST. You can ignore other time zones.  
    */ 
	
    @JsonFormat(
    	      shape = JsonFormat.Shape.STRING,
    	      pattern = "yyyy-MM-dd-hh")
    @JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private Date departureTime; 

    @JsonFormat(
    	      shape = JsonFormat.Shape.STRING,
    	      pattern = "yyyy-MM-dd-hh")
    @JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private Date arrivalTime;
	
    @JsonView({View.Reservation.class, View.Flight.class})
    private int seatsLeft; 
	
    @JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String description;
	
    @Embedded
    @JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private Plane plane;  // Embedded
	
    @JsonView(View.Flight.class)
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
    		name="flight_passengers",
    		joinColumns= {@JoinColumn(name="flight_flight_number",referencedColumnName="flightNumber")},
    		inverseJoinColumns={@JoinColumn(name="passengers_id",referencedColumnName="id")})     		
    private Set<Passenger> passengers;
    
    @ManyToMany(mappedBy="flights",fetch=FetchType.LAZY)
    private List<Reservation> reservations;
   
    public Flight() {
		
	}
    
	public Flight(String flightNumber, double price, String origin, String destination, Date departureTime, Date arrivalTime,int seatsLeft,String description,Plane plane) {
		super();
		this.flightNumber = flightNumber;
		this.price = price;
		this.origin = origin;
		this.destination = destination;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.seatsLeft = seatsLeft;
		this.description = description;
		this.plane = plane;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getSeatsLeft() {
		return seatsLeft;
	}

	public void setSeatsLeft(int seatsLeft) {
		this.seatsLeft = seatsLeft;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Plane getPlane() {
		return plane;
	}

	public void setPlane(Plane plane) {
		this.plane = plane;
	}

	public Set<Passenger> getPassengers() {
		return passengers;
	}

	public void setPassengers(Set<Passenger> passengers) {
		this.passengers = passengers;
		this.setSeatsLeft(this.plane.getCapacity() - this.passengers.size());
	}

	public void addPassenger(Passenger passenger) {
		this.passengers.add(passenger);
		this.setSeatsLeft(this.plane.getCapacity() - this.passengers.size());
	}

	public void deletePassenger(Passenger passenger) {
		this.passengers.remove(passenger);
	}
	
	public boolean compareFlight(Flight flight) {
		return this.compareTime(flight.departureTime, flight.arrivalTime);
	}
	
	public boolean compareTime(Date departureTime,Date arrivalTime ) {
		Date maxDepartureTime = this.departureTime;
		Date minArrivalTime = this.arrivalTime;
		if(this.departureTime.compareTo(departureTime) <= 0) {
			maxDepartureTime = departureTime;
		}
		if(this.arrivalTime.compareTo(arrivalTime) >= 0) {
			minArrivalTime = arrivalTime;
		}
		if(maxDepartureTime.compareTo(minArrivalTime) <= 0) {
			return true;
		}
		return false;
	}
	
	public boolean update(double price,
						  String origin,
						  String destination,
						  Date departureTime,
						  Date arrivalTime,
						  String description,
						  Plane plane) {
		// capacity too small
		if(this.passengers.size() > plane.getCapacity()) {
			return false;
		}
		// cause overlap
		for(Passenger passenger : passengers) {
			List<Flight> flights = passenger._getFlightList();
			for(Flight flight : flights) {
				if(flight.flightNumber.equals(this.flightNumber)) continue;
				if(flight.compareTime(departureTime, arrivalTime)) {
					return false;
				}
			}
		}
		// update
		this.price = price;
		this.origin = origin;
		this.destination = destination;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.description = description;
		this.plane = plane;
		
		return true;
	}
}
