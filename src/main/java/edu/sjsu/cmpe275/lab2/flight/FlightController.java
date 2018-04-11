package edu.sjsu.cmpe275.lab2.flight;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.lab2.View;
import edu.sjsu.cmpe275.lab2.exception.CustomRestExceptionHandler;
import edu.sjsu.cmpe275.lab2.exception.ExceptionJSONInfo;
import edu.sjsu.cmpe275.lab2.flight.Flight;
import edu.sjsu.cmpe275.lab2.flight.FlightService;
import edu.sjsu.cmpe275.lab2.passenger.Passenger;


@RestController
public class FlightController {
	
	@Autowired
	private FlightService flightService;

	@JsonView(View.Flight.class)
	@RequestMapping(method=RequestMethod.GET, value = "/flight/{flightNumber}", produces={MediaType.APPLICATION_JSON_VALUE, 
			   													MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Flight> getFlight(@PathVariable String flightNumber,
				@RequestParam(value="xml",required=false, defaultValue="false") String xmlEnabled) {
		Flight f = flightService.getFlight(flightNumber);
		if(f == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested flight with id "+ flightNumber +" does not exist.");
			
		}
		HttpHeaders httpHeaders = new HttpHeaders();;
		if(xmlEnabled.equals("true")) {
			httpHeaders.setContentType(MediaType.APPLICATION_XML);
		}
		else {
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}
		return new ResponseEntity<Flight>(f, httpHeaders, HttpStatus.OK);
	}
	
	@JsonView(View.Flight.class)
	@RequestMapping(method=RequestMethod.POST, value="/flight/{flightNumber}", produces={MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Flight> addFlight(@PathVariable String flightNumber,
							@RequestParam("price") double price,
							@RequestParam("origin") String origin,
							@RequestParam("destination") String destination,
							@RequestParam("description") String description,
							@RequestParam("departureTime") @DateTimeFormat(pattern = "yyyy-MM-dd-hh") Date departureTime,
							@RequestParam("arrivalTime") @DateTimeFormat(pattern = "yyyy-MM-dd-hh") Date arrivalTime,
							@RequestParam("capacity") int capacity,
							@RequestParam("model") String model,
							@RequestParam("manufacturer") String manufacturer,
							@RequestParam("year") int year
							) {
		
		Flight flight = flightService.getFlight(flightNumber);
		if(flight  == null) {
			System.out.println("empty!!!");
			int seatsLeft = capacity;
			flight = new Flight(flightNumber,price, origin, destination, departureTime, arrivalTime, seatsLeft, description, new Plane(capacity,model,manufacturer,year));
			flightService.addFlight(flight);
		}
		else {
			// revise
			System.out.println("revise!!!");
			boolean valid = flight.update(price, origin, destination, departureTime, arrivalTime, description, new Plane(capacity,model,manufacturer,year));
			if(valid) {
				flightService.updateFlight(flight);
			}
			else {
				throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Flight has conflict.");
			}
			
		}
		HttpHeaders httpHeaders = new HttpHeaders();;
		httpHeaders.setContentType(MediaType.APPLICATION_XML);
		return new ResponseEntity<Flight>(flight, httpHeaders, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/flight/{flightNumber}")
	public ResponseEntity<Object> deleteFlight(@PathVariable String flightNumber) {
		Flight f = flightService.getFlight(flightNumber);
		if(f == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested flight with id "+ flightNumber +" does not exist.");
			
		}

		Set<Passenger> passengerList = f.getPassengers();
		if(passengerList.isEmpty()) {
			flightService.deleteFlight(flightNumber);
			ExceptionJSONInfo info = new ExceptionJSONInfo();
			info.setCode(200);
			info.setMsg("Flight with number "+ flightNumber +" is canceled successfully.");
			return new ResponseEntity<Object>(info,HttpStatus.OK );
		}
		else {
			ExceptionJSONInfo info = new ExceptionJSONInfo();
			info.setCode(400);
			info.setMsg("Flight with number "+ flightNumber +" cannot be deleted due to having reservations.");
			return new ResponseEntity<Object>(info,HttpStatus.BAD_REQUEST);
		}
		
	}


}

