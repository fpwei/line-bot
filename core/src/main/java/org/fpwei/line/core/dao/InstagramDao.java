package org.fpwei.line.core.dao;

import org.fpwei.line.core.entity.Instagram;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstagramDao extends PagingAndSortingRepository<Instagram, Integer> {

    @Query(value = "SELECT * FROM instagram WHERE status = '1' ORDER BY RAND() LIMIT :rows", nativeQuery = true)
    List<Instagram> findInstagramByRandom(@Param("rows") int rows);
}
