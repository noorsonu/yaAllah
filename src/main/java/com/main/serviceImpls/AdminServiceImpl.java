package com.main.serviceImpls;

import com.main.dtos.*;
import com.main.entities.Comment;
import com.main.entities.Post;
import com.main.entities.UserEntity;
import com.main.repositories.CommentRepository;
import com.main.repositories.PostRepository;
import com.main.repositories.UserRepository;
import com.main.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long totalAdmins = userRepository.countByUserType("ADMIN");
        
        // Get today's start time in UTC
        Instant todayStart = java.time.LocalDate.now(java.time.ZoneOffset.UTC).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        long usersToday = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(todayStart))
                .count();
        
        long postsToday = postRepository.findAll().stream()
                .filter(post -> post.getCreatedAt().isAfter(todayStart))
                .count();
        
        long commentsToday = commentRepository.findAll().stream()
                .filter(comment -> comment.getCreatedAt().isAfter(todayStart))
                .count();

        return new AdminDashboardStatsDto(totalUsers, totalPosts, totalComments, totalAdmins, 
                                        usersToday, postsToday, commentsToday);
    }

    @Override
    public List<AdminUserDto> getAllUsersWithStats() {
        return userRepository.findAll().stream()
                .map(this::convertToAdminUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserDto getUserWithStats(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToAdminUserDto(user);
    }

    @Override
    public AdminUserDto updateUser(Long userId, AdminUpdateUserRequest updateRequest) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getName() != null) {
            user.setName(updateRequest.getName());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getUserType() != null) {
            user.setUserType(updateRequest.getUserType());
        }

        UserEntity updatedUser = userRepository.save(user);
        return convertToAdminUserDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPostDto> getAllPostsWithStats() {

        List<Post> posts = postRepository.findAll();
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        return posts.stream()
                .map(this::convertToAdminPostDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public AdminPostDto getPostWithStats(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToAdminPostDto(post);
    }

    @Override
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCommentDto> getAllCommentsWithDetails() {
        return commentRepository.findAll().stream()
                .map(this::convertToAdminCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }

    private AdminUserDto convertToAdminUserDto(UserEntity user) {
        long postCount = postRepository.countByCreatedBy(user);
        long commentCount = commentRepository.countByUser(user);
        
        return new AdminUserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserType(),
                user.getCreatedAt(),
                postCount,
                commentCount
        );
    }

    private AdminPostDto convertToAdminPostDto(Post post) {
        long commentCount = commentRepository.countByPost(post);

        String authorName = null;
        String authorEmail = null;
        UserEntity createdBy = post.getCreatedBy();
        if (createdBy != null) {
            authorName = createdBy.getName();
            authorEmail = createdBy.getEmail();
        }

        return new AdminPostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                authorName,
                authorEmail,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentCount
        );
    }

    private AdminCommentDto convertToAdminCommentDto(Comment comment) {
        return new AdminCommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getPost().getTitle(),
                comment.getUser().getName(),
                comment.getUser().getEmail(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

	@Override
	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("User not found");
		}
		userRepository.deleteById(userId);
	}
}