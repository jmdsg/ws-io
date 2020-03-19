package com.fiberg.wsio.auto;

import com.fiberg.wsio.annotation.WsIOMessageWrapper;

import javax.annotation.Nonnull;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

@WsIOMessageWrapper
@WebService(name = "Jiji")
public class A {
    @Nonnull
    @WebResult
    @WebMethod(operationName = "jeje")
    public B getB() {
        return new B();
    }
}
