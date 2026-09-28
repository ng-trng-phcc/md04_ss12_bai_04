package com.example.productservice.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String m) { super(m); }
}
