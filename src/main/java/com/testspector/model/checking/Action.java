package com.testspector.model.checking;

public interface Action<T> {


    String getName();

    void execute(T t);
    
}
