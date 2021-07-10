package me.laotang.carry.mvvm.binding;

public interface Command<T> {
    void execute(T t);
}
