package com.hsf_project.repository;

import com.hsf_project.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    List<Combo> findByStatusAndIsDeletedFalseOrderByIdAsc(String status);
    List<Combo> findByIsDeletedFalseOrderByIdDesc();
}
