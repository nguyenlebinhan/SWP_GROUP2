/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

/**
 *
 * @author ADMIN
 */
public class ClosingResult {

        private final boolean success;
        private final String message;
        private final int affected;

        private ClosingResult(boolean success, String message, int affected) {
            this.success = success;
            this.message = message;
            this.affected = affected;
        }

        public static ClosingResult ok(String message, int affected) {
            return new ClosingResult(true, message, affected);
        }

        public static ClosingResult fail(String message) {
            return new ClosingResult(false, message, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getAffected() {
            return affected;
        }
    }