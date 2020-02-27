package com.tts.TransitApp.model;

import java.util.List;

import lombok.Data;
//Store Data from API calls
@Data
public class DistanceResponse {
	public List<Row> rows;
	

}
