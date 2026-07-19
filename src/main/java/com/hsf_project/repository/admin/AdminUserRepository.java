package com.hsf_project.repository.admin;

import com.hsf_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<User, Long> {
    @Query("select count(u) from User u where (u.isDeleted = false or u.isDeleted is null)")
    long countActiveUsers();

    @Query("select count(u) from User u where (u.isDeleted = false or u.isDeleted is null) and upper(u.role.roleName) = upper(:role)")
    long countByRole(@Param("role") String role);

    @Query("select u from User u left join fetch u.cinema where (u.isDeleted = false or u.isDeleted is null) and upper(u.role.roleName) = 'MANAGER' order by u.createdAt desc, u.id desc")
    List<User> findManagers();

    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(String email);
}
