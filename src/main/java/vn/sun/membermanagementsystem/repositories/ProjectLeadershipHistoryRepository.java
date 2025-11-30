package vn.sun.membermanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.sun.membermanagementsystem.entities.ProjectLeadershipHistory;

@Repository
public interface ProjectLeadershipHistoryRepository extends JpaRepository<ProjectLeadershipHistory, Long> {

}
