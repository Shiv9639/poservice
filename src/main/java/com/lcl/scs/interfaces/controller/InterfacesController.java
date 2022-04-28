package com.lcl.scs.interfaces.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lcl.scs.interfaces.model.Interfaces;
import com.lcl.scs.util.logging.LoggingUtilities;

@RestController
@RequestMapping("/interfaces")
@ComponentScan(basePackages = "com.lcl.scs.interfaces")
public class InterfacesController {
	@Autowired
	private com.lcl.scs.interfaces.service.InterfacesService interfacesService;

	@PostMapping(value = "/")
	public List<Interfaces> getAllInterfaces() {
		return interfacesService.findAll();
	}
	
	@PostMapping(value="/byRicefnum", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Interfaces getInterfaceByRicefNum(@RequestHeader("ricefnum") String ricefnum) {
		//System.out.println("Parameter: "+ricefnum);
		LoggingUtilities.generateInfoLog("Parameter: "+ricefnum);
		return interfacesService.findByRicefnum(ricefnum);
	}
	
	@PostMapping(value="/bySource", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<Interfaces> getInterfaceBySource(@RequestHeader("source") String source) {
		List<Interfaces> result= interfacesService.findBySource(source);
		System.out.println("Size: "+result.size());
		
		for(Interfaces i:result) {
			System.out.println("hello: "+i.toString());
		}
		
		return result;
	}
	
	@PostMapping(value="/save")
	public ResponseEntity<?> saveOrUpdateInterfaces(@RequestHeader("ricefnum") String ricefNum,@RequestHeader("source") String source){
		Interfaces i=new Interfaces();
		i.setRicefnum(ricefNum);
		i.setSource(source);
		interfacesService.saveOrUpdateInterfaces(i);
		return new ResponseEntity("Added successfully",HttpStatus.OK);
	}
}
