package navik.domain.term.service.factory;

import org.springframework.stereotype.Component;

import navik.domain.term.entity.Term;
import navik.domain.term.entity.UserTerm;
import navik.domain.users.entity.User;

@Component
public class UserTermFactory {
	public static UserTerm create(User user, Term term) {
		return UserTerm.builder()
			.user(user)
			.term(term)
			.isAgreed(true)
			.build();
	}
}
