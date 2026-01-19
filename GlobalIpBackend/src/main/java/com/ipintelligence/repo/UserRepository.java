package com.ipintelligence.repo;

import com.ipintelligence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    @Query(
    		  value = """
    		    SELECT DATE(created_at) AS day, COUNT(*) 
    		    FROM users
    		    GROUP BY DATE(created_at)
    		    ORDER BY DATE(created_at)
    		  """,
    		  nativeQuery = true
    		)
    		List<Object[]> countUsersPerDay();

}