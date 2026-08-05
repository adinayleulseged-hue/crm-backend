package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.SegmentCreateDTO;
import com.crm.crm_backend.dto.request.SegmentUpdateDTO;
import com.crm.crm_backend.dto.response.SegmentResponseDTO;
import com.crm.crm_backend.service.core.SegmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SegmentController {

    private final SegmentService segmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SegmentResponseDTO> createSegment(
            @Valid @RequestBody SegmentCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(segmentService.createSegment(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<SegmentResponseDTO> getSegment(@PathVariable Long id) {

        return ResponseEntity.ok(segmentService.getSegment(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<Page<SegmentResponseDTO>> getAllSegments(Pageable pageable) {

        return ResponseEntity.ok(segmentService.getAllSegments(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SegmentResponseDTO> updateSegment(
            @PathVariable Long id,
            @Valid @RequestBody SegmentUpdateDTO dto) {

        return ResponseEntity.ok(segmentService.updateSegment(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long id) {

        segmentService.deleteSegment(id);

        return ResponseEntity.noContent().build();
    }
}
