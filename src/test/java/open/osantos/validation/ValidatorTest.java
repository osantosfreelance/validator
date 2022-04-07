package open.osantos.validation;

import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class ValidatorTest {

    @Test
    public void constructor() {
        val validator = new Validator(RandomStringUtils.randomAlphabetic(10));
    }

}
