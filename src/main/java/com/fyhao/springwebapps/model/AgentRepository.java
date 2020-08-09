package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.Agent;

import org.springframework.data.repository.CrudRepository;

public interface AgentRepository extends CrudRepository<Agent, UUID> {
    
}