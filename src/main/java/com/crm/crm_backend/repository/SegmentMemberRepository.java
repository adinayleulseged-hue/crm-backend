package com.crm.crm_backend.repository;


import com.crm.crm_backend.model.entity.SegmentMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentMemberRepository
        extends JpaRepository<SegmentMember, Long> {

    List<SegmentMember> findBySegmentId(Long segmentId);

    List<SegmentMember> findByCustomerId(Long customerId);
}