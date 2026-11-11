
package com.bank_project.account.register;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @Test
    void shouldAcceptValidCpf() {
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertFalse(CpfValidator.isValid("12345678900"));
    }

    @Test
    void shouldRejectRepeatedDigits() {
        assertFalse(CpfValidator.isValid("11111111111"));
    }

    @Test
    void shouldRejectCpfWithWrongLength() {
        assertFalse(CpfValidator.isValid("123456789"));
    }

    @Test
    void shouldRejectNullCpf() {
        assertFalse(CpfValidator.isValid(null));
    }
}
