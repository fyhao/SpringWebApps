package com.fyhao.springwebapps.model;

import java.util.List;
import java.util.UUID;

import com.fyhao.springwebapps.entity.Bot;

import org.springframework.data.repository.CrudRepository;

public interface BotRepository extends CrudRepository<Bot, UUID> {
    List<Bot> findByChannelAndEnabledTrueOrderByPriorityAsc(String channel);
    Bot findFirstByChannelAndNameAndEnabledTrue(String channel, String name);
}
