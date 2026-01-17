package navik.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import navik.domain.term.entity.Term;
import navik.domain.term.repository.projection.TermInfoView;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
	@Query("""
		SELECT t.id as id, t.content as content, t.updatedAt as updatedAt
		FROM Term t
		WHERE t.id = :termId
		""")
	TermInfoView getTermInfo(@Param("termId") Long termId);
}
