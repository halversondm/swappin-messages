package com.halverson.swappin_messages.dao;

import com.halverson.swappin_messages.entity.MessageStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageStorageDao extends JpaRepository<MessageStorage, Integer> {
}
