package com.epam.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.epam.data.RoadAccident;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This is to be completed by mentees
 */
public class DataProcessor {

    private final List<RoadAccident> roadAccidentList;
    
    private static final Long FIRST_ELEMENT = 1L;
    private static final int FROM_INDEX = 0;
    private static final int TO_INDEX = 3;

    public DataProcessor(List<RoadAccident> roadAccidentList){
        this.roadAccidentList = roadAccidentList;
    }


//    First try to solve task using java 7 style for processing collections

    /**
     * Return road accident with matching index
     * @param index
     * @return
     */
    public RoadAccident getAccidentByIndex7(String index){
    	if(index != null && !index.isEmpty()){
    		for(RoadAccident roadAccident : roadAccidentList){
        		if(roadAccident.getAccidentId().equals(index)){
        			return roadAccident;
        		}
        	}	
    	}
        return null;
    }


    /**
     * filter list by longtitude and latitude values, including boundaries
     * @param minLongitude
     * @param maxLongitude
     * @param minLatitude
     * @param maxLatitude
     * @return
     */
    public Collection<RoadAccident> getAccidentsByLocation7(float minLongitude, float maxLongitude, float minLatitude, float maxLatitude){
    	Collection<RoadAccident> roadAccidentCollection = new ArrayList<>(); 
    	for(RoadAccident roadAccident : roadAccidentList){
    		if(isWithinLocation(roadAccident, minLongitude ,maxLongitude, minLatitude, maxLatitude)){
    			roadAccidentCollection.add(roadAccident);
    		}
    	}
        return roadAccidentCollection;
    }

    private boolean isWithinLocation(RoadAccident roadAccident,	float minLongitude, float maxLongitude, float minLatitude,
			float maxLatitude) {
    	return (roadAccident.getLongitude() >= minLongitude && roadAccident.getLongitude() <= maxLongitude &&
    			roadAccident.getLatitude() >= minLatitude && roadAccident.getLatitude() <= maxLatitude);
	}


	/**
     * count incidents by road surface conditions
     * ex:
     * wet -> 2
     * dry -> 5
     * @return
     */
    public Map<String, Long> getCountByRoadSurfaceCondition7(){
    	Map<String, Long> incidentCountMap = new HashMap<>();
    	for(RoadAccident roadAccident : roadAccidentList){
    		String roadSurfaceKey = roadAccident.getRoadSurfaceConditions();
    		Long incidentCount = incidentCountMap.get(roadSurfaceKey);
    		if(incidentCountMap.containsKey(roadSurfaceKey)){
    			incidentCount++;
    		}else{
    			incidentCount = FIRST_ELEMENT;
    		}
    		incidentCountMap.put(roadSurfaceKey, incidentCount);
    	}
        return incidentCountMap;
    }

    /**
     * find the weather conditions which caused the top 3 number of incidents
     * as example if there were 10 accidence in rain, 5 in snow, 6 in sunny and 1 in foggy, then your result list should contain {rain, sunny, snow} - top three in decreasing order
     * @return
     */
    public List<String> getTopThreeWeatherCondition7(){
    	Map<String, Long> incidentCountMap = new TreeMap<>();
    	for(RoadAccident roadAccident : roadAccidentList){
    		String weatherConditionKey = roadAccident.getWeatherConditions();
    		Long incidentCount = incidentCountMap.get(weatherConditionKey);
    		if(incidentCountMap.containsKey(weatherConditionKey)){
    			incidentCount++;
    		}else{
    			incidentCount = FIRST_ELEMENT;
    		}
    		incidentCountMap.put(weatherConditionKey, incidentCount);
    	}
    	
    	Map<String, Long> sortedIncidentCountMap = new TreeMap<>(new Comparator<String>(){
			public int compare(String o1, String o2) {
				return incidentCountMap.get(o2).compareTo(incidentCountMap.get(o1));
			}
        });
    	
    	sortedIncidentCountMap.putAll(incidentCountMap);
    	return new ArrayList<String>(sortedIncidentCountMap.keySet()).subList(FROM_INDEX, TO_INDEX); 
    }

    /**
     * return a multimap where key is a district authority and values are accident ids
     * ex:
     * authority1 -> id1, id2, id3
     * authority2 -> id4, id5
     * @return
     */
    public Multimap<String, String> getAccidentIdsGroupedByAuthority7(){
    	Multimap<String, String> myMultimap = ArrayListMultimap.create();
    	for(RoadAccident roadAccident : roadAccidentList){
    		myMultimap.put(roadAccident.getDistrictAuthority(), roadAccident.getAccidentId());
    	}
        return myMultimap;
    }


    // Now let's do same tasks but now with streaming api



    public RoadAccident getAccidentByIndex(String index){
    	return roadAccidentList.stream().filter(accident -> index.equals(accident.getAccidentId()))
    	.findAny().orElse(null);   	
    }


    /**
     * filter list by longtitude and latitude fields
     * @param minLongitude
     * @param maxLongitude
     * @param minLatitude
     * @param maxLatitude
     * @return
     */
    public Collection<RoadAccident> getAccidentsByLocation(float minLongitude, float maxLongitude, float minLatitude, float maxLatitude){
    	return roadAccidentList.stream() 			
    	.filter(minLong -> minLong.getLongitude() >= minLongitude)
    	.filter(maxLong -> maxLong.getLongitude() <= maxLongitude)
    	.filter(minLat -> minLat.getLatitude() >= minLatitude)
    	.filter(maxLat -> maxLat.getLatitude() <= maxLatitude)
    	.collect(Collectors.toList());	 
    }

    /**
     * find the weather conditions which caused max number of incidents
     * @return
     */
    public List<String> getTopThreeWeatherCondition(){
    	return roadAccidentList.stream().collect(Collectors.groupingBy(RoadAccident::getWeatherConditions, Collectors.counting()))
    	.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(3)
    	.map(roadAccident  -> roadAccident.getKey()).collect(Collectors.toList());
    }

    /**
     * count incidents by road surface conditions
     * @return
     */
    public Map<String, Long> getCountByRoadSurfaceCondition(){
    	return roadAccidentList.stream().collect(
        Collectors.groupingBy(RoadAccident::getRoadSurfaceConditions, Collectors.counting()));
    }

    /**
     * To match streaming operations result, return type is a java collection instead of multimap
     * @return
     */
    public Map<String, List<String>> getAccidentIdsGroupedByAuthority(){
    	return roadAccidentList.stream().collect(Collectors.groupingBy(RoadAccident::getDistrictAuthority,
    	Collectors.mapping(RoadAccident::getAccidentId, Collectors.toList())));
    }

}
