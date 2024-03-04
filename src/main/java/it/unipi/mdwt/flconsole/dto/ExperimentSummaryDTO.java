package it.unipi.mdwt.flconsole.dto;

import it.unipi.mdwt.flconsole.model.ExpConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExperimentSummaryDTO {
    private String id;
    private String name;
    private ExpConfig config;
}
