package devsign_server.api.domain.chat.service;

import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.chat.dto.MessageResponse;
import devsign_server.api.domain.chat.entity.GroupChat;
import devsign_server.api.domain.chat.entity.Message;
import devsign_server.api.domain.chat.repository.GroupChatRepository;
import devsign_server.api.domain.chat.repository.MessageRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final GroupChatRepository groupChatRepository;
    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final ApplicantRepository applicantRepository;

    public List<MessageResponse> getMessages(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        checkChatAccess(memberId, project);

        GroupChat groupChat = groupChatRepository.findByProjectProjectId(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        return messageRepository.findByGroupChatGroupChatIdOrderByCreatedAtAsc(groupChat.getGroupChatId())
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse saveMessage(Long memberId, Long groupChatId, String content) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));

        checkChatAccess(memberId, groupChat.getProject());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Message message = Message.builder()
                .groupChat(groupChat)
                .member(member)
                .content(content)
                .build();

        return MessageResponse.from(messageRepository.save(message));
    }

    private void checkChatAccess(Long memberId, Project project) {
        boolean isAuthor = project.isAuthor(memberId);
        boolean isApproved = applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                project.getProjectId(), memberId, ApplicantStatus.APPROVED);

        if (!isAuthor && !isApproved) {
            throw new CustomException(ErrorCode.NOT_APPROVED_MEMBER);
        }
    }
}
