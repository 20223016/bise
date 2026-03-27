package com.community.service.mapper;

import com.community.service.entity.Registration;
import com.community.service.entity.User;
import com.community.service.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByVolunteer(User volunteer);
    List<Registration> findByActivity(Activity activity);
    Optional<Registration> findByVolunteerAndActivity(User volunteer, Activity activity);
    void deleteByActivityId(Long activityId);
}
