package com.hbm.interfaces;

public interface IOrderedEnum<T extends Enum<T>> {
    T[] getOrder();
}