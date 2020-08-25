package com.udacity.vehicles.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.CarService;

import java.net.URI;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Implements testing of the CarController class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CarControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<Car> json;


    @Autowired
    private CarRepository carRepository;


    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;

    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @Before
    public void setup() {
        Car car = getCar();
        car.setId(1L);
        given(carService.save(any())).willReturn(car);
        given(carService.findById(any())).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    /**
     * Tests for successful creation of new car in the system
     *
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void createCar() throws Exception {
        Car car = getCar();
        mvc.perform(
                post(new URI("/cars"))
                        .content(json.write(car).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());
    }

    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     *
     * @throws Exception if the read operation of the vehicle list fails
     */
    @Test
    public void listCars() throws Exception {
        /**
         * TODO: [DONE] Add a test to check that the `get` method works by calling
         *   the whole list of vehicles. This should utilize the car from `getCar()`
         *   below (the vehicle will be the first in the list).
         */

        mvc.perform(
                get(new URI("/cars"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carList").isArray())
                .andExpect(jsonPath("$._embedded.carList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.carList[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$._embedded.carList[0].condition", Matchers.is(Condition.USED.name())));
    }

    /**
     * Tests the read operation for a single car by ID.
     *
     * @throws Exception if the read operation for a single car fails
     */
    @Test
    public void findCar() throws Exception {
        /**
         * TODO: [DONE] Add a test to check that the `get` method works by calling
         *   a vehicle by ID. This should utilize the car from `getCar()` below.
         */

        mvc.perform(
                get(new URI("/cars/1"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.condition", Matchers.is(Condition.USED.name())))
                .andExpect(jsonPath("$.details.body", Matchers.is("sedan")))
                .andExpect(jsonPath("$.details.model", Matchers.is("Impala")))
                .andExpect(jsonPath("$.details.numberOfDoors", Matchers.is(4)))
                .andExpect(jsonPath("$.details.fuelType", Matchers.is("Gasoline")))
                .andExpect(jsonPath("$.details.engine", Matchers.is("3.6L V6")))
                .andExpect(jsonPath("$.details.mileage", Matchers.is(32280)))
                .andExpect(jsonPath("$.details.modelYear", Matchers.is(2018)))
                .andExpect(jsonPath("$.details.productionYear", Matchers.is(2018)))
                .andExpect(jsonPath("$.details.externalColor", Matchers.is("white")))
                .andExpect(jsonPath("$.details.manufacturer.code", Matchers.is(101)))
                .andExpect(jsonPath("$.details.manufacturer.name", Matchers.is("Chevrolet")))
                .andExpect(jsonPath("$.location.lat", Matchers.is(40.730610)))
                .andExpect(jsonPath("$.location.lon", Matchers.is(-73.935242)));

        /**
         *         MvcResult mvcResult = mvc.perform(
         *                 get(new URI("/cars/1"))
         *                         .contentType(MediaType.APPLICATION_JSON_UTF8)
         *                         .accept(MediaType.APPLICATION_JSON_UTF8))
         *                 .andExpect(status().isOk()).andReturn();
         *         String jsonString = mvcResult.getResponse().getContentAsString();
         *         System.out.println("json string=====>" + jsonString);
         *         ObjectContent<Car> parse = json.parse(jsonString);
         *         Car car = parse.getObject();
         *         System.out.println("car =====>" + car.getId() + " " + car.getDetails());
         */

    }

    /**
     * Tests the deletion of a single car by ID.
     *
     * @throws Exception if the delete operation of a vehicle fails
     */
    @Test
    public void deleteCar() throws Exception {
        /**
         * TODO: [DONE] Add a test to check whether a vehicle is appropriately deleted
         *   when the `delete` method is called from the Car Controller. This
         *   should utilize the car from `getCar()` below.
         */
        mvc.perform(
                delete(new URI("/cars/1"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

    }


    /**
     * Tests for successful update of an existing car in the system
     *
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void updateCar() throws Exception {
        Car car = getCar();
        Car savedCar = carRepository.save(car);
        System.out.println("car =====>" + savedCar.getId() + " " + savedCar.getCondition().name());
        savedCar.setCondition(Condition.NEW);
        System.out.println("car =====>" + savedCar.getId() + " " + savedCar.getCondition().name());
        Long id = savedCar.getId();


        mvc.perform(
                put(new URI("/cars/" + id))
                        .content(json.write(savedCar).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }


    /**
     * Creates an example Car object for use in testing.
     *
     * @return an example Car object
     */
    private Car getCar() {
        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        return car;
    }
}