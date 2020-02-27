package com.tts.TransitApp.model;

import lombok.Data;
//Store data from user
@Data
public class BusRequest {
    public String address;
    public String city;
}
