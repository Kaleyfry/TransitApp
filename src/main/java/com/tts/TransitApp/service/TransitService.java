package com.tts.TransitApp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tts.TransitApp.model.Bus;
import com.tts.TransitApp.model.BusComparator;
import com.tts.TransitApp.model.BusRequest;
import com.tts.TransitApp.model.DistanceResponse;
import com.tts.TransitApp.model.GeoCodingResponse;
import com.tts.TransitApp.model.Location;

@Service
public class TransitService {
    @Value("${transit_url}")
    public String transitUrl;
	
    @Value("${geocoding_url}")
    public String geocodingUrl;
	
    @Value("${distance_url}")
    public String distanceUrl;
	
    @Value("${google_api_key}")
    public String googleApiKey;
    
    private List<Bus> getBuses(){
    	//Make API call
        RestTemplate restTemplate = new RestTemplate();
        //.getForObject is a method provided by RestTemplate to make a HTTP 
        //request to the transitURL endpoint in order to get bus information
        Bus[] buses = restTemplate.getForObject(transitUrl, Bus[].class);
        //return a list of 
        return Arrays.asList(buses);
    }
    
    private Location getCoordinates(String description) {
        description = description.replace(" ", "+");
        String url = geocodingUrl + description + "+GA&key=" + googleApiKey;
        RestTemplate restTemplate = new RestTemplate();
        GeoCodingResponse response = restTemplate.getForObject(url, GeoCodingResponse.class);
        return response.results.get(0).geometry.location;
    }
    
    private double getDistance(Location origin, Location destination) {
        String url = distanceUrl + "origins=" + origin.lat + "," + origin.lng 
        + "&destinations=" + destination.lat + "," + destination.lng + "&key=" + googleApiKey;
        RestTemplate restTemplate = new RestTemplate();
        //Storing the data given by the API call (go to that url endpoint)
        //as a variable called response of the data type DistanceResponse
        DistanceResponse response = restTemplate.getForObject(url, DistanceResponse.class);
        return response.rows.get(0).elements.get(0).distance.value * 0.000621371;
    }
    //return a list of buses that are close to the person based on location 
    public List<Bus> getNearbyBuses(BusRequest request){
        List<Bus> allBuses = this.getBuses();
        Location personLocation = this.getCoordinates(request.address + " " + request.city);
        List<Bus> nearbyBuses = new ArrayList<>();
        for(Bus bus : allBuses) {
            Location busLocation = new Location();
            busLocation.lat = bus.LATITUDE;
            busLocation.lng = bus.LONGITUDE;
            double latDistance = Double.parseDouble(busLocation.lat) - Double.parseDouble(personLocation.lat);
            double lngDistance = Double.parseDouble(busLocation.lng) - Double.parseDouble(personLocation.lng);
            if (Math.abs(latDistance) <= 0.02 && Math.abs(lngDistance) <= 0.02) {
                double distance = getDistance(busLocation, personLocation);
                if (distance <= 1) {
                    bus.distance = (double) Math.round(distance * 100) / 100;
                    nearbyBuses.add(bus);
                }
            }
        } //end of the loop
        //sort buses based on distance
        Collections.sort(nearbyBuses, new BusComparator());
        return nearbyBuses;
    }
}