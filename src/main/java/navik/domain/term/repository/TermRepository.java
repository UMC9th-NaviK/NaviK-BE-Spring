package navik.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import navik.domain.term.entity.Term;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
}
