package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.ExpConfigDao;
import it.unipi.mdwt.flconsole.dao.ExperimentDao;
import it.unipi.mdwt.flconsole.model.ExpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ExpConfigService {

    private final ExpConfigDao experimentDao;
    private final Logger applicationLogger;

    @Autowired
    public ExpConfigService(ExpConfigDao experimentDao, Logger applicationLogger) {
        this.experimentDao = experimentDao;
        this.applicationLogger = applicationLogger;
    }

    public List<ExpConfig> getUsersConfigList() {
        return null;
    }

    public void saveConfig(ExpConfig config) {
        experimentDao.save(config);
    }

}
