package com.yuihara;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import yuihara.yuihara.model.ShopItem; // THIS MUST MATCH YOUR MODEL PACKAGE

@Repository
public interface ShopRepository extends MongoRepository<ShopItem, String> { 
}