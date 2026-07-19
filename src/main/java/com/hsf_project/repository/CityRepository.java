package com.hsf_project.repository;

import com.hsf_project.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CityRepository extends JpaRepository<City,Integer> {
    List<City> findByIsDeletedFalseOrderByNameAsc();
}
