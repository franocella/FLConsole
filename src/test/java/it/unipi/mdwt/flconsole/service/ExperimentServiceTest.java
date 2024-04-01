package it.unipi.mdwt.flconsole.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static it.unipi.mdwt.flconsole.utils.Constants.PAGE_SIZE;

@SpringBootTest
public class ExperimentServiceTest {

    @Autowired
    private ExperimentService experimentService;


    @Test
    void searchExperiment(){
        String expName = "Experiment";
        int page = 0; // Page number
        int size = PAGE_SIZE; // Page size

//        Page<ExperimentSummary> matchingExps = experimentService.searchExperiment(expName, "First test",page,size);
//
//        assertNotNull(matchingExps);
//        assertFalse((matchingExps.isEmpty()));
//        assertTrue(matchingExps.getTotalElements() > 0); // Ensure there are total elements
//        assertEquals(size, matchingExps.getSize()); // Ensure page size matches requested size
//        assertEquals(page, matchingExps.getNumber()); // Ensure page number matches requested page
//
//        for (ExperimentSummary experiment : matchingExps){
//            System.out.println("Exp ID:" + experiment.getId());
//            System.out.println("Exp Name: "+experiment.getName());
//        }
    }

    @Test
    void runExp() {
        experimentService.runExp("expConfig", "email@gmail.com");
    }
}
