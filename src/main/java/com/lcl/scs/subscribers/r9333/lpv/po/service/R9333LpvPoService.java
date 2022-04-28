package com.lcl.scs.subscribers.r9333.lpv.po.service;

import java.util.List;


import org.springframework.stereotype.Service;

import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCode;
@Service
public interface R9333LpvPoService {
	List<LpvReasonCode> findReasonCode(String reasonCode);
}
