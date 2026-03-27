package com.community.service.service;

import com.community.service.entity.Demand;
import com.community.service.entity.PointRecord;
import com.community.service.entity.ProgressReport;
import com.community.service.entity.User;
import com.community.service.mapper.DemandRepository;
import com.community.service.mapper.ProgressReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressReportService {

    private final ProgressReportRepository progressReportRepository;
    private final DemandRepository demandRepository;
    private final PointService pointService;

    public List<ProgressReport> getReportsByDemand(Demand demand) {
        return progressReportRepository.findByDemandOrderByCreatedAtDesc(demand);
    }

    public List<ProgressReport> getReportsByVolunteer(User volunteer) {
        return progressReportRepository.findByVolunteerOrderByCreatedAtDesc(volunteer);
    }

    @Transactional
    public ProgressReport createReport(Long demandId, User volunteer, Integer progressPercentage, 
                                     String resourcesUsed, String reportContent, String imageUrl) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        
        if (!demand.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("您不是该需求的指派志愿者");
        }
        
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new RuntimeException("进度百分比必须在0-100之间");
        }
        
        ProgressReport report = ProgressReport.builder()
                .demand(demand)
                .volunteer(volunteer)
                .progressPercentage(progressPercentage)
                .resourcesUsed(resourcesUsed)
                .reportContent(reportContent)
                .imageUrl(imageUrl)
                .platformApprovalStatus(ProgressReport.PlatformApprovalStatus.PENDING)
                .residentApprovalStatus(ProgressReport.ResidentApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        ProgressReport savedReport = progressReportRepository.save(report);
        
        demand.setProgressPercentage(progressPercentage);
        demandRepository.save(demand);
        
        return savedReport;
    }

    @Transactional
    public void platformApprove(Long reportId, String rejectionReason) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("汇报不存在"));
        
        report.setPlatformApprovalStatus(ProgressReport.PlatformApprovalStatus.APPROVED);
        report.setPlatformReviewedAt(LocalDateTime.now());
        report.setPlatformRejectionReason(null);
        progressReportRepository.save(report);
        
        checkAndUpdateDemandStatus(report);
    }

    @Transactional
    public void platformReject(Long reportId, String rejectionReason) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("汇报不存在"));
        
        report.setPlatformApprovalStatus(ProgressReport.PlatformApprovalStatus.REJECTED);
        report.setPlatformReviewedAt(LocalDateTime.now());
        report.setPlatformRejectionReason(rejectionReason);
        progressReportRepository.save(report);
    }

    @Transactional
    public void residentApprove(Long reportId, User resident) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("汇报不存在"));
        
        if (!report.getDemand().getResident().getId().equals(resident.getId())) {
            throw new RuntimeException("您不是该需求的求助居民");
        }
        
        report.setResidentApprovalStatus(ProgressReport.ResidentApprovalStatus.APPROVED);
        report.setResidentReviewedAt(LocalDateTime.now());
        report.setResidentRejectionReason(null);
        progressReportRepository.save(report);
        
        // 居民审核通过后，给志愿者增加积分
        User volunteer = report.getVolunteer();
        int points = calculatePoints(report.getProgressPercentage());
        pointService.addPoints(volunteer, points, 
            "任务进度汇报通过审核奖励 - " + report.getDemand().getTitle(), 
            PointRecord.PointType.ACTIVITY_PARTICIPATION);
        
        checkAndUpdateDemandStatus(report);
    }

    @Transactional
    public void residentReject(Long reportId, User resident, String rejectionReason) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("汇报不存在"));
        
        if (!report.getDemand().getResident().getId().equals(resident.getId())) {
            throw new RuntimeException("您不是该需求的求助居民");
        }
        
        report.setResidentApprovalStatus(ProgressReport.ResidentApprovalStatus.REJECTED);
        report.setResidentReviewedAt(LocalDateTime.now());
        report.setResidentRejectionReason(rejectionReason);
        progressReportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long reportId, User volunteer) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("汇报不存在"));
        
        if (!report.getVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("您只能删除自己的汇报");
        }
        
        progressReportRepository.delete(report);
    }

    private void checkAndUpdateDemandStatus(ProgressReport report) {
        Demand demand = report.getDemand();
        
        boolean platformApproved = report.getPlatformApprovalStatus() == ProgressReport.PlatformApprovalStatus.APPROVED;
        boolean residentApproved = report.getResidentApprovalStatus() == ProgressReport.ResidentApprovalStatus.APPROVED;
        
        if (platformApproved && residentApproved && report.getProgressPercentage() == 100) {
            demand.setStatus(Demand.DemandStatus.COMPLETED);
            demandRepository.save(demand);
        } else if (report.getProgressPercentage() > 0 && report.getProgressPercentage() < 100) {
            demand.setStatus(Demand.DemandStatus.IN_PROGRESS);
            demandRepository.save(demand);
        }
    }

    private int calculatePoints(int progressPercentage) {
        // 根据进度百分比计算积分
        if (progressPercentage >= 100) {
            return 50; // 完成100%奖励50积分
        } else if (progressPercentage >= 80) {
            return 40; // 完成80%以上奖励40积分
        } else if (progressPercentage >= 60) {
            return 30; // 完成60%以上奖励30积分
        } else if (progressPercentage >= 40) {
            return 20; // 完成40%以上奖励20积分
        } else if (progressPercentage >= 20) {
            return 10; // 完成20%以上奖励10积分
        } else {
            return 5;  // 完成20%以下奖励5积分
        }
    }
}