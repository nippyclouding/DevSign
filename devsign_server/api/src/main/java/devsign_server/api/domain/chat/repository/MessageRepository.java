package devsign_server.api.domain.chat.repository;

import devsign_server.api.domain.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m JOIN FETCH m.member WHERE m.groupChat.groupChatId = :groupChatId ORDER BY m.createdAt ASC")
    List<Message> findByGroupChatGroupChatIdOrderByCreatedAtAsc(@Param("groupChatId") Long groupChatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.member.memberId = :memberId")
    void deleteByMemberMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.groupChat.project.member.memberId = :memberId")
    void deleteByGroupChatProjectMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.groupChat.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
