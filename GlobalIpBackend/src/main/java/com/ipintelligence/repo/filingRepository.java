package com.ipintelligence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipintelligence.model.Filing;
import com.ipintelligence.model.IpAsset;

@Repository
public interface filingRepository extends JpaRepository<Filing, Integer> {
	
	List<Filing> findByIpAsset(IpAsset ipAsset);
	
}
