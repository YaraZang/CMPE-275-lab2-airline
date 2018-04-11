package edu.sjsu.cmpe275.lab2.passenger;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import edu.sjsu.cmpe275.lab2.passenger.Passenger;

@RestController
public class PassengerController {
	
	
	@Autowired
	private PassengerService passengerService;
	
	@JsonView(View.Passenger.class)
	@RequestMapping(method=RequestMethod.GET, value="/passenger/{id}", produces={MediaType.APPLICATION_JSON_VALUE, 
            								 MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Passenger> getPassenger(@PathVariable String id,
			 									  @RequestParam(value="xml",required=false, defaultValue="false") String xmlEnabled) {
		Passenger p = passengerService.getPassenger(id);
		if(p == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND,"Sorry, the requested passenger with id "+id+" does not exist.");
		}
		HttpHeaders httpHeaders = new HttpHeaders();;
		if(xmlEnabled.equals("true")) {
			httpHeaders.setContentType(MediaType.APPLICATION_XML);
		}
		else {
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}
		return new ResponseEntity<Passenger>(p, httpHeaders, HttpStatus.OK);
	}
	
	
	@JsonView(View.Passenger.class)
	@RequestMapping(method=RequestMethod.POST, value="/passenger", produces={MediaType.APPLICATION_JSON_VALUE, 
			 													   MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Passenger> addPassenger(@RequestParam("firstname") String firstname,
							 @RequestParam("lastname") String lastname,
							 @RequestParam("age") int age,
							 @RequestParam("gender") String gender,
							 @RequestParam("phone") String phone,
							 @RequestParam(value="xml",required=false, defaultValue="false") String xmlEnabled) {		
		List<Passenger> allPassengers = passengerService.getAllPassengers();
		for(Passenger passenger : allPassengers) {
			if(phone.equals(passenger.getPhone())) {
				throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Sorry, the phone num " + phone + " already exists.");
			}
		}
		Passenger p = new Passenger(firstname,lastname,age,gender,phone);
		passengerService.addPassenger(p);
		
		HttpHeaders httpHeaders = new HttpHeaders();;
		if(xmlEnabled.equals("true")) {
			httpHeaders.setContentType(MediaType.APPLICATION_XML);
		}
		else {
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}
		return new ResponseEntity<Passenger>(p, httpHeaders, HttpStatus.OK);
	}
	
	@JsonView(View.Passenger.class)
	@RequestMapping(method=RequestMethod.PUT, value="/passenger/{id}", produces={MediaType.APPLICATION_JSON_VALUE, 
			   														   MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Passenger> updatePassenger(@PathVariable String id,
								@RequestParam("firstname") String firstname,
								@RequestParam("lastname") String lastname,
								@RequestParam("age") int age,
								@RequestParam("gender") String gender,
								@RequestParam("phone") String phone,
								@RequestParam(value="xml",required=false, defaultValue="false") String xmlEnabled
							   ) {
		List<Passenger> allPassengers = passengerService.getAllPassengers();
		for(Passenger passenger : allPassengers) {
			if(phone.equals(passenger.getPhone())) {
				throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Sorry, the phone num " + phone + " already exists.");
			}
		}
		
		Passenger p = passengerService.getPassenger(id);
		if(p == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested passenger with id "+id+" does not exist.");
			
		}
		p.setFirstname(firstname);
		p.setLastname(lastname);
		p.setAge(age);
		p.setGender(gender);
		p.setPhone(phone);
		passengerService.updatePassenger(p);
		HttpHeaders httpHeaders = new HttpHeaders();;
		if(xmlEnabled.equals("true")) {
			httpHeaders.setContentType(MediaType.APPLICATION_XML);
		}
		else {
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}
		return new ResponseEntity<Passenger>(p, httpHeaders, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/passenger/{id}")
	public ResponseEntity<Object> deletePassenger(@PathVariable String id) {
		Passenger p = passengerService.getPassenger(id);
		if(p == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested passenger with id "+id+" does not exist.");
			
		}
		List<Flight> flightList = p._getFlightList();
		for(Flight flight : flightList) {
			flight.deletePassenger(p);
		}
		passengerService.deletePassenger(id);
		
		ExceptionJSONInfo info = new ExceptionJSONInfo();
		info.setCode(200);
		info.setMsg("Passenger with id " + id + " is deleted successfully");
		return new ResponseEntity<Object>(info,HttpStatus.OK );
	}


}
