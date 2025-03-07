package com.app.backend.domain.post.service.post.domain.chat.message.service;

import com.app.backend.domain.chat.message.dto.request.MessageRequest;
import com.app.backend.domain.chat.message.dto.response.MessageResponse;
import com.app.backend.domain.chat.message.entity.Message;
import com.app.backend.domain.chat.message.repository.MessageRepository;
import com.app.backend.domain.chat.message.service.MessageService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageRepository messageRepository;

	@InjectMocks
	private MessageService messageService;

	private List<Message> messageList;

	@BeforeEach
	void setUp() {
		messageList = IntStream.rangeClosed(1, 25)
			.mapToObj(i -> createMessage(
				100L, // 채팅방 ID
				(long)((i % 3) + 1), // senderId
				"User" + ((i % 3) + 1), // senderNickname
				"메시지 " + (26 - i), // content (메시지 1, 메시지 2, 메시지 3)
				LocalDateTime.now().minusSeconds(i * 10L)
			))
			.collect(Collectors.toList());
	}

	@Test
	@DisplayName("[성공] 채팅 메세지 조회 - 0번 페이지, 최신순 정렬")
	void getMessagesByChatRoomId() {
		//given
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("createdAt")));

		List<Message> pagedMessageList = messageList.stream()
			.sorted(Comparator.comparing(Message::getCreatedAt).reversed()) // 최신순 정렬
			.skip(pageable.getOffset()) // 0 * 20 = 0번째부터 시작
			.limit(pageable.getPageSize()) // 최대 20개 가져오기
			.toList();

		Page<Message> messages = new PageImpl<>(pagedMessageList, pageable, messageList.size());

		when(messageRepository.findByChatRoomIdAndDisabledFalse(any(Long.class), any(Pageable.class))).thenReturn(messages);

		//when
		Page<MessageResponse> result = messageService.getMessagesByChatRoomId(100L, 0, 20);

		//then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(25);
		assertThat(result.getContent().size()).isEqualTo(20);

		// 최신순 정렬 확인
		assertThat(result.getContent().get(0).content()).isEqualTo("메시지 25");
		assertThat(result.getContent().get(1).content()).isEqualTo("메시지 24");
		assertThat(result.getContent().get(2).content()).isEqualTo("메시지 23");
		assertThat(result.getContent().get(18).content()).isEqualTo("메시지 7");
		assertThat(result.getContent().get(19).content()).isEqualTo("메시지 6");
	}

	@Test
	@DisplayName("[성공] 채팅 메세지 조회 - 1번 페이지, 최신순 정렬")
	void getMessagesByChatRoomId2() {
		//given
		Pageable pageable = PageRequest.of(1, 20, Sort.by(Sort.Order.desc("createdAt")));

		List<Message> pagedMessageList = messageList.stream()
			.sorted(Comparator.comparing(Message::getCreatedAt).reversed()) // 최신순 정렬
			.skip(pageable.getOffset()) // 0 * 20 = 0번째부터 시작
			.limit(pageable.getPageSize()) // 최대 20개 가져오기
			.toList();

		Page<Message> messages = new PageImpl<>(pagedMessageList, pageable, messageList.size());

		when(messageRepository.findByChatRoomIdAndDisabledFalse(any(Long.class), any(Pageable.class))).thenReturn(messages);

		//when
		Page<MessageResponse> result = messageService.getMessagesByChatRoomId(100L, 1, 20);

		//then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(25);
		assertThat(result.getContent().size()).isEqualTo(5);

		// 최신순 정렬 확인
		assertThat(result.getContent().get(0).content()).isEqualTo("메시지 5");
		assertThat(result.getContent().get(1).content()).isEqualTo("메시지 4");
		assertThat(result.getContent().get(2).content()).isEqualTo("메시지 3");
		assertThat(result.getContent().get(3).content()).isEqualTo("메시지 2");
		assertThat(result.getContent().get(4).content()).isEqualTo("메시지 1");
	}

	@Test
	@DisplayName("[예외] 존재하지 않는 페이지 요청 (빈 결과 반환)")
	void getMessagesByChatRoomId_NonExistingPage() {
		// given
		Pageable pageable = PageRequest.of(2, 20, Sort.by(Sort.Order.desc("createdAt"))); // 2번 페이지 요청
		List<Message> pagedMessageList = List.of(); // 빈 리스트 반환

		Page<Message> messages = new PageImpl<>(pagedMessageList, pageable, messageList.size());

		when(messageRepository.findByChatRoomIdAndDisabledFalse(any(Long.class), any(Pageable.class))).thenReturn(messages);

		// when
		Page<MessageResponse> result = messageService.getMessagesByChatRoomId(100L, 2, 20);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(25); // 전체 메시지는 25개
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	@DisplayName("[성공] 메세지 저장")
	void saveMessage_Success() {
		// given
		MessageRequest request = new MessageRequest(2L, 1L, "user", "테스트 메세지");
		Message message = createMessage(2L, 1L, "user", "테스트 메세지", LocalDateTime.now());

		when(messageRepository.save(any(Message.class))).thenReturn(message);

		// when
		MessageResponse response = messageService.saveMessage(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.content()).isEqualTo("테스트 메세지");
		assertThat(response.senderNickname()).isEqualTo("user");

		verify(messageRepository, times(1)).save(any(Message.class));
	}

	private Message createMessage(Long chatRoomId, Long senderId, String senderNickname, String content, LocalDateTime createdAt) {
		return Message.builder()
			.id(new ObjectId())
			.chatRoomId(chatRoomId)
			.senderId(senderId)
			.senderNickname(senderNickname)
			.content(content)
			.createdAt(createdAt)
			.disabled(false)
			.build();
	}
}