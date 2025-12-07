package vn.sun.membermanagementsystem.services.impls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.sun.membermanagementsystem.dto.request.UserCreateDTO;
import vn.sun.membermanagementsystem.dto.response.UserListItemDTO;
import vn.sun.membermanagementsystem.dto.response.UserProfileDetailDTO;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.UserMapper;
import vn.sun.membermanagementsystem.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDTO userCreateDTO;
    private UserProfileDetailDTO userProfileDetailDTO;
    private UserListItemDTO userListItemDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = User.builder()
                .id(1L)
                .name("Lê Quốc Việt")
                .email("le.quoc.viet-c@sun-asterisk.com")
                .passwordHash("123qwe!@#")
                .birthday(LocalDate.of(2004, 5, 27))
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("Lê Quốc Việt");
        userCreateDTO.setEmail("le.quoc.viet-c@sun-asterisk.com");
        userCreateDTO.setPassword("123qwe!@#");
        userCreateDTO.setBirthday(LocalDate.of(2004, 5, 27));
        userCreateDTO.setRole(UserRole.MEMBER);
        userCreateDTO.setStatus(UserStatus.ACTIVE);

        userProfileDetailDTO = UserProfileDetailDTO.builder()
                .id(1L)
                .name("Lê Quốc Việt")
                .email("le.quoc.viet-c@sun-asterisk.com")
                .birthday(LocalDate.of(2004, 5, 27))
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userListItemDTO = UserListItemDTO.builder()
                .id(1L)
                .name("Lê Quốc Việt")
                .email("le.quoc.viet-c@sun-asterisk.com")
                .birthday(LocalDate.of(2004, 5, 27))
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Create user successfully")
    void testCreateUser_Success() {
        when(userRepository.existsByEmailAndNotDeleted(userCreateDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toProfileDetailDTO(testUser)).thenReturn(userProfileDetailDTO);

        UserProfileDetailDTO result = userService.createUser(userCreateDTO);

        assertNotNull(result);
        assertEquals(userProfileDetailDTO.getId(), result.getId());
        assertEquals(userProfileDetailDTO.getName(), result.getName());
        assertEquals(userProfileDetailDTO.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmailAndNotDeleted(userCreateDTO.getEmail());
        verify(passwordEncoder, times(1)).encode(userCreateDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toProfileDetailDTO(testUser);
    }

    @Test
    @DisplayName("Create user with duplicate email should throw DuplicateResourceException")
    void testCreateUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmailAndNotDeleted(userCreateDTO.getEmail())).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.createUser(userCreateDTO)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, times(1)).existsByEmailAndNotDeleted(userCreateDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Create user with null status should default to ACTIVE")
    void testCreateUser_NullStatus_DefaultsToActive() {
        userCreateDTO.setStatus(null);
        when(userRepository.existsByEmailAndNotDeleted(userCreateDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            return testUser;
        });
        when(userMapper.toProfileDetailDTO(any(User.class))).thenReturn(userProfileDetailDTO);

        UserProfileDetailDTO result = userService.createUser(userCreateDTO);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Delete user successfully")
    void testDeleteUser_Success() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Delete non-existing user should throw ResourceNotFoundException")
    void testDeleteUser_NotFound_ThrowsException() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        assertTrue(exception.getMessage().contains("User not found with ID"));
        verify(userRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get user detail by ID successfully")
    void testGetUserDetailById_Success() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileDetailDTO(testUser)).thenReturn(userProfileDetailDTO);

        UserProfileDetailDTO result = userService.getUserDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Lê Quốc Việt", result.getName());
        verify(userRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(userMapper, times(1)).toProfileDetailDTO(testUser);
    }

    @Test
    @DisplayName("Get user detail by ID not found should throw ResourceNotFoundException")
    void testGetUserDetailById_NotFound_ThrowsException() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserDetailById(1L)
        );

        assertTrue(exception.getMessage().contains("User not found with ID"));
        verify(userRepository, times(1)).findByIdAndNotDeleted(1L);
    }

    @Test
    @DisplayName("Get all users successfully")
    void testGetAllUsers_Success() {
        User user2 = User.builder()
                .id(2L)
                .name("Lê Quốc A")
                .email("le.quoc.a@sun-asterisk.com")
                .build();

        List<User> users = Arrays.asList(testUser, user2);
        List<UserListItemDTO> expectedDTOs = Arrays.asList(
                userListItemDTO,
                UserListItemDTO.builder().id(2L).name("Lê Quốc A").build()
        );

        when(userRepository.findAllNotDeleted()).thenReturn(users);
        when(userMapper.toListItemDTOList(users)).thenReturn(expectedDTOs);

        List<UserListItemDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAllNotDeleted();
        verify(userMapper, times(1)).toListItemDTOList(users);
    }

    @Test
    @DisplayName("Get all users returns empty list when no users exist")
    void testGetAllUsers_EmptyList() {
        when(userRepository.findAllNotDeleted()).thenReturn(List.of());
        when(userMapper.toListItemDTOList(anyList())).thenReturn(List.of());

        List<UserListItemDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAllNotDeleted();
    }

    @Test
    @DisplayName("Get users by status successfully")
    void testGetUsersByStatus_Success() {
        List<User> users = Arrays.asList(testUser);
        List<UserListItemDTO> expectedDTOs = Arrays.asList(userListItemDTO);

        when(userRepository.findByStatusAndNotDeleted(UserStatus.ACTIVE)).thenReturn(users);
        when(userMapper.toListItemDTOList(users)).thenReturn(expectedDTOs);

        List<UserListItemDTO> result = userService.getUsersByStatus(UserStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserStatus.ACTIVE, result.get(0).getStatus());
        verify(userRepository, times(1)).findByStatusAndNotDeleted(UserStatus.ACTIVE);
        verify(userMapper, times(1)).toListItemDTOList(users);
    }

    @Test
    @DisplayName("Get users by role successfully")
    void testGetUsersByRole_Success() {
        List<User> users = Arrays.asList(testUser);
        List<UserListItemDTO> expectedDTOs = Arrays.asList(userListItemDTO);

        when(userRepository.findByRoleAndNotDeleted(UserRole.MEMBER)).thenReturn(users);
        when(userMapper.toListItemDTOList(users)).thenReturn(expectedDTOs);

        List<UserListItemDTO> result = userService.getUsersByRole(UserRole.MEMBER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserRole.MEMBER, result.get(0).getRole());
        verify(userRepository, times(1)).findByRoleAndNotDeleted(UserRole.MEMBER);
        verify(userMapper, times(1)).toListItemDTOList(users);
    }

    @Test
    @DisplayName("Get user by email successfully")
    void testGetUserByEmail_Success() {
        String email = "le.quoc.viet-c@sun-asterisk.com";
        when(userRepository.findByEmailAndNotDeleted(email)).thenReturn(Optional.of(testUser));
        when(userMapper.toListItemDTO(testUser)).thenReturn(userListItemDTO);

        UserListItemDTO result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmailAndNotDeleted(email);
        verify(userMapper, times(1)).toListItemDTO(testUser);
    }

    @Test
    @DisplayName("Get user by email not found should throw ResourceNotFoundException")
    void testGetUserByEmail_NotFound_ThrowsException() {
        String email = "notfound@example.com";
        when(userRepository.findByEmailAndNotDeleted(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByEmail(email)
        );

        assertTrue(exception.getMessage().contains("User not found with email"));
        verify(userRepository, times(1)).findByEmailAndNotDeleted(email);
    }

    @Test
    @DisplayName("Delete user sets deletedAt timestamp")
    void testDeleteUser_SetsDeletedAt() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertNotNull(user.getDeletedAt());
            return user;
        });

        userService.deleteUser(1L);

        verify(userRepository, times(1)).save(argThat(user -> user.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Create user encodes password correctly")
    void testCreateUser_EncodesPassword() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        userCreateDTO.setPassword(rawPassword);
        when(userRepository.existsByEmailAndNotDeleted(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(encodedPassword, user.getPasswordHash());
            return testUser;
        });
        when(userMapper.toProfileDetailDTO(any(User.class))).thenReturn(userProfileDetailDTO);

        userService.createUser(userCreateDTO);

        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("Create user sets timestamps correctly")
    void testCreateUser_SetsTimestamps() {
        when(userRepository.existsByEmailAndNotDeleted(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertNull(user.getDeletedAt());
            return testUser;
        });
        when(userMapper.toProfileDetailDTO(any(User.class))).thenReturn(userProfileDetailDTO);

        userService.createUser(userCreateDTO);

        verify(userRepository, times(1)).save(argThat(user ->
                user.getCreatedAt() != null && user.getUpdatedAt() != null && user.getDeletedAt() == null
        ));
    }
}
