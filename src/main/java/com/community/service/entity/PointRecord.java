package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "point_records")
public class PointRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer amount; // positive for income, negative for expense

    private String description;

    @Enumerated(EnumType.STRING)
    private PointType type;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PointType {
        // 积分获取类型
        SERVICE_REWARD,      // 服务奖励
        SIGN_IN,             // 每日签到
        FIRST_REGISTER,      // 首次注册
        ACTIVITY_PARTICIPATION, // 活动参与
        DEMAND_HELP,         // 帮助他人解决需求
        VOLUNTEER_RECRUIT,   // 招募志愿者
        EXCELLENT_VOLUNTEER, // 优秀志愿者奖励
        
        // 积分消耗类型
        REDEMPTION,          // 兑换商品
        FINE,                // 违规罚款
        
        // 其他类型
        MANUAL_ADJUSTMENT    // 手动调整
    }
}
