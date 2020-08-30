package com.fyhao.springwebapps.model;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.fyhao.springwebapps.entity.CQueue;

public interface CQueueRepository extends CrudRepository<CQueue, UUID> {
    
	public CQueue findByName(String name);
}
