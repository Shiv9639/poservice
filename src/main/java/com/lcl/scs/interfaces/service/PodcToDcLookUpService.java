package com.lcl.scs.interfaces.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lcl.scs.interfaces.model.PodcToDcLookUp;
import com.lcl.scs.interfaces.repository.PodcToDcLookUpRepository;

@Service
public class PodcToDcLookUpService {
	
	private static Map<String, String> podcToDcMapping = new HashMap<>();
	private static Map<String, Integer> dcToTransitTimeMapping = new HashMap<>();
	private static PodcToDcLookUpRepository dao;
	
	@Autowired
	public PodcToDcLookUpService(PodcToDcLookUpRepository podRepository) {
		super();
		dao = podRepository;
	}

	public static Supplier<List<PodcToDcLookUp>> getAll = () ->{
		return dao.findAll();
	};
	

	//load once and use... should be convert to cache in future..
	public static Supplier<Map<String, String>> podcToDcMap = () -> {
		if(podcToDcMapping.isEmpty()) {
			podcToDcMapping.putAll(getAll.get().stream()
		      .collect(Collectors.toMap(PodcToDcLookUp::getPodcName, PodcToDcLookUp::getDcName, (x1, x2) -> x1)));
		}
		
		return podcToDcMapping;
		
	};
	
	//load once and use... should be convert to cache in future..
		public static Supplier<Map<String, Integer>> dcToTransitTimeMap = () -> {
			if(dcToTransitTimeMapping.isEmpty()) {
				dcToTransitTimeMapping.putAll(getAll.get().stream()
			      .collect(Collectors.toMap(PodcToDcLookUp::getDcName, PodcToDcLookUp::getTransitTime, (x1, x2) -> x1)));
			}
			
			return dcToTransitTimeMapping;
			
		};
	
}
