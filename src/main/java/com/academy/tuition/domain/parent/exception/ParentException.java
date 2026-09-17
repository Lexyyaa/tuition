package com.academy.tuition.domain.parent.exception;

import com.academy.tuition.domain.exception.BusinessException;
import com.academy.tuition.domain.exception.ErrorCode;

public class ParentException extends BusinessException {

    public ParentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ParentException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
