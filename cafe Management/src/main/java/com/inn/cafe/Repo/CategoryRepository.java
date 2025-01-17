package com.inn.cafe.Repo;

import com.inn.cafe.POJO.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

    public interface CategoryRepository extends JpaRepository<Category, Integer> {
        @Query(value = "SELECT nextval('next_val')", nativeQuery = true)
        int getNextVal(); // Method to get the next value from the sequence
        @Query(value = "SELECT nextval('next_val')", nativeQuery = true) // Adjust according to your DB's sequence setup

        int getNextval();
    }