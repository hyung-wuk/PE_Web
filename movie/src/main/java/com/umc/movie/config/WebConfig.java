// src/main/java/com/umc/movie/config/WebConfig.java
package com.umc.movie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // [1]
public class WebConfig implements WebMvcConfigurer { // [2]

    @Override
    public void addCorsMappings(CorsRegistry registry) { // [3]
        registry.addMapping("/**") // [4]
                .allowedOrigins("http://localhost:5173") // [5]
                .allowedMethods("GET", "POST", "PATCH", "DELETE") // [6]
                .allowedHeaders("*"); // [7]
    }
}

/*
 * 이 파일의 역할
 * 5173 프론트에서 8080 Spring 서버로 요청할 때 적용할 CORS 허용 정책을 설정한다.
 * 프론트가 아니라 응답을 보내는 서버 쪽에 설정하는 것이다.
 *
 * [1] @Configuration: Spring이 읽고 관리할 설정 클래스라는 표시다.
 * 이 클래스는 @SpringBootApplication의 컴포넌트 탐색 범위 안에 있으므로 자동으로 발견된다.
 *
 * [2] implements WebMvcConfigurer: Spring MVC의 웹 설정을 필요한 부분만 추가·조정하기 위한 인터페이스다.
 * 여기서는 CORS 설정 메서드만 재정의한다. 컨트롤러처럼 API를 만드는 클래스는 아니다.
 *
 * [3] addCorsMappings: Spring이 웹 설정을 구성할 때 호출하는 메서드다.
 * registry는 우리가 new로 만들거나 프론트에서 보내는 값이 아니라 Spring이 인자로 전달하는 설정 등록 객체다.
 * @Override는 인터페이스에 정의된 메서드를 재정의한다는 Java 표시다.
 * 우리가 컨트롤러나 서비스에서 이 메서드를 직접 호출할 필요는 없다.
 *
 * [4] addMapping("/**"): 이 CORS 정책을 모든 경로에 적용한다.
 * 아래 메서드들을 점으로 이어 쓰는 것은 함수를 인자로 넘기는 것이 아니라 메서드 호출을 연결하는 방식이다.
 * addMapping이 반환한 설정 객체에 allowedOrigins, allowedMethods, allowedHeaders를 차례로 호출한다.
 * 경로를 등록했다고 해당 API가 생기는 것은 아니다. 컨트롤러가 없으면 여전히 404일 수 있다.
 *
 * [5] allowedOrigins: 응답을 읽도록 허용할 프론트의 출처(Origin)를 지정한다.
 * 여기에는 요청 대상인 8080 서버가 아니라 요청을 보내는 5173 프론트 주소를 적는다.
 * Origin은 프로토콜 + 호스트 + 포트다. 127.0.0.1:5173이나 localhost:5174는 이 주소와 다른 Origin이다.
 * 허용된 교차 출처 요청에는 Spring이 Access-Control-Allow-Origin 등의 응답 헤더를 제공하고,
 * 브라우저가 이를 확인해 프론트 코드에 응답을 제공할지 판단한다.
 * 이것은 로그인·권한 검사나 외부 도구의 접근을 차단하는 방화벽 설정이 아니다.
 *
 * [6] allowedMethods: 교차 출처 요청에 허용할 HTTP 메서드 목록이다.
 * GET은 조회, POST는 생성, PATCH는 일부 수정, DELETE는 삭제에 사용한다.
 * 이 설정만으로 각 메서드의 API가 구현되지는 않는다. PUT은 현재 허용 목록에 없다.
 * 사전 요청(OPTIONS)이 필요한 경우 Spring의 CORS 처리가 실제 요청 메서드의 허용 여부를 확인한다.
 *
 * [7] allowedHeaders("*"): 교차 출처 요청에서 보낼 요청 헤더 이름을 모두 허용한다.
 * 예를 들어 Content-Type, Authorization 같은 헤더를 사용할 수 있도록 하는 설정이다.
 * 응답 헤더를 모두 공개하거나, 토큰을 자동으로 넣거나, 인증을 통과시켜 준다는 뜻은 아니다.
 *
 * 설정을 바꾼 뒤에는 Spring 서버를 재시작하고 프론트에서 다시 요청해 확인한다.
 * CORS가 해결되어도 API 경로 오류(404), DB 오류 등은 별도로 해결해야 한다.
 */
