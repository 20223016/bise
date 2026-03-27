package com.community.service.mapper;

import com.community.service.entity.Comment;
import com.community.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 根据评论对象类型和ID查询评论
    List<Comment> findByTargetTypeAndTargetId(String targetType, Long targetId);
    
    // 根据用户查询评论
    List<Comment> findByUser(User user);
    
    // 根据父评论ID查询回复
    List<Comment> findByParentId(Long parentId);
}
