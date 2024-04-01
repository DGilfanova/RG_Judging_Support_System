package ru.itis.rgjudge.exception;

public class ElementNotFoundException extends RuntimeException {

    public ElementNotFoundException(Integer elementId) {
        super(String.format("Element with id = %s not found", elementId));
    }
}
