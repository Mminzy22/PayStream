package com.paystream.inventory.photo.repository;

import com.paystream.inventory.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Photo findByFileName(String fileName);
}
