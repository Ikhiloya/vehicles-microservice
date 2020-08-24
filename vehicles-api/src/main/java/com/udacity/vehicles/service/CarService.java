package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {
    private Logger logger = LoggerFactory.getLogger(CarService.class);
    private final CarRepository repository;
    private final WebClient webClientMaps;
    private final WebClient webClientPricing;



    public CarService(CarRepository repository, @Qualifier("maps") WebClient webClientMaps,
                      @Qualifier("pricing") WebClient webClientPricing) {
        /**
         * TODO: [DONE] Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here.
         */
        this.repository = repository;
        this.webClientMaps = webClientMaps;
        this.webClientPricing = webClientPricing;
    }

    /**
     * Gathers a list of all vehicles
     *
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     *
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: [DONE] Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException();
        }
        Car car = optionalCar.get();
        Long vehicleId = car.getId();
        /**
         * TODO: [DONE] Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * TODO: Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */


        Mono<Price> priceMono = webClientPricing.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/services/price")
                        .queryParam("vehicleId", vehicleId)
                        .build())
                .retrieve()
                .bodyToMono(Price.class);

        if (priceMono.blockOptional().isPresent()) {
            Price price = priceMono.blockOptional().get();
            logger.info("price fetched for id {}, is  {}", vehicleId, price.getPrice());
            car.setPrice(price.getCurrency() + " " + price.getPrice());
        }


        /**
         * TODO: [DONE] Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */

        Mono<Address> addressMono = webClientMaps.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps")
                        .queryParam("lat", car.getLocation().getLat())
                        .queryParam("lon", car.getLocation().getLon())
                        .build())
                .retrieve()
                .bodyToMono(Address.class);


        if (addressMono.blockOptional().isPresent()) {
            Address address = addressMono.blockOptional().get();
            logger.info("address fetched {}, is  {}", address.getAddress(), address.getCity());
            car.getLocation().setAddress(address.getAddress());
            car.getLocation().setCity(address.getCity());
            car.getLocation().setState(address.getState());
            car.getLocation().setZip(address.getZip());
        }


        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     *
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     *
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * TODO: [DONE] Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         */
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException();
        }
        Car car = optionalCar.get();
        /**
         * TODO: [DONE] Delete the car from the repository.
         */
        repository.delete(car);
    }
}
