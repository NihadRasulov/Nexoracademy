package az.demo.NexoraAcademy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;

public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    private String startField;
    private String endField;
    private boolean inclusive;

    @Override
    public void initialize(DateRange annotation) {
        this.startField = annotation.startField();
        this.endField = annotation.endField();
        this.inclusive = annotation.inclusive();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Object start = accessor(value, startField);
            Object end = accessor(value, endField);
            if (start == null || end == null || !(start instanceof Comparable)) {
                return true;
            }
            int cmp = ((Comparable) start).compareTo(end);
            return inclusive ? cmp <= 0 : cmp < 0;
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    private Object accessor(Object target, String fieldName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(fieldName);
        return method.invoke(target);
    }
}
