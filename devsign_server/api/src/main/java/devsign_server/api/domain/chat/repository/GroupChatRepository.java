package devsign_server.api.domain.chat.repository;

import devsign_server.api.domain.chat.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    Optional<GroupChat> findByProjectProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM GroupChat gc WHERE gc.project.member.memberId = :memberId")
    void deleteByProjectMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM GroupChat gc WHERE gc.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
