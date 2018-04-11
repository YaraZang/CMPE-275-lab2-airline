package edu.sjsu.cmpe275.lab2.flight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.sjsu.cmpe275.lab2.flight.Flight;
import edu.sjsu.cmpe275.lab2.flight.FlightRepository;

@Service
public class FlightService {
	
	@Autowired
	private FlightRepository flightRepository;
	
	public Flight getFlight(String id) {
		return flightRepository.findById(id).orElse(null);
	}
	
	public void addFlight(Flight f) {
		flightRepository.save(f);
	}
	
	public Flight updateFlight(Flight f) {
		return flightRepository.save(f);
	}
	
	public void deleteFlight(String id) {
		flightRepository.deleteById(id);
	}
	

}
