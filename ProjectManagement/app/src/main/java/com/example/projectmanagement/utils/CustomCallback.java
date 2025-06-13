package com.example.projectmanagement.utils;

public interface CustomCallback<S, E> {
    void onSuccess(S result);

    void onError(E e);
}