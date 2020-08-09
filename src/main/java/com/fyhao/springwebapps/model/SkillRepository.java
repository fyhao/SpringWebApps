package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.Skill;

import org.springframework.data.repository.CrudRepository;

public interface SkillRepository extends CrudRepository<Skill, UUID> {
    
    public Skill findByName(String skill);
}