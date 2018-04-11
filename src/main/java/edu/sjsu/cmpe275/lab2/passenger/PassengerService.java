package edu.sjsu.cmpe275.lab2.passenger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PassengerService {
	
	@Autowired
	private PassengerRepository passengerRepository;
	
	public List<Passenger> getAllPassengers(){
		List<Passenger> passengers = new ArrayList<>();
		passengerRepository.findAll().forEach(passengers::add);
		return passengers;
	}
	
	public Passenger getPassenger(String id) {
		return passengerRepository.findById(id).orElse(null);
	}
	
	public void addPassenger(Passenger p) {
		passengerRepository.save(p);
	}
	
	public Passenger updatePassenger(Passenger p) {
		return passengerRepository.save(p);
	}
	
	public void deletePassenger(String id) {
		passengerRepository.deleteById(id);
	}
	

}
