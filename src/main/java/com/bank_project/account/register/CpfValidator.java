
package com.bank_project.account.register;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {

        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(cpf, 9);
        int secondDigit = calculateDigit(cpf, 10);

        return firstDigit == Character.getNumericValue(cpf.charAt(9))
                && secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateDigit(String cpf, int length) {

        int sum = 0;

        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i))
                    * (length + 1 - i);
        }

        int remainder = (sum * 10) % 11;

        return remainder == 10 ? 0 : remainder;
    }
}
