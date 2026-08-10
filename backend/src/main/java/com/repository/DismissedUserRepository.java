
package com.repository;

import com.model.DismissedUser;
import com.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DismissedUserRepository extends JpaRepository<DismissedUser, Long> {

    @Query("SELECT d FROM DismissedUser d WHERE d.dismissedBy = :user")
    List<DismissedUser> findByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DismissedUser d WHERE d.dismissedBy = :dismissedBy AND d.dismissedUser = :dismissedUser")
    boolean existsByUsers(@Param("dismissedBy") User dismissedBy, @Param("dismissedUser") User dismissedUser);
}