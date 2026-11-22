
package com.hms.department.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hms.department.Department;
import com.hms.department.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId);
    }

    public Optional<Department> getDepartmentByName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName);
    }

    public boolean departmentExists(String departmentName) {
        return departmentRepository.existsByDepartmentName(departmentName);
    }
}
