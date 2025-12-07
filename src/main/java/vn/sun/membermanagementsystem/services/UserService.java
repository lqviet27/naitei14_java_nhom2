package vn.sun.membermanagementsystem.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.sun.membermanagementsystem.dto.request.UserCreateDTO;
import vn.sun.membermanagementsystem.dto.request.UserUpdateDTO;
import vn.sun.membermanagementsystem.dto.response.UserListItemDTO;
import vn.sun.membermanagementsystem.dto.response.UserProfileDetailDTO;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.util.List;

public interface UserService {

    // Create and Update operations - return full detail after operation
    UserProfileDetailDTO createUser(UserCreateDTO userCreateDTO);
    UserProfileDetailDTO updateUser(UserUpdateDTO userUpdateDTO);
    boolean deleteUser(Long userId);
    
    // Read operations
    UserProfileDetailDTO getUserDetailById(Long userId);
    UserUpdateDTO getUserFormById(Long userId);
    
    // List operations - return lightweight DTO
    List<UserListItemDTO> getAllUsers();
    List<UserListItemDTO> getUsersByStatus(UserStatus status);
    List<UserListItemDTO> getUsersByRole(UserRole role);
    UserListItemDTO getUserByEmail(String email);
    
    // Paginated list operations
    Page<UserListItemDTO> getAllUsersForList(Pageable pageable);
    Page<UserListItemDTO> searchUsersForList(String keyword, UserStatus status, UserRole role, Pageable pageable);
    Page<UserListItemDTO> searchUsersForListWithTeam(String keyword, UserStatus status, UserRole role, Long teamId, Pageable pageable);
}
