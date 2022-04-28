package com.lcl.scs.interfaces.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.lcl.scs.interfaces.model.PodcToDcLookUp;

public interface PodcToDcLookUpRepository extends MongoRepository<PodcToDcLookUp, String> {
	
	List<PodcToDcLookUp> findAll();

}
