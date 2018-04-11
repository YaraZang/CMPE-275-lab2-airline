package edu.sjsu.cmpe275.lab2.reservation;

import java.util.ArrayList;
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
import edu.sjsu.cmpe275.lab2.flight.FlightService;
import edu.sjsu.cmpe275.lab2.passenger.Passenger;
import edu.sjsu.cmpe275.lab2.passenger.PassengerService;
import edu.sjsu.cmpe275.lab2.reservation.Reservation;


@RestController
public class ReservationController {
	
	@Autowired
	private ReservationService reservationService;
	@Autowired
	private PassengerService passengerService;
	@Autowired
	private FlightService flightService;
	
	
	@RequestMapping("/reservation/{number}")
	@JsonView(View.Reservation.class)
	public ResponseEntity<Reservation> getReservation(@PathVariable String number) {
		Reservation p = reservationService.getReservation(number);
		if(p == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested reservation with id "+ number +" does not exist.");
			
		}
		return ResponseEntity.ok().body(p);
	}
	
	//search reservation
	
	@JsonView(View.Reservation.class)
	@RequestMapping(method=RequestMethod.GET, value= "/reservation", produces={MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<List<Reservation>> searchReservation(@RequestParam(value="passengerId",required=false, defaultValue="") String passengerId,
														 @RequestParam(value="origin",required=false, defaultValue="") String origin,
														 @RequestParam(value="destination",required=false, defaultValue="") String destination,
														 @RequestParam(value="flightNumber",required=false, defaultValue="") String flightNumber
														) {
		List<Reservation> results = new ArrayList<>();
		List<Reservation> all_res = new ArrayList<>() ; // = reservationService.getAllReservations();
		if(!passengerId.isEmpty()) {
			Passenger p = passengerService.getPassenger(passengerId);
			if(p == null) {
				throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Passenger id not found.");
			}
			all_res = p.getReservations();
			
			if(origin.isEmpty() && destination.isEmpty() && flightNumber.isEmpty()) {
				//return ResponseEntity.ok().body(all_res);
				return new ResponseEntity<List<Reservation>>(all_res, HttpStatus.OK);
			}
		}
		else {
			all_res = reservationService.getAllReservations();
		}
		
		for(Reservation res:all_res) {
			List<Flight> flights = res.getFlights();
			for(Flight flight : flights) {
				if( (origin.isEmpty() || flight.getOrigin().equals(origin)) &&
					(destination.isEmpty() || flight.getDestination().equals(destination)) &&
					(flightNumber.isEmpty() || flight.getFlightNumber().equals(flightNumber))
						) {
					results.add(res);
					break;
				}
			}
		}
		if(results.isEmpty()) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "not find such combination info.");
		}
		//return ResponseEntity.ok().body(results);
		HttpHeaders httpHeaders = new HttpHeaders();;
		httpHeaders.setContentType(MediaType.APPLICATION_XML);
		return new ResponseEntity<List<Reservation>>(results, httpHeaders, HttpStatus.OK);
	}
	
	
	
	@JsonView(View.Reservation.class)
	@RequestMapping(method=RequestMethod.POST, value="/reservation", produces={MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Reservation> addReservation(@RequestParam("passengerId") String passengerId,
							 		  @RequestParam("flightLists") List<String> flightNumbers) {
		
		List<Flight> flights = new ArrayList<Flight>();
		for (String flightNumber: flightNumbers) {
			flights.add(flightService.getFlight(flightNumber));
		}
		
		// check seats available
		if(!_checkFlightsSeat(flights)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Seats not available.");
		}
		
		//time overlap
		// 1 check 
		if(!_checkSelfOverlap(flights)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Time overlap.");
		}
		
		// 2
		Passenger passenger = passengerService.getPassenger(passengerId);
		if(!_checkOverlapForPassenger(passenger, flights)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Time conflicts with existed reservations.");
		}
				
		for(Flight flight:flights) {
			flight.addPassenger(passenger);
		}
		Reservation r = new Reservation(passenger,flights);
		reservationService.addReservation(r);
		HttpHeaders httpHeaders = new HttpHeaders();;
		httpHeaders.setContentType(MediaType.APPLICATION_XML);
		return new ResponseEntity<Reservation>(r, httpHeaders, HttpStatus.OK);
	}
	
	@JsonView(View.Reservation.class)
	@RequestMapping(method=RequestMethod.POST, value="/reservation/{number}")
	public Reservation updateReservation(@PathVariable String number,
								  @RequestParam(value="flightsAdded",required=false, defaultValue="") List<String> flightsAdded,
								  @RequestParam(value="flightsRemoved",required=false, defaultValue="") List<String> flightsRemoved
								  ) {
		List<Flight> flightsAdd = new ArrayList<Flight>();
		for (String flightNumber: flightsAdded) {
			flightsAdd.add(flightService.getFlight(flightNumber));
		}
		
		List<Flight> flightsRm = new ArrayList<Flight>();
		for (String flightNumber: flightsRemoved) {
			flightsRm.add(flightService.getFlight(flightNumber));
		}
		
		// check seats available
		if(!_checkFlightsSeat(flightsAdd)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Seats not available.");
		}
				
		//time overlap
		// 1 check 
		if(!_checkSelfOverlap(flightsAdd)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Time overlap.");
		}
				
		// 2
		Reservation researvation =  reservationService.getReservation(number);
		Passenger passenger = researvation.getPassenger();
		if(!_checkOverlapForPassgengerWithExclude(passenger, flightsAdd, flightsRm)) {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Time conflicts with existed reservations.");
		}
		
		researvation.updateFlights(flightsAdd, flightsRm);
		for(Flight flight:flightsAdd) {
			flightService.updateFlight(flight);
		}
		for(Flight flight:flightsRm) {
			flightService.updateFlight(flight);
		}
		reservationService.updateReservation(researvation);
		return reservationService.getReservation(number);
	}
	
	

	@RequestMapping(method=RequestMethod.DELETE, value="/reservation/{number}")
	public ResponseEntity<Object> deleteReservation(@PathVariable String number) {
		Reservation r = reservationService.getReservation(number);
		if(r == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested reservation with id "+ number +" does not exist.");
			
		}
		Passenger p = r.getPassenger();
		List<Flight> flightList = r.getFlights();
		for(Flight flight : flightList) {
			flight.deletePassenger(p);
		}
		
		reservationService.deleteReservation(number);
		ExceptionJSONInfo info = new ExceptionJSONInfo();
		info.setCode(200);
		info.setMsg("Reservation with number "+ number +" is canceled successfully.");
		return new ResponseEntity<Object>(info,HttpStatus.OK );
	}

	private boolean _checkFlightsSeat(List<Flight> flights) {
		for(Flight flight : flights) {
			if(flight.getSeatsLeft() == 0) {
				return false;
			}
		}
		return true;
	}
	
	private boolean _checkSelfOverlap(List<Flight> flights) {
			for(int i = 0; i < flights.size() - 1 ; ++i) {
				for(int j = i+1; j < flights.size(); ++j) {
					if (flights.get(i).compareFlight(flights.get(j)) ) {
						return false;
					}
				}
			}
			return true;
	}
	
	private boolean _checkOverlapForPassenger(Passenger p, List<Flight> flights) {
		List<Reservation> existedReservations = p.getReservations();
		//already existed flights
		List<Flight> existedFlights = new ArrayList<>();
		for(Reservation res : existedReservations) {
			existedFlights.addAll(res.getFlights());
		}		
		//compare existed flights and chosen flights, check if there is time overlap	
		for(Flight candidate_flight : flights) {
			for(Flight eachFlight : existedFlights) {
				if(candidate_flight.compareFlight(eachFlight)) {
					return false;
				}				
			}			
		}
		return true;
	}
	
	private boolean _checkOverlapForPassgengerWithExclude(Passenger p, List<Flight> flights, List<Flight> exclude_flights) {
		List<Reservation> existedReservations = p.getReservations();
		//already existed flights
		List<Flight> existedFlights = new ArrayList<>();
		for(Reservation res : existedReservations) {
			existedFlights.addAll(res.getFlights());
		}		
		//compare existed flights and chosen flights, check if there is time overlap	
		for(Flight candidate_flight : flights) {
			for(Flight eachFlight : existedFlights) {
				if(exclude_flights.contains(eachFlight)) continue;
				if(candidate_flight.compareFlight(eachFlight)) {
					return false;
				}				
			}			
		}
		return true;
	}

}
