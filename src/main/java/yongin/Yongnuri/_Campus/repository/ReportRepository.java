package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Reports;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Reports,Long> {
    @Query("SELECT r.reportedId, COUNT(r) " +
            "FROM Reports r " +
            "GROUP BY r.reportedId")
    List<Object[]> findReportCountsGrouped();

    List<Reports> findByReportedId(Long reportedId);

    Long countByReportedId(Long reportedId);
}
