package com.app.backend.global.init.db

import com.app.backend.domain.category.entity.Category
import com.app.backend.domain.category.repository.CategoryRepository
import com.app.backend.domain.chat.room.entity.ChatRoom
import com.app.backend.domain.chat.room.repository.ChatRoomRepository
import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.group.entity.GroupMembership
import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.RecruitStatus
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.group.repository.GroupRepository
import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.repository.MemberRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Profile("dev")
@Component
class DataInit(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val groupRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val groupMembershipRepository: GroupMembershipRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        //외래 키를 참조하는 테이블부터 순서대로 삭제
        chatRoomRepository.deleteAll()
        groupMembershipRepository.deleteAll()
        groupRepository.deleteAll()
        categoryRepository.deleteAll()
        memberRepository.deleteAll()

        try {
            redisTemplate.connectionFactory?.connection?.serverCommands()
        } catch (e: Exception) {
            println("dev 모드 캐시 초기화 실패: ${e.message}")
        }

        if (memberRepository.findAll().none { it.role == "ROLE_ADMIN" })
            memberRepository.save(Member.create("admin", passwordEncoder.encode("admin"), "admin", "ROLE_ADMIN"))

        val categories = listOf("운동", "축구", "농구", "야구", "음식", "게임", "여행")
        val savedCategories = categories.map { categoryRepository.save(Category(it)) }

        val addresses = listOf(
            "서울 서초구 서초동", "서울 서초구 잠원동", "서울 서초구 반포동", "서울 서초구 방배동",
            "경기 성남시 분당구", "경기 수원시 권선구", "경기 안양시 만안구", "인천 남동구 구월동",
            "대전 중구 은행동", "대구 수성구 두산동", "광주 동구 용연동", "부산 수영구 광안동",
            "강원특별자치도 강릉시 송정동", "제주특별자치도 서귀포시 성산읍"
        )

        repeat(100) { i ->
            val category = savedCategories.random()

            val member = Member.create(
                "user${i + 1}",
                passwordEncoder.encode("user${i + 1}"),
                "user${i + 1}",
                "ROLE_USER",
                false,
                Member.Provider.LOCAL,
                null
            )
            memberRepository.save(member)

            val splitAddress = addresses.random().split(" ")
            val group = Group.of(
                "모임${i + 1}",
                splitAddress[0],
                splitAddress[1],
                splitAddress[2],
                "${category.name} 모임${i + 1} 입니다.",
                RecruitStatus.RECRUITING,
                Random.nextInt(49) + 1,
                category
            )
            groupRepository.save(group)

            groupMembershipRepository.save(GroupMembership.of(member, group, GroupRole.LEADER))

            val chatRoom = chatRoomRepository.save(ChatRoom(group))
            group.chatRoom = chatRoom

        }
    }
}