package com.lnnktrn.timetravel_java.exception;

public class NoSuchRecordException extends RuntimeException {

    public NoSuchRecordException(Long id) {
        super("Record with id=" + id + " does not exist");
    }

    public NoSuchRecordException(Long id, Long version) {
        super("Record with id=" + id + " and version=" + version + " does not exist");
    }

    public NoSuchRecordException(String message) {
        super(message);
    }
}