package com.fyhao.springwebapps.model;

import java.util.List;
import java.util.UUID;

import com.fyhao.springwebapps.entity.AgentTerminal;

import org.springframework.data.repository.CrudRepository;

public interface AgentTerminalRepository extends CrudRepository<AgentTerminal, UUID> {
    public List<AgentTerminal> findByStatus(String status);
}