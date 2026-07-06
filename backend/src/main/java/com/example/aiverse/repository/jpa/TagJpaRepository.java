package com.example.aiverse.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aiverse.entity.Tag;
import com.example.aiverse.repository.TagUsage;

public interface TagJpaRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    @Query("""
            SELECT new com.example.aiverse.repository.TagUsage(t.id, t.name, COUNT(at.id))
            FROM Tag t LEFT JOIN AssetTag at ON at.tag = t
            WHERE :query IS NULL OR t.name LIKE CONCAT('%', :query, '%')
            GROUP BY t.id, t.name
            ORDER BY COUNT(at.id) DESC, t.name ASC
            """)
    List<TagUsage> searchOrderByUsage(@Param("query") String query, Pageable pageable);
}
