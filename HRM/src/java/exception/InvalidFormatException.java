/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception;

/**
 *
 * @author ADMIN
 */
public class InvalidFormatException extends RuntimeException {
    private final String fieldName;

    public InvalidFormatException( String message) {
        super(message);
        this.fieldName = null;
    }

    public InvalidFormatException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message,cause);
        this.fieldName = null;
    }

    public String getFieldName() {
        return fieldName;
    }
    
    
    
}
