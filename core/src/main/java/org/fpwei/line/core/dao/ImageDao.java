package org.fpwei.line.core.dao;

import org.fpwei.line.core.entity.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ImageDao extends PagingAndSortingRepository<Image, Integer> {
    Image findFirstByPostId(int postId);

    List<Image> findImagesByPostId(int postId, Pageable pageable);

    int countByPostId(int postId);
}
