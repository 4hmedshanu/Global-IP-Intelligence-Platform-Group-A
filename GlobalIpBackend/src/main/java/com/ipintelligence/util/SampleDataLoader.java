package com.ipintelligence.util;

import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.IpAsset.AssetType;
import com.ipintelligence.repo.IpAssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SampleDataLoader implements CommandLineRunner {
    private final IpAssetRepository repo;

    public SampleDataLoader(IpAssetRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            // Add sample patents
            LocalDateTime now = LocalDateTime.now();
            repo.save(new IpAsset(null, "EXT-001", "AI Patent Filing", "AI invention", AssetType.PATENT, "APP-001", "PUB-001", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 20), "Granted", "US", "USPTO", "John Doe", "TechCorp", "AI/ML", "AI/ML", "Artificial Intelligence", "Active", LocalDate.of(2035, 6, 1), "{}", now, now));
            repo.save(new IpAsset(null, "EXT-002", "Blockchain Trademark", "Blockchain brand", AssetType.TRADEMARK, "APP-002", "PUB-002", LocalDate.of(2025, 5, 10), LocalDate.of(2025, 5, 15), LocalDate.of(2025, 5, 20), LocalDate.of(2025, 5, 30), "Pending", "EP", "EPO", "Jane Roe", "InnovateLabs", "Blockchain", "Blockchain", "Blockchain Protocol", "Active", LocalDate.of(2035, 5, 10), "{}", now, now));
            repo.save(new IpAsset(null, "EXT-003", "IoT Patent Filing", "IoT device", AssetType.PATENT, "APP-003", "PUB-003", LocalDate.of(2025, 4, 15), LocalDate.of(2025, 4, 20), LocalDate.of(2025, 4, 25), LocalDate.of(2025, 4, 30), "Granted", "CN", "CNIPA", "Alice Smith", "FutureSystems", "IoT", "IoT", "IoT Security", "Active", LocalDate.of(2035, 4, 15), "{}", now, now));
            repo.save(new IpAsset(null, "EXT-004", "Biotech Patent Filing", "Biotech discovery", AssetType.PATENT, "APP-004", "PUB-004", LocalDate.of(2025, 3, 5), LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 15), LocalDate.of(2025, 3, 20), "Granted", "US", "USPTO", "Bob Lee", "TechCorp", "Biotech", "Biotech", "Biotech", "Active", LocalDate.of(2035, 3, 5), "{}", now, now));
            repo.save(new IpAsset(null, "EXT-005", "Quantum Patent Filing", "Quantum tech", AssetType.PATENT, "APP-005", "PUB-005", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 5), LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 15), "Pending", "EP", "EPO", "Carol King", "InnovateLabs", "Quantum", "Quantum", "Quantum", "Active", LocalDate.of(2035, 2, 1), "{}", now, now));
        }
    }
}
