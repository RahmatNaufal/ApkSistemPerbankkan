package com.intbank.repository;

import com.intbank.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);

    @Query("select a from AuditLog a " +
            "where (:action is null or a.action = :action) " +
            "and (:actor is null or a.actorUserId in " +
            "     (select u.id from User u where lower(u.email) like lower(concat('%', :actor, '%')))) " +
            "and (:keyword is null or lower(coalesce(a.targetId, '')) like lower(concat('%', :keyword, '%')) " +
            "     or lower(coalesce(a.reason, '')) like lower(concat('%', :keyword, '%')) " +
            "     or lower(a.action) like lower(concat('%', :keyword, '%')))")
    Page<AuditLog> search(@Param("action") String action,
                          @Param("actor") String actor,
                          @Param("keyword") String keyword,
                          Pageable pageable);
}
