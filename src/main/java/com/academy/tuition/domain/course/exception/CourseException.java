package com.academy.tuition.domain.course.exception;

import com.academy.tuition.domain.exception.BusinessException;
import com.academy.tuition.domain.exception.ErrorCode;

public class CourseException extends BusinessException {

    public CourseException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CourseException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
