package study.data_jpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// @Repository 의 의미 1. 스프링빈에 컨포넌트 스캔 대상.
// 2. 예외가 터지면 JPA 예외를 스프링이 추상화한 예외로 변환
// @Transaction
// 트랜잭션을 걸고 들어오면 그 트랜잭션을 이어받음,
// 트랜잭션이 없어도 스프링 DATA JPA는 자기 레포지토리 계층에서 트랜잭션을 시작한다.
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member>{

    // 스프링 데이터 JPA는 메서드 이름을 분석해서 JPQL을 생성하고 실행
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);
    
    // 많이 쓰이는 JPQL 쿼리 작성 방법
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) " +
            "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    //반환타입을 Page, 파라미터 Pageable은 쿼리에 대한 조건 1페이지야, 2페이지야
    @Query(value = "select m from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    //clear()을 자동으로 해줌
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m join fetch m.team")
    List<Member> findMemberFetchJoin();

    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름으로 쿼리에서 특히 편리하다.
    //@EntityGraph(attributePaths = {"team"})

    //NamedEntityGraph 사용
    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly",  value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);

    //List<UsernameOnlyDto> findProjectionsByUsername(@Param("username") String username);
    <T> List<T> findProjectionsByUsername(String username, Class<T> type);
}
