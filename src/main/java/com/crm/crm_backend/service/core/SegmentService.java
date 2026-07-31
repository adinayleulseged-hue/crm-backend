package com.crm.crm_backend.service.core;


import com.crm.crm_backend.dto.request.SegmentCreateDTO;
import com.crm.crm_backend.dto.request.SegmentUpdateDTO;
import com.crm.crm_backend.dto.response.SegmentResponseDTO;
import com.crm.crm_backend.mapper.SegmentMapper;
import com.crm.crm_backend.model.entity.CustomerSegment;
import com.crm.crm_backend.repository.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SegmentService {

    private final CustomerSegmentRepository segmentRepository;
    private final SegmentMapper segmentMapper;

    public SegmentResponseDTO createSegment(SegmentCreateDTO dto) {

        CustomerSegment segment = segmentMapper.toEntity(dto);

        segment.setSegmentNumber("SEG-" + System.currentTimeMillis());
        segment.setActive(true);

        CustomerSegment saved = segmentRepository.save(segment);

        return segmentMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public SegmentResponseDTO getSegment(Long id) {

        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segment not found"));

        return segmentMapper.toResponseDTO(segment);
    }

    @Transactional(readOnly = true)
    public Page<SegmentResponseDTO> getAllSegments(Pageable pageable) {

        return segmentRepository.findAll(pageable)
                .map(segmentMapper::toResponseDTO);
    }

    public SegmentResponseDTO updateSegment(Long id,
                                            SegmentUpdateDTO dto) {

        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segment not found"));

        segmentMapper.updateEntityFromDTO(dto, segment);

        CustomerSegment updated = segmentRepository.save(segment);

        return segmentMapper.toResponseDTO(updated);
    }

    public void deleteSegment(Long id) {

        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segment not found"));

        segmentRepository.delete(segment);

        log.info("Segment {} deleted", id);
    }
}
