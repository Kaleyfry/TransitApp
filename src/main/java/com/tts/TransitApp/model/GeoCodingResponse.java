package com.tts.TransitApp.model;

import java.util.List;

import lombok.Data;

@Data
public class GeoCodingResponse {
	public List<Geocoding> results;

}
