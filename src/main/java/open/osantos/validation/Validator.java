package open.osantos.validation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import open.osantos.validation.exception.ValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility Class for doing validations for CSIHubAPI
 * This Standardises specific Validation Checks done on JSON Models
 *
 * This class is very versatile. Please feel free to contribute
 * and add other validation methods/functions if needed
 *
 * USAGE:
 * 1. Create an instance of {@link Validator}
 * 2. Use the util like so:
 *      Validator.validate(Object, String);
 * 3. Call Methods according to Validation Needs like so:
 *      Validator.validate(Object, String)
 *          .required()
 *          .mustMatchRegex(String)
 *          .mustBeOneOfTheseIntPrimitiveValues(int[]);
 *
 * NOTE:
 *  Always create a new instance of this class per Validation Call to avoid
 *  Multiple Threads using the same Validation Instance.
 *
 *  If your Validation Class a Spring Component, Instantiate this Util on Method Level
 *  If your Validation Method STATIC, Instantiate this Util on Method Level
 *  If your Validation Class is a POJO, you can do either once above or Instantiate this Util on Declaration or Constructor
 *
 * @author OlieSantos <WR10EE:Olie.Santos@ing.com> 03/03/2021
 *
 */
@Slf4j
public class Validator {

    private static final String PERIOD = ".";
    private final ValidationErrors errors = new ValidationErrors();
    private final String apiName;
    private Object validee;
    private String node;
    private boolean invalidInput = false;
    private boolean continiousValidation = false;

    public Validator(final String apiName) {
        this.apiName = apiName;
    }

    public void addException(final ValidationException exception) {
        this.errors.addException(exception);
    }

    public void throwValidationErrorsIfAny() {
        if (this.errors.hasException()) {
            log.debug(apiName + " has Validation Errors.");
            log.debug(apiName + errors);

            String exceptionString = this.errors.stringifyErrorList();
            if (this.errors.exceptionList.size() > 1)
                exceptionString = "[ " + exceptionString + " ]";

            throw new ValidationException(apiName, exceptionString, this.node);
        }
    }

    public void clearExceptions() {
        this.errors.clearExceptions();
    }

    private void throwValidationException(final StringBuilder builder) {
        if (!continiousValidation) {
            log.error("Validation Errors : {}", builder.toString());
            throw new ValidationException(apiName, builder.toString(), this.node);
        }

        this.invalidInput = true;
        addException(new ValidationException(apiName, builder.toString(), this.node));
    }

    /**
     * Use this for non-list nodes
     *
     * @param validee - Node Object]
     * @return this
     */
    public Validator validate(final Object validee) {
        this.validee = validee;
        this.invalidInput = false;
        this.continiousValidation = false;
        return this;
    }

    public Validator node(final String node) {
        this.node = node;
        return this;
    }

    public Validator idx(final int idx) {
        this.node = MessageFormat.format(node, idx);
        return this;
    }

    public Validator continousValidation() {
        this.continiousValidation = true;
        return this;
    }

    /**
     * Throws an error if this.validee is null
     *
     * @return this
     */
    public Validator required() {
        if (ObjectUtils.isEmpty(this.validee) || (validee instanceof String && StringUtils.isBlank(String.valueOf(validee)))) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("It is a mandatory field.");
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee is null/non-null depending on shouldBeNull Param
     *
     * @param shouldBeNull when true, throw exception if validee is a non-null value=
     * @param conditionalString the condition for the reference node for it to be true or false
     * @return this
     */
    public Validator mustBeNullWhen(final boolean shouldBeNull, final String conditionalString) {
        if (!ObjectUtils.isEmpty(this.validee) && shouldBeNull) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Should be null when ");
            builder.append(conditionalString);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is not of values param
     *
     * @param values list of values that validee should only have
     * @return this
     *
     * Deprecated because all enumeration values should be compared to an integer
     */
    @Deprecated
    public Validator mustBeOneOfTheseValues(Object... values) {
        if (!ObjectUtils.isEmpty(this.validee) && !ArrayUtils.contains(values, this.validee)) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Values can only be either ");
            builder.append(Arrays.toString(values));
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is not of values param
     *
     * @param values list of int values that validee should only have
     * @return this
     */
    public Validator mustBeOneOfTheseIntPrimitiveValues(int... values) {
        return mustBeOneOfTheseIntPrimitiveValues("", values);
    }

    /**
     * Throws an error if this.validee's value is not of values param
     *
     * @param codingScheme coding scheme where the list of values belongs to
     * @param values list of int values that validee should only have
     * @return this
     */
    public Validator mustBeOneOfTheseIntPrimitiveValues(final String codingScheme, final int... values) {
        if (!ObjectUtils.isEmpty(this.validee) && !ArrayUtils.contains(values, (Integer) this.validee)) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Values can only be either ");
            builder.append(Arrays.toString(values));
            builder.append(PERIOD);
            if (!StringUtils.isBlank(codingScheme)) {
                builder.append(" Please refer to ");
                builder.append(codingScheme);
                builder.append(" coding scheme.");
            }
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value does not match the given regex pattern
     *
     * @param regex regex string that validee should abide to
     * @return this
     */
    public Validator mustMatchRegex(final String regex) {
        if (!ObjectUtils.isEmpty(this.validee) && !(String.valueOf(this.validee)).matches(regex)) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must match the given regex ");
            builder.append(regex);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is not numeric
     *
     * @return this
     */
    public Validator mustBeNumberOnly() {
        if (!ObjectUtils.isEmpty(this.validee) && !NumberUtils.isDigits(String.valueOf(this.validee))) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must only contain numbers.");
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value size in characters does not equate to length param
     *
     * @param length exact size of characters that validee should have
     * @return this
     */
    public Validator mustHaveExactLength(final int length) {
        if (!ObjectUtils.isEmpty(this.validee) && String.valueOf(this.validee).length() != length) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must exactly be ");
            builder.append(length);
            builder.append(" characters long.");
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value size in characters exceeds length param
     *
     * @param length maximum size of characters that validee can have
     * @return this
     */
    public Validator mustHaveMaximumLength(final int length) {
        if (!ObjectUtils.isEmpty(this.validee) && String.valueOf(this.validee).length() > length) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must not exceed allowed ");
            builder.append(length);
            builder.append(" characters long.");
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's list size exceeds length param
     *
     * @param max maximum size of lsit items that validee can have
     * @return this
     */
    public Validator mustNotExceedMaximumListItems(final int max) {
        if (!ObjectUtils.isEmpty(this.validee) && ((Collection<?>) this.validee).size() > max) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("List items must not exceed ");
            builder.append(max);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is not of a valid email
     *
     * @return this
     */
    public Validator email() {
        EmailValidator validator = EmailValidator.getInstance();
        if (!ObjectUtils.isEmpty(this.validee) && !validator.isValid(String.valueOf(this.validee))) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Provide a valid Email Address.");
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is more than max param
     *
     * @param max maximum value that validee can have
     * @return this
     */
    public Validator mustBeLessThan(final int max) {
        if (!ObjectUtils.isEmpty(this.validee) && Integer.parseInt(String.valueOf(this.validee)) > max) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must be lesser than or equals to ");
            builder.append(max);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws an error if this.validee's value is less than min param
     *
     * @param min minimum value that validee can have
     * @return this
     */
    public Validator mustBeGreaterThan(final int min) {
        if (!ObjectUtils.isEmpty(this.validee) && Integer.parseInt(String.valueOf(this.validee)) < min) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append("Value must be greater than or equals to ");
            builder.append(min);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Throws a custom error message if condition fails.
     * NOTE: USE THIS ONLY FOR EDGE CASES NOT COVERED BY EXISTING VALIDATION METHODS IN UTIL
     *
     * @param condition conditional value that determines if validation is passed or not
     * @param errorMessage custom error message thrown
     * @return this
     */
    public Validator mustBeTrue(final boolean condition, final String errorMessage) {
        if (!condition) {
            val builder = new StringBuilder();
            builder.append(invalidPrefix());
            builder.append(errorMessage);
            builder.append(PERIOD);
            throwValidationException(builder);
        }
        return this;
    }

    /**
     * Checks list object if there are duplicate fields depending on fields param
     * @param fields names of unique fields the validee must have
     * @return this
     */
    public Validator mustNotHaveDuplicates(final String... fields) {
        if (!ObjectUtils.isEmpty(this.validee)) {

            val builder = new StringBuilder();
            val uniqueKeys = new HashSet<String>();

            ((Collection<?>) this.validee).forEach(validee -> {
                val keys = new ArrayList<String>();

                Arrays.stream(fields).forEach(fieldName -> {
                    try {
                        val field = validee.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(validee);
                        if (!ObjectUtils.isEmpty(value))
                            keys.add(String.valueOf(value));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.error(e.getMessage());
                    }
                });

                val key = keys.toString();
                if (uniqueKeys.contains(key)) {
                    builder.append(invalidPrefix());
                    builder.append("Fields : ");
                    builder.append(Arrays.toString(fields));
                    builder.append(" must be unique.");
                    throwValidationException(builder);
                }
                uniqueKeys.add(key);
            });
        }
        return this;
    }

    public void throwValidationError(final String message) {
        throwValidationException(new StringBuilder(message));
    }

    public String invalidPrefix() {
        return !this.invalidInput ? ("Invalid Input: " + node + ". ") : "";
    }

    /**
     * Main Wrapper that encapsulates Validation Error Information
     */
    @Getter
    @Setter
    private class ValidationErrors {
        private final List<ValidationException> exceptionList = new ArrayList<>();

        public void addException(final ValidationException exception) {
            this.exceptionList.add(exception);
        }

        public boolean hasException() {
            return CollectionUtils.isNotEmpty(this.exceptionList);
        }

        public String stringifyErrorList() {
            return this.exceptionList.stream().map(ValidationException::getMessage).collect(Collectors.joining(","));
        }

        public void clearExceptions() {
            this.exceptionList.clear();
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class Validee {
        private Object validee;
        private String node;
        private Integer idx;

        public Validee(Object validee) {
            this.validee = validee;
        }
    }

}
