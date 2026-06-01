package com.example.demo.repository;

import com.example.demo.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("SELECT r FROM Rental r JOIN FETCH r.user JOIN FETCH r.book WHERE r.user.id = :userId")
    List<Rental> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Rental r JOIN FETCH r.user JOIN FETCH r.book WHERE r.user.id = :userId AND r.status = :status")
    List<Rental> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT r FROM Rental r JOIN FETCH r.user JOIN FETCH r.book WHERE r.status = 'PENDING_APPROVAL'")
    List<Rental> findPendingReturnsWithDetails();

    @Query("SELECT r FROM Rental r JOIN FETCH r.user JOIN FETCH r.book ORDER BY r.startDate DESC")
    List<Rental> findAllWithDetails();
}