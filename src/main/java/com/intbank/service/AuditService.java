package com.intbank.service;

import com.intbank.domain.AuditLog;
import com.intbank.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mencatat aktivitas penting (registrasi, login, transaksi, perubahan status).
 * Dipanggil dari service lain; aktor diambil dari konteks security bila ada.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditService(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void record(String action, String targetType, String targetId, String reason) {
        Long actor = null;
        try {
            actor = currentUserService.currentUserId();
        } catch (RuntimeException ignored) {
            // Proses sistem/seed tanpa konteks login: actor = sistem.
        }
        auditLogRepository.save(new AuditLog(actor, action, targetType, targetId, reason, null));
    }
}
