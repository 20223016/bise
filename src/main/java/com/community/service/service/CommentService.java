package com.community.service.service;

import com.community.service.entity.Comment;
import com.community.service.entity.User;
import com.community.service.mapper.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CommentService {

    private final CommentRepository commentRepository;

    // 获取指定对象的评论列表
    public List<Comment> getCommentsByTarget(String targetType, Long targetId) {
        return commentRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    // 获取用户的评论列表
    public List<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUser(user);
    }

    // 获取评论的回复列表
    public List<Comment> getRepliesByCommentId(Long commentId) {
        return commentRepository.findByParentId(commentId);
    }

    // 创建评论
    @Transactional
    public Comment createComment(User user, String targetType, Long targetId, String content, Long parentId) {
        Comment comment = Comment.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .parentId(parentId)
                .build();
        return commentRepository.save(comment);
    }

    // 删除评论
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        // 检查是否是评论作者
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此评论");
        }
        commentRepository.delete(comment);
    }

    // 点赞评论
    @Transactional
    public void likeComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setLikesCount(comment.getLikesCount() + 1);
        commentRepository.save(comment);
    }
}
