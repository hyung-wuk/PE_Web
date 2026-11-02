package com.umc.movie.exception;

import com.umc.movie.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) // [1]
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON4001", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // [2]
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body(ApiResponse.failure("COMMON4091", "이미 존재하는 데이터입니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class) // [3]
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(409).body(ApiResponse.failure("COMMON4092", exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class) // [4]
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        return ResponseEntity.status(status)
                .body(ApiResponse.failure("COMMON" + status, exception.getReason()));
    }
}

/*
 * 예외가 발생하는 곳과 응답하는 곳은 다르다.
 * [1]·[2]는 우리가 throw를 직접 쓰지 않아도 Spring의 검증·DB 처리 과정에서 발생하고,
 * [3]·[4]는 현재 서비스 코드에서 throw new ...로 직접 던진다.
 * 이 클래스의 핸들러들은 그렇게 발생한 예외를 받아 오류 응답으로 바꾸는 역할이다.
 *
 * [1] handleValidation: @Valid로 요청 DTO를 검사하다 검증 규칙을 어겼을 때 발생하는
 * MethodArgumentNotValidException을 처리한다.
 * 발생 지점은 MemberController.signUp()의 @Valid @RequestBody SignUpRequest request다.
 * JSON을 DTO로 받은 뒤 Spring이 SignUpRequest의 @NotBlank, @Email, @Pattern, @Size 등을 검사한다.
 * 규칙을 어기면 Spring이 예외를 발생시키므로, 우리 코드에 이 예외를 직접 throw하는 줄은 없다.
 * 이 경우 컨트롤러 메서드 본문에 들어가기 전에 중단되므로 memberService.signUp()도 호출되지 않는다.
 * exception.getBindingResult() → 검증 결과를 가져온다.
 * .getFieldErrors() → 필드별 검증 오류 목록을 가져온다.
 * .get(0) → 목록에서 첫 번째 오류 하나를 선택한다.
 * .getDefaultMessage() → 그 오류의 메시지를 꺼낸다.
 * DTO의 @NotBlank(message = "이메일을 입력해주세요.") 등에 지정한 문구를 가져오는 과정이다.
 * 오류가 여러 개여도 여기서는 첫 번째 메시지만 사용하며, 오류 목록의 순서가 항상 같다고 보장되지는 않는다.
 *
 * [2] handleDataIntegrity: UNIQUE, FK 등 DB 제약 조건 위반으로 발생하는
 * DataIntegrityViolationException을 처리한다.
 * 현재는 MemberService.signUp()의 memberRepository.save(member)로 저장하는 흐름에서 발생할 수 있다.
 * DB가 제약 위반으로 저장을 거부하면, Spring이 해당 오류를 DataIntegrityViolationException으로 변환한다.
 * 우리가 직접 throw하지 않으며, DB 반영 시점에 따라 save() 호출 중이나 트랜잭션 커밋 과정에서 발생할 수 있다.
 * 예: 같은 이메일의 가입 요청 둘이 거의 동시에 서비스 중복 검사를 통과해도,
 * 먼저 저장한 요청 이후 나머지 요청은 DB의 이메일 UNIQUE 제약에 막힐 수 있다.
 * 여기서는 예외 객체에서 메시지를 꺼내지 않고 "이미 존재하는 데이터입니다."라는 고정 문구를 사용한다.
 * DB 예외 메시지에는 SQL이나 테이블명 같은 내부 정보가 포함될 수 있으므로 그대로 응답하지 않는다.
 * 이 문구는 워크북의 예시이며, 실제로는 중복 외에 FK 위반 등으로도 이 예외가 발생할 수 있다.
 *
 * [3] handleIllegalArgument: 서비스에서 인자가 부적절하다고 던진 IllegalArgumentException을 처리한다.
 * 현재 회원가입 서비스에서는 이메일·닉네임 중복을 발견했을 때 이 예외를 던진다.
 * 발생 지점은 MemberService.signUp()의 existsByEmail(), existsByNickname() 결과를 검사하는 if문이다.
 * 중복이면 우리가 직접 throw new IllegalArgumentException(...)을 실행해 저장으로 넘어가지 못하게 한다.
 * exception.getMessage()는 예외를 생성할 때 넣었던 설명 문자열을 꺼낸다.
 * 예: throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
 * → getMessage()의 결과는 "이미 사용 중인 이메일입니다."다.
 * 이 예외가 원래 중복 전용인 것은 아니며, 여기서 409로 처리하도록 정한 것이다.
 *
 * [4] handleResponseStatus: HTTP 상태와 이유를 지정하여 던진 ResponseStatusException을 처리한다.
 * 현재 직접 던지는 곳은 다음 두 군데다.
 * - MemberService.signUp(): 비밀번호가 UTF-8 기준 72바이트를 초과하면 BAD_REQUEST(400)으로 던진다.
 * - RatingService.getRatingsByMember(): 조회할 회원이 없으면 NOT_FOUND(404)로 던진다.
 * 예: new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.")
 * exception.getReason()은 생성할 때 지정한 이유인 "존재하지 않는 유저입니다."를 꺼낸다.
 * getMessage()는 상태 코드 등이 포함된 설명일 수 있으므로, 여기서는 이유 문구만 가져오는 getReason()을 사용한다.
 * 이유를 지정하지 않았다면 getReason()의 결과는 null일 수 있다.
 */
