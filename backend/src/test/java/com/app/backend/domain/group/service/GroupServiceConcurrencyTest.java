package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
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
class GroupServiceConcurrencyTest extends SpringBootTestSupporter {

    private static final Logger log          = LoggerFactory.getLogger(GroupServiceConcurrencyTest.class);
    private static final int    THREAD_COUNT = Math.max(100, Runtime.getRuntime().availableProcessors());

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @Order(1)
    @DisplayName("[Normal] modifyGroup(): 여러 클라이언트에서 동시에 같은 ID로 모임 조회 후 값 수정 시도")
    void modifyGroup() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> memberRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Category> newCategoryRef = new AtomicReference<>();
        AtomicReference<Group>    groupRef       = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = new Category("category");
                em.persist(category);
                Category newCategory = new Category("nCategory");
                em.persist(newCategory);
                newCategoryRef.set(newCategory);

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

                for (int i = 1; i <= memberRefs.size(); i++) {
                    Member member = Member.create("testUsername%d".formatted(i),
                                                  "testPassword%d".formatted(i),
                                                  "testNickname%d".formatted(i),
                                                  "ROLE_USER",
                                                  false,
                                                  Provider.LOCAL,
                                                  null);
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.LEADER);
                    em.persist(groupMembership);
                    memberRefs.get(i - 1).set(member);
                }

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> members     = memberRefs.stream().map(AtomicReference::get).toList();
        Category     newCategory = newCategoryRef.get();
        Group        group       = groupRef.get();

        Long       groupId         = group.getId();
        List<Long> memberIds       = members.stream().map(Member::getId).toList();
        String     newCategoryName = newCategory.getName();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger lastUpdatedThreadIndex   = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    GroupRequest.Update update = new GroupRequest.Update("new test%d".formatted(threadIndex),
                                                                         "new test province%d".formatted(threadIndex),
                                                                         "new test city%d".formatted(threadIndex),
                                                                         "new test town%d".formatted(threadIndex),
                                                                         "new test description%d"
                                                                                 .formatted(threadIndex),
                                                                         RecruitStatus.CLOSED.name(),
                                                                         20,
                                                                         newCategoryName);

                    Thread.sleep(100);
                    groupService.modifyGroup(groupId, memberIds.get(threadIndex % memberIds.size()), update);

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
            Group updatedGroup = em.find(Group.class, groupId);

            assertThat(updatedGroup.getName()).isNotEqualTo(group.getName());
            assertThat(updatedGroup.getName()).isEqualTo("new test%d".formatted(lastIndex));
            assertThat(updatedGroup.getProvince()).isNotEqualTo(group.getProvince());
            assertThat(updatedGroup.getProvince()).isEqualTo("new test province%d".formatted(lastIndex));
            assertThat(updatedGroup.getCity()).isNotEqualTo(group.getCity());
            assertThat(updatedGroup.getCity()).isEqualTo("new test city%d".formatted(lastIndex));
            assertThat(updatedGroup.getTown()).isNotEqualTo(group.getTown());
            assertThat(updatedGroup.getTown()).isEqualTo("new test town%d".formatted(lastIndex));
            assertThat(updatedGroup.getDescription()).isNotEqualTo(group.getDescription());
            assertThat(updatedGroup.getDescription()).isEqualTo("new test description%d".formatted(lastIndex));
            assertThat(updatedGroup.getRecruitStatus()).isNotEqualTo(group.getRecruitStatus());
            assertThat(updatedGroup.getRecruitStatus()).isEqualTo(RecruitStatus.CLOSED);
            assertThat(updatedGroup.getMaxRecruitCount()).isNotEqualTo(group.getMaxRecruitCount());
            assertThat(updatedGroup.getMaxRecruitCount()).isEqualTo(20);
            assertThat(updatedGroup.getCategory().getName()).isNotEqualTo(group.getCategory().getName());
            assertThat(updatedGroup.getCategory().getName()).isEqualTo(newCategoryName);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Test
    @Order(2)
    @DisplayName("[Normal] deleteGroup(): 여러 클라이언트에서 동시에 같은 ID로 모임 삭제 시도")
    void deleteGroup() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> memberRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Group> groupRef = new AtomicReference<>();

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

                for (int i = 1; i <= memberRefs.size(); i++) {
                    Member member = Member.create("testUsername%d".formatted(i),
                                                  "testPassword%d".formatted(i),
                                                  "testNickname%d".formatted(i),
                                                  "ROLE_USER",
                                                  false,
                                                  Provider.LOCAL,
                                                  null);
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.Companion.of(member, group, GroupRole.LEADER);
                    em.persist(groupMembership);
                    memberRefs.get(i - 1).set(member);
                }

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> members = memberRefs.stream().map(AtomicReference::get).toList();
        Group        group   = groupRef.get();

        Long       groupId   = group.getId();
        List<Long> memberIds = members.stream().map(Member::getId).toList();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger firstDeletedThreadIndex  = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(100);
                    boolean flag = groupService.deleteGroup(groupId, memberIds.get(threadIndex % memberIds.size()));

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    if (flag && firstDeletedThreadIndex.get() == 0) {
                        firstDeletedThreadIndex.compareAndSet(0, threadIndex);
                        log.info("[{}-thread] First deleted the group", threadIndex);
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
        int firstIndex = firstDeletedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(firstIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Group deletedGroup = em.find(Group.class, groupId);
            List<GroupMembership> deletedGroupMemberships = em
                    .createQuery("SELECT gm FROM GroupMembership gm WHERE gm.groupId = :groupId", GroupMembership.class)
                    .setParameter("groupId", groupId)
                    .getResultList();

            assertThat(deletedGroup.getDisabled()).isTrue();
            assertThat(deletedGroupMemberships.stream().noneMatch(GroupMembership::getDisabled)).isFalse();
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

}