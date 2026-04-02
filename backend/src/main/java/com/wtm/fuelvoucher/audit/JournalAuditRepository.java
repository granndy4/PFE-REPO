package com.wtm.fuelvoucher.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {
}
