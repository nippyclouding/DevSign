package devsign_server.api.domain.chat.repository;

import devsign_server.api.domain.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGroupChatGroupChatIdOrderByCreatedAtAsc(Long groupChatId);
}
