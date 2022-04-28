package com.lcl.scs.subscribers.r9333.lpv.po.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCodeTransaction;

@Repository
public interface LpvReasonCodeTransactionRepository extends MongoRepository<LpvReasonCodeTransaction, String> {
}
