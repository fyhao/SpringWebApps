package com.fyhao.springwebapps.model;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import com.fyhao.springwebapps.entity.Task;
public interface TaskRepository extends CrudRepository<Task, UUID> {
    
}