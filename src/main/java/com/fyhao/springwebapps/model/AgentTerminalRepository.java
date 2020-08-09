package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.AgentTerminal;

import org.springframework.data.repository.CrudRepository;

public interface AgentTerminalRepository extends CrudRepository<AgentTerminal, UUID> {
    
}