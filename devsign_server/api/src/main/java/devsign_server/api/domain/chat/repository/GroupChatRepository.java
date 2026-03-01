package devsign_server.api.domain.chat.repository;

import devsign_server.api.domain.chat.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    Optional<GroupChat> findByProjectProjectId(Long projectId);
}
