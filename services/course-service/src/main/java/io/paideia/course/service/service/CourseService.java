package io.paideia.course.service.service;

import java.util.List;
import java.util.UUID;

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

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> findAll() {
        return courseRepository.findAll().stream().map(courseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO findById(UUID id) {
        return courseRepository.findById(id).map(courseMapper::toResponse).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Transactional
    public CourseResponseDTO create(CourseRequestDTO dto) {
        if (courseRepository.existsByTitle(dto.title())) {
            throw new CourseTitleConflictException(dto.title());
        }
        CourseEntity saved = courseRepository.save(courseMapper.toEntity(dto));
        return courseMapper.toResponse(saved);
    }
}
