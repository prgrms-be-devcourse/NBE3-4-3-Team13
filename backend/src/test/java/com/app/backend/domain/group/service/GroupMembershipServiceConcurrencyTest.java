package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Tag("concurrency")
@SqlGroup({
        @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
             scripts = "classpath:/sql/truncate_tbl.sql"),
        @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
             scripts = "classpath:/sql/truncate_tbl.sql")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupMembershipServiceConcurrencyTest extends SpringBootTestSupporter {

    private static final Logger log          = LoggerFactory.getLogger(GroupMembershipServiceConcurrencyTest.class);
    private static final int    THREAD_COUNT = Math.max(100, Runtime.getRuntime().availableProcessors());

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @Order(1)
    @DisplayName("[Normal] approveJoining(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 신청 허가/거부 시도")
    void approveJoining() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> leaderRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = new Category("category");
                em.persist(category);

                Group group = Group.Companion.of("test",
                                                 "test province",
                                                 "test city",
                                                 "test town",
                                                 "test description",
                                                 RecruitStatus.RECRUITING,
                                                 10,
                                                 category);
                em.persist(group);
                groupRef.set(group);

                for (int i = 1; i <= leaderRefs.size(); i++) {
                    Member member = Member.create("testLeaderUsername%d".formatted(i),
                                                  "testLeaderPassword%d".formatted(i),
                                                  "testLeaderNickname%d".formatted(i),
                                                  "ROLE_USER",
                                                  false,
                                                  Provider.LOCAL,
                                                  null);
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.LEADER);
                    em.persist(groupMembership);
                    leaderRefs.get(i - 1).set(member);
                }
                Member member = Member.create("testLeaderUsername",
                                              "testLeaderPassword",
                                              "testLeaderNickname",
                                              "ROLE_USER",
                                              false,
                                              Provider.LOCAL,
                                              null);
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.PARTICIPANT);
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> leaders = leaderRefs.stream().map(AtomicReference::get).toList();
        Group        group   = groupRef.get();
        Member       member  = memberRef.get();

        Long       groupId   = group.getId();
        List<Long> leaderIds = leaders.stream().map(Member::getId).toList();
        Long       memberId  = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger firstUpdatedThreadIndex  = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(100);
                    boolean flag = groupMembershipService.approveJoining(leaderIds.get(threadIndex % leaderIds.size()),
                                                                         groupId,
                                                                         memberId,
                                                                         threadIndex % 2 == 0);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    if (flag && firstUpdatedThreadIndex.get() == 0) {
                        firstUpdatedThreadIndex.compareAndSet(0, threadIndex);
                        log.info("[{}-thread] First updated the group membership", threadIndex);
                    }
                } catch (GroupMembershipException e) {
                    log.info("[{}-thread] Entity not found: {}", threadIndex, e.getMessage());
                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int firstIndex = firstUpdatedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(firstIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class,
                                                             new GroupMembershipId(memberId, groupId));

            assertThat(updatedGroupMembership.getStatus())
                    .isEqualTo(firstIndex % 2 == 0 ? MembershipStatus.APPROVED : MembershipStatus.REJECTED);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Test
    @Order(2)
    @DisplayName("[Normal] modifyGroupRole(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 내 회원 권한 변경 시도")
    void modifyGroupRole() throws Exception {
        //Given+
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> leaderRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = new Category("category");
                em.persist(category);

                Group group = Group.Companion.of("test",
                                                 "test province",
                                                 "test city",
                                                 "test town",
                                                 "test description",
                                                 RecruitStatus.RECRUITING,
                                                 10,
                                                 category);
                em.persist(group);
                groupRef.set(group);

                for (int i = 1; i <= leaderRefs.size(); i++) {
                    Member member = Member.create("testLeaderUsername%d".formatted(i),
                                                  "testLeaderPassword%d".formatted(i),
                                                  "testLeaderNickname%d".formatted(i),
                                                  "ROLE_USER",
                                                  false,
                                                  Provider.LOCAL,
                                                  null);
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.LEADER);
                    em.persist(groupMembership);
                    leaderRefs.get(i - 1).set(member);
                }
                Member member = Member.create("testLeaderUsername",
                                              "testLeaderPassword",
                                              "testLeaderNickname",
                                              "ROLE_USER",
                                              false,
                                              Provider.LOCAL,
                                              null);
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.PARTICIPANT);
                groupMembership.modifyStatus(MembershipStatus.APPROVED);
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> leaders = leaderRefs.stream().map(AtomicReference::get).toList();
        Group        group   = groupRef.get();
        Member       member  = memberRef.get();

        Long       groupId   = group.getId();
        List<Long> leaderIds = leaders.stream().map(Member::getId).toList();
        Long       memberId  = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger lastUpdatedThreadIndex   = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(100);
                    groupMembershipService.modifyGroupRole(leaderIds.get(threadIndex % leaderIds.size()),
                                                           groupId,
                                                           memberId);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    lastUpdatedThreadIndex.set(threadIndex);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int lastIndex = lastUpdatedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(lastIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class,
                                                             new GroupMembershipId(memberId, groupId));

            assertThat(updatedGroupMembership.getGroupRole())
                    .isEqualTo(methodCallSuccessThreads.size() % 2 == 0 ? GroupRole.PARTICIPANT : GroupRole.LEADER);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Test
    @Order(3)
    @DisplayName("[Normal] leaveGroup(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 탈퇴 시도")
    void leaveGroup() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = new Category("category");
                em.persist(category);

                Group group = Group.Companion.of("test",
                                                 "test province",
                                                 "test city",
                                                 "test town",
                                                 "test description",
                                                 RecruitStatus.RECRUITING,
                                                 10,
                                                 category);
                em.persist(group);
                groupRef.set(group);

                Member leader = Member.create("testLeaderUsername",
                                              "testLeaderPassword",
                                              "testLeaderNickname",
                                              "ROLE_USER",
                                              false,
                                              Provider.LOCAL,
                                              null);
                em.persist(leader);
                GroupMembership groupLeaderMembership = GroupMembership.Companion.of(leader, group, GroupRole.LEADER);
                em.persist(groupLeaderMembership);

                Member member = Member.create("testUsername",
                                              "testPassword",
                                              "testNickname",
                                              "ROLE_USER",
                                              false,
                                              Provider.LOCAL,
                                              null);
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.PARTICIPANT);
                groupMembership.modifyStatus(MembershipStatus.APPROVED);
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        Group  group  = groupRef.get();
        Member member = memberRef.get();

        Long groupId  = group.getId();
        Long memberId = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger firstDeletedThreadIndex  = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(100);
                    boolean flag = groupMembershipService.leaveGroup(groupId, memberId);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    if (flag && firstDeletedThreadIndex.get() == 0) {
                        firstDeletedThreadIndex.compareAndSet(0, threadIndex);
                        log.info("[{}-thread] First deleted the group membership", threadIndex);
                    }
                } catch (GroupMembershipException e) {

                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int firstIndex = firstDeletedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(firstIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class,
                                                             new GroupMembershipId(memberId, groupId));

            assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.LEAVE);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

}