package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Reports;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Reports, Long> {

    List<Reports> findByReportedUser_Id(Long reportedUserId);

    Long countByReportedUser_Id(Long reportedUserId);

    Long countByReportedUser_IdAndStatus(Long reportedUserId, Enum.ReportStatus status);

    interface AdminReportRow {
        Long getId();
        Long getReportStudentId();
        Enum.ChatType getReportType();
        Long getTypeId();
        Long getReportId();
        Long getReportedId();
        Enum.ReportReason getReportReason();
        String getContent();
        LocalDateTime getProcessedAt();
        Enum.ReportStatus getStatus();
    }

    /**
     * (★수정★) @ManyToOne 관계를 사용하도록 JPQL 쿼리 수정
     */
    @Query("""
    select
      r.id                        as id,
      r.reportId                  as reportStudentId,
      r.postType                  as reportType,
      r.postId                    as typeId,
      r.reportId                  as reportId,
      r.reportedUser.id           as reportedId,
      r.reportReason              as reportReason,
      r.content                   as content,
      r.processedAt               as processedAt,
      r.status                    as status
    from Reports r
    left join r.reportedUser
    order by r.createdAt desc
""")
    List<AdminReportRow> findAdminReportList();
}
