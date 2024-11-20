package com.choikang.back.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {
    //200 OK

    //201 CREATED
    LOGIN_SUCCESSED(HttpStatus.OK, "로그인에 성공했습니다.");
    //202 ACCEPTED

    //204 NO RESPONSE

    private final HttpStatus httpStatus;
    private final String message;
}
