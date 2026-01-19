package com.ipintelligence.service.impl;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.model.IpAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class MockDataService {

//    private final Random random = new Random();
//
//    private final String[] companies = {
//            "Tech Innovations Ltd", "Global Patents Inc", "Innovation Labs",
//            "Research Corp", "Advanced Systems", "Future Technologies"
//    };
//
//    private final String[] inventors = {
//            "John Smith", "Maria Garcia", "Wei Zhang", "Sarah Johnson",
//            "Ahmed Hassan", "Emily Brown", "Carlos Rodriguez", "Lisa Anderson"
//    };
//
//    public List<IpAssetDto> generateMockPatents(String query, String source, int count) {
//        List<IpAssetDto> patents = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            IpAssetDto dto = new IpAssetDto();
//
//            switch (source) {
//                case "WIPO":
//                    dto.setExternalId("WO2024/" + String.format("%06d", 100000 + i));
//                    dto.setPublicationNumber("WO2024/" + String.format("%06d", 100000 + i));
//                    dto.setApplicationNumber("PCT/IB2023/" + (50000 + i));
//                    dto.setJurisdiction("WO");
//                    dto.setPatentOffice("WIPO_MOCK");
//                    dto.setTitle("WIPO Patent: " + capitalizeWords(query) + " Innovation System " + (i + 1));
//                    dto.setDescription("International patent application for " + query +
//                            " technology. This invention provides improved methods and systems for " +
//                            query + " applications with enhanced performance and reliability.");
//                    break;
//
//                case "EPO":
//                    dto.setExternalId("EP" + (3000000 + i));
//                    dto.setPublicationNumber("EP" + (3000000 + i) + "B1");
//                    dto.setApplicationNumber("EP23" + String.format("%06d", 180000 + i));
//                    dto.setJurisdiction("EP");
//                    dto.setPatentOffice("EPO_MOCK");
//                    dto.setTitle("European Patent: Advanced " + capitalizeWords(query) + " Method " + (i + 1));
//                    dto.setDescription("European patent for " + query +
//                            " technology. The invention relates to a method and apparatus for " +
//                            query + " with improved efficiency and reduced costs.");
//                    dto.setGrantDate(LocalDate.now().minusDays(random.nextInt(500)));
//                    break;
//
//                case "TMVIEW":
//                    dto.setExternalId("EU" + String.format("%09d", 900000 + i));
//                    dto.setPublicationNumber("EU" + String.format("%09d", 900000 + i));
//                    dto.setApplicationNumber("EUTM2023/" + (700000 + i));
//                    dto.setJurisdiction("EU");
//                    dto.setPatentOffice("TMVIEW_MOCK");
//                    dto.setAssetType(IpAsset.AssetType.TRADEMARK);
//                    dto.setTitle("EU Trademark: " + capitalizeWords(query) + "™ Brand " + (i + 1));
//                    dto.setDescription("European Union trademark for " + query +
//                            " related goods and services. Class 9, 35, 42 registration.");
//                    break;
//            }
//
//            // Common fields
//            dto.setInventor(inventors[random.nextInt(inventors.length)] +
//                    (i % 2 == 0 ? ", " + inventors[random.nextInt(inventors.length)] : ""));
//            dto.setAssignee(companies[random.nextInt(companies.length)]);
//            dto.setStatus(random.nextBoolean() ? "Published" : "Granted");
//
//            int daysAgo = random.nextInt(1500);
//            dto.setPublicationDate(LocalDate.now().minusDays(daysAgo));
//            dto.setPriorityDate(LocalDate.now().minusDays(daysAgo + 365));
//            dto.setApplicationDate(LocalDate.now().minusDays(daysAgo + 200));
//
//            if (dto.getAssetType() == null) {
//                dto.setAssetType(IpAsset.AssetType.PATENT);
//            }
//
//            // Add some classification codes
//            dto.setCpcClassification("G06F 17/30" + (i % 10));
//            dto.setIpcClassification("G06F 17/30" + (i % 10));
//            dto.setClassification("G06F 17/30" + (i % 10));
//
//            // Keywords
//            dto.setKeywords(query + "; innovation; technology; system; method");
//
//            patents.add(dto);
//        }
//
//        return patents;
//    }
//
//    private String capitalizeWords(String str) {
//        String[] words = str.split("\\s+");
//        StringBuilder result = new StringBuilder();
//        for (String word : words) {
//            if (word.length() > 0) {
//                result.append(Character.toUpperCase(word.charAt(0)))
//                        .append(word.substring(1).toLowerCase())
//                        .append(" ");
//            }
//        }
//        return result.toString().trim();
//    }
}
