package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.repository.CabRepository;
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
	@Autowired
	CabRepository cabRepository;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
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
		Driver driver=null;

		TripBooking tripBooking=new TripBooking();
		//set primary trim attributes
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
	    tripBooking.setBill(distanceInKm*10);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setCustomer(customer);
		List<Driver> driverList=driverRepository2.findAll();

		for(Driver driver1:driverList){
		    if(driver1.getCab().getAvailable()){
				if(driver==null|| driver1.getDriverId()<driver.getDriverId())
					driver=driver1;
			    }
		}
		if(driver==null){
			throw new Exception("No cab available!");
		}
		driver.getCab().setAvailable(false);
		tripBooking.setDriver(driver);

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);
		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		return  tripBooking;


	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		Driver driver=tripBooking.getDriver();
		Customer customer=tripBooking.getCustomer();
		driver.getCab().setAvailable(true);
		cabRepository.save(driver.getCab());
		//update status
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CANCELED);
        tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		Driver driver=tripBooking.getDriver();
		Customer customer=tripBooking.getCustomer();
		driver.getCab().setAvailable(true);
		cabRepository.save(driver.getCab());
		//update status
		tripBooking.setStatus(TripStatus.COMPLETED);

		tripBookingRepository2.save(tripBooking);
	}
}
