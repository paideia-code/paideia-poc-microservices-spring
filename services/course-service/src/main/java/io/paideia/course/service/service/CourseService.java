package io.paideia.course.service.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.paideia.course.service.controller.dto.CourseRequestDTO;
import io.paideia.course.service.controller.dto.CourseResponseDTO;
import io.paideia.course.service.exception.custom.CourseNotFoundException;
import io.paideia.course.service.exception.custom.CourseTitleConflictException;
import io.paideia.course.service.model.entity.CourseEntity;
import io.paideia.course.service.model.repository.CourseRepository;
import io.paideia.course.service.service.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> findAll(Pageable pageable) {
        log.debug("event=course.list.requested page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return courseRepository.findAll(pageable).map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO findById(UUID id) {
        return courseRepository.findById(id)
                .map(courseMapper::toResponse)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Transactional
    public CourseResponseDTO create(CourseRequestDTO dto) {
        if (courseRepository.existsByTitle(dto.title())) {
            log.warn("event=course.create.conflict title={}", dto.title());
            throw new CourseTitleConflictException(dto.title());
        }

        CourseEntity saved = courseRepository.save(courseMapper.toEntity(dto));

        log.info("event=course.created courseId={} status={}", saved.getId(), saved.getStatus());

        return courseMapper.toResponse(saved);
    }
}
