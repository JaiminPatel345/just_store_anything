package com.jaimin.justStore.repository;

import com.jaimin.justStore.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FileRepository extends JpaRepository<File, Long> {
    
    // Find all files ordered by creation date (newest first)
    List<File> findAllByOrderByCreatedAtDesc();
    
    // Find by YouTube Video ID
    Optional<File> findByYoutubeVideoId(String youtubeVideoId);
    
    // Find by original file name (case-insensitive partial match)
    List<File> findByOriginalFileNameContainingIgnoreCase(String fileName);
    
    // Find by date range
    List<File> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Combined search with multiple optional filters using native query
    @Query(value = "SELECT DISTINCT f.* FROM files f LEFT JOIN file_tags t ON f.id = t.file_id WHERE " +
           "(:fileName IS NULL OR f.original_file_name ILIKE CONCAT('%', CAST(:fileName AS VARCHAR), '%')) AND " +
           "(:tag IS NULL OR t.tag = :tag) AND " +
           "(CAST(:startDate AS TIMESTAMP) IS NULL OR f.created_at >= CAST(:startDate AS TIMESTAMP)) AND " +
           "(CAST(:endDate AS TIMESTAMP) IS NULL OR f.created_at <= CAST(:endDate AS TIMESTAMP)) " +
           "ORDER BY f.created_at DESC",
           nativeQuery = true)
    List<File> searchFiles(@Param("fileName") String fileName,
                           @Param("tag") String tag,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);
}
