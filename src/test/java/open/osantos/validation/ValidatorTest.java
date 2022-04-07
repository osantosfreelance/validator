package open.osantos.validation;

import lombok.val;
import open.osantos.validation.exception.ValidationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorTest {

    private String node;
    private String apiName;
    private Validator validator;

    @BeforeEach
    public void setup() {
        this.node = RandomStringUtils.randomAlphabetic(5);
        this.apiName = RandomStringUtils.randomAlphabetic(5);
        this.validator = new Validator(apiName);
    }

    @Test
    public void testRequired_String() {
        validator.validate(RandomStringUtils.randomNumeric(5)).node(node).required();

        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(null).node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());

        exception = assertThrows(ValidationException.class, () -> validator.validate("").node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());

        exception = assertThrows(ValidationException.class, () -> validator.validate(" ").node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());
    }

    @Test
    public void testRequired_Numeric() {
        validator.validate(Integer.valueOf(RandomStringUtils.randomNumeric(5))).node(node).required();
        validator.validate(0).node(node).required();

        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(null).node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());
    }

    @Test
    public void testRequired_Collection() {
        validator.validate(Arrays.asList(new String[]{"foo", "bar"})).node(node).required();

        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(null).node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());

        exception = assertThrows(ValidationException.class, () -> validator.validate(Collections.EMPTY_LIST).node(node).required());
        assertEquals("Invalid Input: " + node + ". It is a mandatory field.", exception.getMessage());
    }

}
