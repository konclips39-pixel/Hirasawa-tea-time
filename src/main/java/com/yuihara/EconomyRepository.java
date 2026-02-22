package com.yuihara;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.yuihara.model.EconomyUser; 

@Repository
public interface EconomyRepository extends MongoRepository<EconomyUser, String> {

    // RENAMED: Changed 'Coins' to 'Tealeafs' to match your EconomyUser model
    List<EconomyUser> findTop10ByOrderByTealeafsDesc();

    List<EconomyUser> findByLevel(int level);

    // UPDATED: Ensuring the parameter name and query match the model
    @Query("{ 'tealeafs' : { $gt: ?0 } }")
    List<EconomyUser> findWealthyUsers(long minimumTealeafs);
}