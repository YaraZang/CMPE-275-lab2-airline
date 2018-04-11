package edu.sjsu.cmpe275.lab2.reservation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.sjsu.cmpe275.lab2.reservation.Reservation;
import edu.sjsu.cmpe275.lab2.reservation.ReservationRepository;

@Service
public class ReservationService {
	
	@Autowired
	private ReservationRepository reservationRepository;
	
	public List<Reservation> getAllReservations(){
		List<Reservation> reservations = new ArrayList<>();
		reservationRepository.findAll().forEach(reservations::add);
		return reservations;
	}
	
	public Reservation getReservation(String id) {
		return reservationRepository.findById(id).orElse(null);
	}
	
	public void addReservation(Reservation r) {
		reservationRepository.save(r);
	}
	
	public Reservation updateReservation(Reservation r) {
		
		return reservationRepository.save(r);
	}
	
	public void deleteReservation(String id) {
		reservationRepository.deleteById(id);
	}
	

}
