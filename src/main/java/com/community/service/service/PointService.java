package com.community.service.service;

import com.community.service.entity.PointRecord;
import com.community.service.entity.Product;
import com.community.service.entity.Redemption;
import com.community.service.entity.User;
import com.community.service.mapper.PointRecordRepository;
import com.community.service.mapper.ProductRepository;
import com.community.service.mapper.RedemptionRepository;
import com.community.service.mapper.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PointService {

    private final PointRecordRepository pointRecordRepository;
    private final ProductRepository productRepository;
    private final RedemptionRepository redemptionRepository;
    private final UserRepository userRepository;

    public List<PointRecord> getPointRecordsByUser(User user) {
        return pointRecordRepository.findByUser(user);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void addPoints(User user, Integer amount, String description, PointRecord.PointType type) {
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);

        PointRecord record = new PointRecord();
        record.setUser(user);
        record.setAmount(amount);
        record.setDescription(description);
        record.setType(type);
        record.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(record);
    }

    @Transactional
    public Redemption redeemProduct(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        int totalPoints = product.getRequiredPoints() * quantity;

        if (user.getPoints() < totalPoints) {
            throw new RuntimeException("积分不足，多多服务赚取积分吧！");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // Deduct points
        user.setPoints(user.getPoints() - totalPoints);
        userRepository.save(user);

        // Update product stock
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Create point record
        PointRecord record = new PointRecord();
        record.setUser(user);
        record.setAmount(-totalPoints);
        record.setDescription("兑换商品: " + product.getName());
        record.setType(PointRecord.PointType.REDEMPTION);
        record.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(record);

        // Create redemption record
        Redemption redemption = new Redemption();
        redemption.setUser(user);
        redemption.setProduct(product);
        redemption.setQuantity(quantity);
        redemption.setTotalPoints(totalPoints);
        redemption.setRedemptionTime(LocalDateTime.now());
        redemption.setStatus(Redemption.RedemptionStatus.PENDING);
        return redemptionRepository.save(redemption);
    }

    // 每日签到
    @Transactional
    public void signIn(User user) {
        // 检查今日是否已签到
        List<PointRecord> todayRecords = pointRecordRepository.findByUserAndTypeAndCreatedAtAfter(
                user, PointRecord.PointType.SIGN_IN, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        
        if (!todayRecords.isEmpty()) {
            throw new RuntimeException("今日已签到");
        }
        
        // 签到奖励积分
        int signInPoints = 5; // 每日签到5积分
        addPoints(user, signInPoints, "每日签到奖励", PointRecord.PointType.SIGN_IN);
    }

    // 首次注册奖励
    @Transactional
    public void firstRegisterReward(User user) {
        // 检查是否已有注册奖励
        List<PointRecord> registerRecords = pointRecordRepository.findByUserAndType(
                user, PointRecord.PointType.FIRST_REGISTER);
        
        if (!registerRecords.isEmpty()) {
            return; // 已经领取过注册奖励
        }
        
        // 注册奖励积分
        int registerPoints = 100; // 首次注册100积分
        addPoints(user, registerPoints, "首次注册奖励", PointRecord.PointType.FIRST_REGISTER);
    }

    // 活动参与奖励
    @Transactional
    public void activityParticipationReward(User user, String activityName) {
        int participationPoints = 10; // 活动参与10积分
        addPoints(user, participationPoints, "参与活动: " + activityName, PointRecord.PointType.ACTIVITY_PARTICIPATION);
    }

    // 帮助他人解决需求奖励
    @Transactional
    public void demandHelpReward(User user, String demandTitle) {
        int helpPoints = 15; // 帮助他人15积分
        addPoints(user, helpPoints, "帮助解决需求: " + demandTitle, PointRecord.PointType.DEMAND_HELP);
    }

    // 招募志愿者奖励
    @Transactional
    public void volunteerRecruitReward(User user, int count) {
        int recruitPoints = 5 * count; // 每招募1人5积分
        addPoints(user, recruitPoints, "招募志愿者奖励: " + count + "人", PointRecord.PointType.VOLUNTEER_RECRUIT);
    }

    // 优秀志愿者奖励
    @Transactional
    public void excellentVolunteerReward(User user, String reason) {
        int excellentPoints = 50; // 优秀志愿者50积分
        addPoints(user, excellentPoints, "优秀志愿者奖励: " + reason, PointRecord.PointType.EXCELLENT_VOLUNTEER);
    }

    // 违规罚款
    @Transactional
    public void fine(User user, int amount, String reason) {
        if (user.getPoints() < amount) {
            throw new RuntimeException("积分余额不足");
        }
        
        user.setPoints(user.getPoints() - amount);
        userRepository.save(user);

        PointRecord record = PointRecord.builder()
                .user(user)
                .amount(-amount)
                .description("违规罚款: " + reason)
                .type(PointRecord.PointType.FINE)
                .createdAt(LocalDateTime.now())
                .build();
        pointRecordRepository.save(record);
    }
}
