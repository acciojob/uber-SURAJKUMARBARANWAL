package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		Customer customer1=new Customer();
		customer1.setPassword(customer.getPassword());
		customer1.setMobile(customer.getMobile());
		customerRepository2.save(customer1);
	}


	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
         Customer customer=customerRepository2.findById(customerId).get();
		 if(customer==null) throw new Exception("Customer not found");
		 //customer exist
		boolean flag=false;

		TripBooking tripBooking=new TripBooking();
		//set primary trim attributes
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
	    tripBooking.setBill(0);
		List<Driver> driverList=driverRepository2.findAll();
		Collections.sort(driverList, new Comparator<Driver>() {
			@Override
			public int compare(Driver o1, Driver o2) {
				return o1.getDriverId()- o2.getDriverId();
			}
		});
		for(Driver driver1:driverList){
			if(driver1.getCab().getAvailable()==true){
				flag=true;
				//setting FK attributes
				tripBooking.setDriver(driver1);
				tripBooking.setCustomer(customer);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				driver1.getTripBookingList().add(tripBooking);
				driverRepository2.save(driver1);
				customer.getTripBookingList().add(tripBooking);
				customerRepository2.save(customer);
				break;
			}
		}
		if(flag==false){
			throw new Exception("No cab available!");
		}
		return  tripBooking;


	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
	    tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBookingRepository2.save(tripBooking);
	}
}
