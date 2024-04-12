package it.unipi.mdwt.flconsole.service;

import it.unipi.mdwt.flconsole.dao.MetricsDao;
import it.unipi.mdwt.flconsole.model.ExpMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsService {

    private final MetricsDao metricsDao;
    @Autowired
    public MetricsService(MetricsDao metricsDao) {
        this.metricsDao = metricsDao;
    }

    public List<ExpMetrics> getMetrics(String id) {
        return metricsDao.findByExpId(id);
    }
}
