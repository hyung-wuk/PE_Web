package com.umc.movie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS) // [1]
public record ApiResponse<T>( // [2]
        boolean isSuccess,
        String code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> created(String message, T result) { // [3]
        return new ApiResponse<>(true, "COMMON201", message, result);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return new ApiResponse<>(true, "COMMON200", message, result);
    }

    public static ApiResponse<Void> failure(String code, String message) { // [4]
        return new ApiResponse<>(false, code, message, null);
    }
}

/*
 * 이 DTO는 API 응답의 공통 포장지다.
 * 기존 NicknameCheckResponse나 RatingResponse를 없애는 것이 아니라 result 안에 담아 반환한다.
 * isSuccess는 처리 성공 여부, code는 우리 서비스의 결과 코드, message는 설명, result는 실제 데이터다.
 * code의 COMMON200 같은 문자열은 실제 HTTP 상태 코드와 별개다. HTTP 상태도 컨트롤러·예외 처리기에서 맞춰야 한다.
 * 성공 응답은 컨트롤러에서 created 또는 ok로 감싸고, 실패 응답은 공통 예외 처리기가 failure로 만든다.
 * 클래스를 선언했다고 모든 응답이 자동으로 감싸지는 것은 아니다. 204 No Content는 본문 자체를 보내지 않는다.
 *
 * [1] result가 null이어도 JSON에 "result": null을 포함하도록 설정한다.
 * 성공·실패의 바깥 모양을 일정하게 유지하기 위한 Jackson 설정이다.
 *
 * [2] record는 필드·생성자·값을 읽는 메서드 등을 Java가 자동으로 제공하는 데이터용 클래스 문법이다.
 * ApiResponse<T>의 <T>는 result에 들어갈 타입을 나중에 정하기 위한 타입 변수 선언이다.
 * T result는 "실제 사용하는 곳에서 정한 타입의 데이터를 담는다"는 뜻이다.
 *
 * 정의할 때: ApiResponse<T>                        → T라는 타입 빈칸 선언
 * 사용할 때: ApiResponse<NicknameCheckResponse>     → result가 닉네임 확인 DTO
 * 사용할 때: ApiResponse<List<RatingResponse>>      → result가 평점 DTO 리스트
 * List<RatingResponse>처럼 타입 안에 다른 제네릭 타입을 넣을 수도 있다.
 *
 * [3] created와 ok는 우리가 만든 static 메서드다. ApiResponse.created(message, dto) 등으로 호출한다.
 * 매개변수를 두 개만 받고 내부 생성자에 true와 결과 코드를 직접 넣으므로 호출할 때 이 값들을 넘길 필요가 없다.
 * return new ApiResponse<>(...)는 응답 객체를 생성하고 바로 반환한다는 뜻이다.
 * <>는 생성할 객체의 타입 인자를 컴파일러가 추론하도록 하는 다이아몬드 문법이다.
 *
 * public static <T> ApiResponse<T> created(String message, T result)
 *               ①       ②                       ③
 * ① <T>는 이 메서드가 사용할 타입 변수를 선언한다. Java에서는 메서드명 뒤가 아니라 반환 타입 앞에 쓴다.
 * ② ApiResponse<T>는 반환 타입이다. T 타입의 result를 담은 ApiResponse를 반환한다.
 * ③ T result는 매개변수의 타입이다. 보통 전달한 DTO 등을 보고 컴파일러가 메서드의 T를 추론한다.
 *
 * 클래스에 이미 T가 있는데 static 메서드에도 <T>를 쓰는 이유:
 * 클래스의 T는 ApiResponse<String>, ApiResponse<Integer>처럼 객체를 사용하는 타입에 따라 달라질 수 있다.
 * 반면 static 메서드는 객체를 먼저 만들지 않고 호출하므로 특정 객체의 T를 기준으로 사용할 수 없다.
 * 그래서 static 메서드가 쓸 타입 변수를 별도로 선언한다. 두 T는 이름만 같고 서로 다른 선언이다.
 *
 * 이름을 바꿔 풀어보면 아래와 같은 역할이다(비교용 예시).
 * public static <R> ApiResponse<R> created(String message, R result) {
 *     return new ApiResponse<>(true, "COMMON201", message, result);
 * }
 *
 * 예: NicknameCheckResponse dto = new NicknameCheckResponse(true);
 * ApiResponse<NicknameCheckResponse> response = ApiResponse.ok("조회 성공", dto);
 * 전달한 dto의 타입에 맞춰 메서드의 T가 추론되고, 해당 DTO를 result에 담은 응답 객체가 반환된다.
 * static은 "객체를 전혀 만들지 않는다"는 뜻은 아니다. 호출 전에 객체가 필요 없다는 뜻이다.
 * 여기서도 메서드 내부의 new로 실제 응답 객체를 만든다.
 *
 * [4] failure는 isSuccess=false, result=null을 넣어 실패 응답을 만든다. 호출할 때는 코드와 메시지만 넘긴다.
 * ApiResponse<Void>는 이 응답에 실제 결과 데이터를 담지 않는다는 타입 표현이다.
 * Void는 제네릭 타입 자리에 쓰는 참조 타입이며, 메서드가 반환값이 없다는 뜻의 void와 다르다.
 * 이 메서드도 ApiResponse 객체를 반환하되 그 안의 result만 null이다.
 */
