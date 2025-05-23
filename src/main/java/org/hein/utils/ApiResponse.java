package org.hein.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Rangoon")
    private Date timeStamp;
    private int statusCode;
    private String message;
    private String errorCode;
    private T payload;

    private ApiResponse(T payload, HttpStatus httpStatus, String errorCode) {
        this.timeStamp = new Date();
        this.statusCode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase().toUpperCase();
        this.errorCode = errorCode;
        this.payload = payload;
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(T data) {
        ApiResponse<T> body = new ApiResponse<>(data, HttpStatus.OK, null);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(T data, HttpStatus httpStatus, String errorCode) {
        ApiResponse<T> body = new ApiResponse<>(data, httpStatus, errorCode);
        return new ResponseEntity<>(body, httpStatus);
    }

}
