package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}