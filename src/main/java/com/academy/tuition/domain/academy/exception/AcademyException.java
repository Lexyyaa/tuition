package com.academy.tuition.domain.academy.exception;

import com.academy.tuition.domain.exception.BusinessException;
import com.academy.tuition.domain.exception.ErrorCode;

public class AcademyException extends BusinessException {

    public AcademyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AcademyException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
