package it.unipi.mdwt.flconsole.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ExpConfigServiceTest {
    @Autowired
    private ExpConfigService expConfigService;

    @Test
    void searchExpConfig(){
        String configName = "fdgtd";
        String clientStrategy = "1";
        String stopCondition = "3";
        String email = "flavio@gmail.com";
        int page = 0; // Page number
        int size = 10; // Page size

        //Page<ExpConfig> matchingConfigs = expConfigService.searchExpConfig(email, configName,clientStrategy,stopCondition,page,size);


        //assertNotNull(matchingConfigs);
        //assertFalse((matchingConfigs.isEmpty()));
//        assertTrue(matchingConfigs.getTotalElements() > 0); // Ensure there are total elements
//        assertEquals(size, matchingConfigs.getSize()); // Ensure page size matches requested size
//        assertEquals(page, matchingConfigs.getNumber()); // Ensure page number matches requested page
//
//        for (ExpConfig configs : matchingConfigs){
//            System.out.println("Exp ID:" + configs.getId());
//            System.out.println("Exp Name: "+configs.getName());
//        }
    }



}
