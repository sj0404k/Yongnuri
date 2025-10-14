package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Reports;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Reports,Long> {

    List<Reports> findByReportedUser_Id(Long reportedUserId);

    Long countByReportedUser_Id(Long reportedUserId);

    Long countByReportedUser_IdAndStatus(Long reportedUserId, Enum.ReportStatus status);
}
