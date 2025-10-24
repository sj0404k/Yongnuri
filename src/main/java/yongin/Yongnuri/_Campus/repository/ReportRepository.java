package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Reports;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Reports, Long> {

    List<Reports> findByReportedId(Long reportedId);

    Long countByReportedId(Long reportedId);
    Long countByReportedIdAndStatus(Long reportedId, Enum.ReportStatus status);

    /** 관리자 목록: 최신순 전체 조회 */
    List<Reports> findAllByOrderByCreatedAtDesc();
}
