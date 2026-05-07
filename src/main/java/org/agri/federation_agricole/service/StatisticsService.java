package org.agri.federation_agricole.service;

import org.agri.federation_agricole.dto.CollectivityLocalStatisticsDTO;
import org.agri.federation_agricole.dto.CollectivityOverallStatisticsDTO;
import org.agri.federation_agricole.exception.BadRequestException;
import org.agri.federation_agricole.repository.CollectivityRepository;
import org.agri.federation_agricole.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final CollectivityRepository collectivityRepository;

    public StatisticsService(StatisticsRepository statisticsRepository,
                             CollectivityRepository collectivityRepository) {
        this.statisticsRepository = statisticsRepository;
        this.collectivityRepository = collectivityRepository;
    }

    public List<CollectivityLocalStatisticsDTO> getLocalStatistics(
            String collectivityId, LocalDate from, LocalDate to) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is required");
        }
        if (from == null || to == null) {
            throw new BadRequestException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from date must be before or equal to to date");
        }
        if (collectivityRepository.getCollectivityById(collectivityId) == null) {
            throw new BadRequestException("Collectivity not found");
        }
        return statisticsRepository.getLocalStatistics(collectivityId, from, to);
    }

    public List<CollectivityOverallStatisticsDTO> getOverallStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from date must be before or equal to to date");
        }
        return statisticsRepository.getOverallStatistics(from, to);
    }
}