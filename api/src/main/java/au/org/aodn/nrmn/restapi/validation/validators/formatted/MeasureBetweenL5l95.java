package au.org.aodn.nrmn.restapi.validation.validators.formatted;

import au.org.aodn.nrmn.restapi.model.db.StagedRowError;
import au.org.aodn.nrmn.restapi.model.db.composedID.ErrorID;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationCategory;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationLevel;
import au.org.aodn.nrmn.restapi.validation.BaseFormattedValidator;
import au.org.aodn.nrmn.restapi.validation.StagedRowFormatted;
import cyclops.control.Validated;
import lombok.val;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MeasureBetweenL5l95  extends BaseFormattedValidator {
    public MeasureBetweenL5l95() {
        super("measure");
    }

    @Override
    public Validated<StagedRowError, String> valid(StagedRowFormatted target) {
        val methodAllowed = Arrays.asList(0,1,2);
        if (!methodAllowed.contains(target.getMethod()) ||
                !target.getRef().getStagedJob().getIsExtendedSize()) {
            return Validated.valid("not affected");
        }

        val l5 = target.getSpeciesAttributes().getL5();
        val l95 =   target.getSpeciesAttributes().getL95();
        val outOfRange = target.getMeasureJson()
                 .entrySet().stream()
                 .filter(entry -> entry.getValue() > l5 && entry.getValue() < l95)
                 .map(Map.Entry::getKey).collect(Collectors.toList());

        if (outOfRange.isEmpty()){
            return Validated.valid("Measure in l5/l95 range");
        }

        val keysStr = outOfRange.stream().map(Object::toString)
                .reduce("", (acc, value) -> acc + "|" + value);
        return Validated.invalid(
                new StagedRowError(
                        new ErrorID(target.getId(),
                                target.getRef().getStagedJob().getId(),
                                target.getRef().getSpecies() + " is superseeded"),
                        ValidationCategory.DATA,
                        ValidationLevel.WARNING,
                        columnTarget + ":" + keysStr,
                        target.getRef())
        );
    }
}
