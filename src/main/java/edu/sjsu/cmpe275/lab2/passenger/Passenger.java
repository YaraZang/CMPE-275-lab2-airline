package edu.sjsu.cmpe275.lab2.passenger;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.lab2.View;
import edu.sjsu.cmpe275.lab2.flight.Flight;
import edu.sjsu.cmpe275.lab2.reservation.Reservation;

@Entity

public class Passenger {
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
	private String id;   
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String firstname;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String lastname;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private int age;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String gender;
	@JsonView({View.Passenger.class,View.Reservation.class, View.Flight.class} )
    private String phone; // Phone numbers must be unique
    

    @OneToMany(cascade = { CascadeType.ALL },mappedBy="passenger",fetch=FetchType.LAZY)
    @JsonView(View.Passenger.class)
    private List<Reservation> reservations;
    
    @ManyToMany(mappedBy="passengers",fetch=FetchType.LAZY)
    private List<Flight> flights;
    
    public Passenger() {
    	
    }
    
	public Passenger(String firstname, String lastname, int age, String gender, String phone) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		this.gender = gender;
		this.phone = phone;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<Flight> _getFlightList(){
		return this.flights;
	}
	
	public List<Reservation> getReservations(){
		return this.reservations;
	}
	
	

}
