package navik.global.auth.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralExceptionHandler;
import navik.global.auth.JwtUserDetails;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		// username은 User의 pk(userid)
		return userRepository.findById(Long.parseLong(userId))
			.map(this::createUserDetails)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
	}

	// DB에서 가져온 User 객체를 Spring Security의 UserDetails 객체로 변환
	private UserDetails createUserDetails(User user) {
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().getKey());

		return new JwtUserDetails(
			user.getId(),
			user.getUserStatus().toString(),
			List.of(grantedAuthority)
		);
	}
}
