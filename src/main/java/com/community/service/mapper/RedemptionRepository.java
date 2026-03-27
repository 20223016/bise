package com.community.service.mapper;

import com.community.service.entity.Product;
import com.community.service.entity.Redemption;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findByUser(User user);
    List<Redemption> findByProduct(Product product);
    void deleteByProduct(Product product);
}
