package com.hsf_project.repository;

import com.hsf_project.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City,Integer> {
    java.util.List<City> findByIsDeletedFalseOrderByNameAsc();
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);
}
