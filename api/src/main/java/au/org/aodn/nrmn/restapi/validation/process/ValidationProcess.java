package au.org.aodn.nrmn.restapi.validation.process;

import static au.org.aodn.nrmn.restapi.util.SpacialUtil.getDistance;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.org.aodn.nrmn.restapi.controller.mapping.StagedRowFormattedMapperConfig;
import au.org.aodn.nrmn.restapi.dto.stage.ValidationCell;
import au.org.aodn.nrmn.restapi.dto.stage.ValidationError;
import au.org.aodn.nrmn.restapi.dto.stage.ValidationResponse;
import au.org.aodn.nrmn.restapi.dto.stage.ValidationRow;
import au.org.aodn.nrmn.restapi.model.db.Diver;
import au.org.aodn.nrmn.restapi.model.db.ObservableItem;
import au.org.aodn.nrmn.restapi.model.db.Site;
import au.org.aodn.nrmn.restapi.model.db.StagedJob;
import au.org.aodn.nrmn.restapi.model.db.StagedRow;
import au.org.aodn.nrmn.restapi.model.db.Survey;
import au.org.aodn.nrmn.restapi.model.db.UiSpeciesAttributes;
import au.org.aodn.nrmn.restapi.model.db.enums.Directions;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationCategory;
import au.org.aodn.nrmn.restapi.model.db.enums.ValidationLevel;
import au.org.aodn.nrmn.restapi.repository.DiverRepository;
import au.org.aodn.nrmn.restapi.repository.ObservableItemRepository;
import au.org.aodn.nrmn.restapi.repository.ObservationRepository;
import au.org.aodn.nrmn.restapi.repository.SiteRepository;
import au.org.aodn.nrmn.restapi.repository.StagedRowRepository;
import au.org.aodn.nrmn.restapi.repository.SurveyRepository;
import au.org.aodn.nrmn.restapi.util.TimeUtils;
import au.org.aodn.nrmn.restapi.validation.StagedRowFormatted;

@Component
public class ValidationProcess {

    @Autowired
    StagedRowRepository rowRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    DiverRepository diverRepository;

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    ObservationRepository observationRepository;

    @Autowired
    ObservableItemRepository observableItemRepository;

    private static final int INVALID_INT = Integer.MIN_VALUE;
    private static final double INVALID_DOUBLE = Double.NEGATIVE_INFINITY;
    private static final int OBS_ITEM_TYPE_NO_SPECIES_FOUND = 6;
    private static final Integer[] METHODS_TO_CHECK = { 0, 1, 2, 7, 10 };
    private static final Pattern VALID_DEPTH_SURVEY_NUM = Pattern.compile("^[0-9]+(\\.[0-9])?$");
    private static final LocalDate DATE_MIN_RLS = LocalDate.parse("2006-01-01");
    private static final LocalDate DATE_MIN_ATRC = LocalDate.parse("1992-01-01");

    private static final double[] FISH_VALUES = { 2.5, 5, 7.5, 10, 12.5, 15, 20, 25, 30, 35, 40, 50, 62.5, 75, 87.5, 100, 112.5, 125, 137.5, 150, 162.5, 175, 187.5, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000 };
    private static final double[] INVERT_VALUES = { 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 10.5, 11, 11.5, 12, 12.5, 13, 13.5, 14, 14.5, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30 };

    private String hashRow(StagedRow r) {
        return r.getDepth() + r.getDiver() + r.getSiteCode() + r.getBlock() + r.getMethod() + r.getSpecies() + r.getDate() + r.getTime();
    }

    // VALIDATION: Rows duplicated
    private Collection<ValidationRow> checkDuplicateRows(Collection<StagedRow> rows) {
        Map<String, List<Long>> mappedRows = new HashMap<String, List<Long>>();
        rows.stream().forEach(r -> {
            String rowHash = hashRow(r);
            List<Long> rowIds = mappedRows.getOrDefault(rowHash, new ArrayList<Long>());
            rowIds.add(r.getId());
            mappedRows.put(rowHash, rowIds);
        });
        Collection<ValidationRow> duplicateRows = new ArrayList<ValidationRow>();
        mappedRows.forEach((r, v) -> {
            if (v.size() > 1)
                duplicateRows.add(new ValidationRow(r, v, ValidationLevel.WARNING, "Rows duplicated"));
        });
        return duplicateRows;
    }

    public Collection<ValidationError> checkFormatting(String programName, Boolean isExtendedSize, Collection<String> siteCodes, Collection<ObservableItem> species, Collection<StagedRow> rows) {

        Collection<String> diverNames = new ArrayList<String>();
        for (Diver d : diverRepository.getAll()) {
            diverNames.add(d.getFullName().toUpperCase());
            diverNames.add(d.getInitials().toUpperCase());
        }

        ValidationResultSet errors = new ValidationResultSet();

        errors.addGlobal(checkDuplicateRows(rows));

        for (StagedRow row : rows) {

            Long rowId = row.getId();

            // Site
            if (!siteCodes.contains(row.getSiteCode()))
                errors.add(rowId, ValidationLevel.BLOCKING, "siteCode", "Site Code does not exist");

            // Diver
            if (row.getDiver() == null || !diverNames.contains(row.getDiver().toUpperCase()))
                errors.add(rowId, ValidationLevel.BLOCKING, "diver", "Diver does not exist");

            // Buddies
            List<String> unknownBuddies = new ArrayList<String>();
            if (row.getBuddy() != null) {
                for(String buddy : row.getBuddy().split(",")) {
                    if(!diverNames.contains(buddy.trim().toUpperCase()))
                        unknownBuddies.add(buddy.trim());
                }
            }

            if(row.getBuddy() == null || row.getBuddy().trim() == "") {
                errors.add(rowId, ValidationLevel.WARNING, "buddy", "Diver does not exist");
            } else if(unknownBuddies.size() == 1) {
                errors.add(rowId, ValidationLevel.WARNING, "buddy", "Diver " + unknownBuddies.get(0) + " does not exist");
            } else if(unknownBuddies.size() > 1) {
                errors.add(rowId, ValidationLevel.WARNING, "buddy", "Divers " + String.join(", ", unknownBuddies) + " do not exist");
            }

            if (row.getPqs() == null || !diverNames.contains(row.getPqs().toUpperCase()))
                errors.add(rowId, ValidationLevel.WARNING, "p-qs", "Diver does not exist");

            // VALIDATION: Species are not superseded
            if (row.getSpecies() != null && !row.getSpecies().equalsIgnoreCase("survey not done")) {
                Optional<ObservableItem> observableItem = species.stream().filter(s -> row.getSpecies().equalsIgnoreCase(s.getObservableItemName())).findAny();
                if (observableItem.isPresent()) {
                    String supersededBy = observableItem.get().getSupersededBy();
                    if (supersededBy != null)
                        errors.add(rowId, ValidationLevel.WARNING, "species", "Superseded by " + supersededBy);
                } else {
                    errors.add(rowId, ValidationLevel.BLOCKING, "species", "Species does not exist");
                }
            }

            // Direction
            if (row.getDirection() != null && !EnumUtils.isValidEnum(Directions.class, row.getDirection().toUpperCase()) && !row.getDirection().equalsIgnoreCase("0") && !row.getDirection().equalsIgnoreCase(""))
                errors.add(rowId, ValidationLevel.BLOCKING, "direction", "Direction is not valid");

            // Latitude
            Double latitude = NumberUtils.toDouble(row.getLatitude(), INVALID_DOUBLE);
            if (latitude < -90.0 || 90.0 < latitude || latitude == INVALID_DOUBLE)
                errors.add(rowId, ValidationLevel.BLOCKING, "latitude",
                        (latitude == INVALID_DOUBLE) ? "Latitude is not number" : "Latitude is out of bounds");

            // Longitude
            Double longitude = NumberUtils.toDouble(row.getLongitude(), INVALID_DOUBLE);
            if (longitude < -180 || 180 < longitude || longitude == INVALID_DOUBLE)
                errors.add(rowId, ValidationLevel.BLOCKING, "longitude",
                        (latitude == INVALID_DOUBLE) ? "Longitude is not number" : "Longitude is out of bounds");

            // Date
            try {
                DateUtils.parseDateStrictly(row.getDate(), "d/M/y");
            } catch (ParseException e) {
                errors.add(rowId, ValidationLevel.BLOCKING, "date", "Date format is not valid");
            }

            // Time
            if (row.getTime() == null || !TimeUtils.parseTime(row.getTime()).isPresent())
                errors.add(rowId, ValidationLevel.WARNING, "time", "Time format is not valid");

            // Block
            if (!Arrays.asList(0, 1, 2).contains(NumberUtils.toInt(row.getBlock(), INVALID_INT))) {
                errors.add(rowId, ValidationLevel.BLOCKING, "block", "Block must be 0, 1 or 2");
            }

            // Vis
            if (!StringUtils.isBlank(row.getVis())) {
                Double vis = NumberUtils.toDouble(row.getVis(), (double)INVALID_INT);
                if (vis < 0) {
                    errors.add(rowId, ValidationLevel.BLOCKING, "vis", (vis == (double)INVALID_INT) ? "Vis is not a decimal" : "Vis is not positive");
                } else {
                    if(vis.toString().split("\\.")[1].length() > 1)
                        errors.add(rowId, ValidationLevel.BLOCKING, "vis", "Vis is more than one decimal place");
                }
            }

            // Inverts
            if (!StringUtils.isBlank(row.getInverts())) {
                int inverts = NumberUtils.toInt(row.getInverts(), INVALID_INT);
                if (inverts == INVALID_INT)
                    errors.add(rowId, ValidationLevel.BLOCKING, "inverts", "Inverts is not an integer");
            }

            // Total
            if (NumberUtils.toInt(row.getTotal(), INVALID_INT) == INVALID_INT)
                errors.add(rowId, ValidationLevel.BLOCKING, "total", "Total is not an integer");

            // Method
            if (NumberUtils.toInt(row.getMethod(), INVALID_INT) == INVALID_INT)
            errors.add(rowId, ValidationLevel.BLOCKING, "method", "Method is not an integer");

            // MeasureJson
            if (row.getMeasureJson() != null)
                row.getMeasureJson().entrySet().stream().forEach(measure -> {
                    if (!StringUtils.isBlank(measure.getValue()) && NumberUtils.toInt(measure.getValue(), INVALID_INT) < 0)
                        errors.add(rowId, ValidationLevel.BLOCKING, measure.getKey().toString(), "Not a valid measurement");
                });

            // Depth
            if (row.getDepth() == null || StringUtils.isBlank(row.getDepth()) || !VALID_DEPTH_SURVEY_NUM.matcher(row.getDepth()).matches())
                errors.add(rowId, ValidationLevel.BLOCKING, "depth", "Depth is invalid, expected: depth[.surveyNum]");

            // RLS Method
            if (programName.equalsIgnoreCase("RLS") && !Arrays.asList(0, 1, 2, 10).contains(NumberUtils.toInt(row.getMethod(), INVALID_INT)))
                errors.add(rowId, ValidationLevel.BLOCKING, "method", "RLS Method must be 0, 1, 2 or 10");

            // Block 0
            if (row.getBlock().equalsIgnoreCase("0") && !Arrays.asList(0, 3, 4, 5).contains(NumberUtils.toInt(row.getMethod(), INVALID_INT)))
                errors.add(rowId, ValidationLevel.BLOCKING, "block", "Block 0 is invalid for method");

            // ATRC Method
            if (programName.equalsIgnoreCase("ATRC")) {
                if (!Arrays.asList(0, 1, 2, 3, 4, 5, 7, 10).contains(NumberUtils.toInt(row.getMethod(), INVALID_INT)))
                    errors.add(rowId, ValidationLevel.BLOCKING, "method", "ATRC Method must be [0-5], 7 or 10");

                if (NumberUtils.toInt(row.getMethod()) == 7 && NumberUtils.toInt(row.getBlock()) != 2)
                    errors.add(rowId, ValidationLevel.BLOCKING, "method", "ATRC Method 7 must be recorded on block 2");
            }

            // Validation: Species Invert Sizing
            if (isExtendedSize && !StringUtils.isBlank(row.getIsInvertSizing()) &&
                    !(row.getIsInvertSizing().equalsIgnoreCase("YES") || row.getIsInvertSizing().equalsIgnoreCase("NO")))
                errors.add(rowId, ValidationLevel.BLOCKING, "isInvertSizing", "Must be 'Yes' or 'No'");

        }

        return errors.getAll();
    }

    private boolean validateMeasure(Boolean isExtended, StagedRowFormatted row) {
        if (Arrays.asList(3, 4, 5).contains(row.getMethod()) || !row.getSpeciesAttributesOpt().isPresent())
            return false;

        return !row.getMeasureJson().isEmpty();
    }

    // VALIDATION: Species size within L5 - L95
    private Collection<ValidationCell> validateMeasureRange(Boolean isExtended, StagedRowFormatted row, UiSpeciesAttributes speciesAttributes) {

        Collection<ValidationCell> errors = new ArrayList<ValidationCell>();

        boolean isInvertSized = isExtended && row.getIsInvertSizing();
        double[] range = isInvertSized ? INVERT_VALUES : FISH_VALUES;

        Double l5 = speciesAttributes.getL5() != null ? speciesAttributes.getL5() : 0;
        Double l95 = speciesAttributes.getL95() != null ? speciesAttributes.getL95() : 0;

        if (l5 != 0 && l95 != 0) {
            List<Integer> outOfRange = row.getMeasureJson().entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != 0 && (l5 > 0 && range[entry.getKey() - 1] < l5) || (l95 > 0 && range[entry.getKey() - 1] > l95))
                    .map(Map.Entry::getKey).collect(Collectors.toList());

            if (!outOfRange.isEmpty()) {
                String message = "Measurements outside L5/95 [" + l5 + "," + l95 + "] for [" + row.getRef().getSpecies() + "]";
                outOfRange.stream().forEach(col -> errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, message, row.getId(), col.toString())));
            }
        }
        return errors;
    }

    // VALIDATION: Species size below LMax
    private Collection<ValidationCell> validateMeasureUnderMax(Boolean isExtended, StagedRowFormatted row, UiSpeciesAttributes speciesAttributes) {

        Collection<ValidationCell> errors = new ArrayList<ValidationCell>();

        boolean isInvertSized = isExtended && row.getIsInvertSizing();
        double[] range = isInvertSized ? INVERT_VALUES : FISH_VALUES;

        Double lMax = speciesAttributes.getLmax() != null ? speciesAttributes.getLmax() : 0;
        if (lMax != 0) {

            List<Integer> outOfRange = row.getMeasureJson().entrySet().stream()
                    .filter(entry -> range[entry.getKey() - 1] > lMax)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!outOfRange.isEmpty()) {
                String message = "Measurement is above Lmax [" + lMax + "] for Species [" + row.getRef().getSpecies() + "]";
                outOfRange.stream().forEach(col -> errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, message, row.getId(), col.toString())));
            }
        }
        return errors;
    }

    // VALIDATION: Species Abundance Check
    private Collection<ValidationError> validateAbundance(StagedRowFormatted row, UiSpeciesAttributes speciesAttributes) {
        ValidationResultSet errors = new ValidationResultSet();
        if (Arrays.asList(1, 2).contains(row.getMethod()) && speciesAttributes != null) {
            Long maxAbundance = speciesAttributes.getMaxAbundance();
            if (maxAbundance != null && row.getTotal() != null && maxAbundance < row.getTotal())
                errors.add(row.getId(), ValidationLevel.WARNING, "total", "Exceeds max abundance " + maxAbundance + " for species " + row.getRef().getSpecies() + "");
        }
        return errors.getAll();
    }

    public Collection<ValidationCell> validateMeasurements(String programName, StagedRowFormatted row) {
        Collection<ValidationCell> errors = new ArrayList<ValidationCell>();

        if (row.getMeasureJson() == null)
            return errors;

        Integer observationTotal = row.getMeasureJson().entrySet().stream().map(Map.Entry::getValue).reduce(0, Integer::sum) + (row.getInverts() != null ? row.getInverts() : 0);

        // VALIDATION: RLS: Debris Zero observations
        if (programName.equalsIgnoreCase("RLS") && row.getCode().equalsIgnoreCase("dez") && row.getSpecies().isPresent()) {
            boolean notZero = ((row.getInverts() != null && row.getInverts() != 0) || (row.getTotal() != null && row.getTotal() != 0) || observationTotal != 0);
            boolean notOne = ((row.getInverts() != null && row.getInverts() != 1) || (row.getTotal() != null && row.getTotal() != 1) || observationTotal != 1);
            if (notZero && notOne)
                errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.BLOCKING, "Debris has Value/Total/Inverts not 0 or 1", row.getId(), "total"));
        }

        if (row.getTotal() != null && !row.getTotal().equals(observationTotal))
            errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Calculated total is " + observationTotal, row.getId(), "total"));

        // VALIDATION: Record has no data and but not flagged as 'Survey Not Done' or
        // 'No Species Found'
        if (observationTotal < 1 && row.getCode() != null && !row.getCode().equalsIgnoreCase("DEZ") && !row.getCode().equalsIgnoreCase("SND") && !(row.getSpecies().isPresent() &&  row.getSpecies().get().getObsItemType() != null  && row.getSpecies().get().getObsItemType().getObsItemTypeId() == OBS_ITEM_TYPE_NO_SPECIES_FOUND))
            errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Record has no data and but not flagged as 'Survey Not Done' or 'No Species Found'", row.getId(), "total"));
        else if (row.getTotal() != null && (observationTotal + row.getTotal() > 0) && row.getSpecies().isPresent() && row.getSpecies().get().getObsItemType() != null && row.getSpecies().get().getObsItemType().getObsItemTypeId() == OBS_ITEM_TYPE_NO_SPECIES_FOUND)
            errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Record is 'No Species Found' but has nonzero total", row.getId(), "total"));
        else if (row.getTotal() != null && (observationTotal + row.getTotal() > 0) && row.getCode().equalsIgnoreCase("SND"))
            errors.add(new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Record is 'Survey Not Done' but has nonzero total", row.getId(), "total"));

        return errors;
    }

    private ValidationCell validateSpeciesBelowToMethod(StagedRowFormatted row) {

        if (row.getSpecies().isPresent()) {
            Set<Integer> methodIds = row.getSpecies().get().getMethods().stream().map(m -> m.getMethodId()).collect(Collectors.toSet());

            if (!methodIds.contains(row.getMethod()))
                return new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Method " + row.getMethod() + " invalid for species " + row.getSpecies().get().getObservableItemName(), row.getId(), "method");

        }
        return null;
    }

    private ValidationError validateSurveyIsNew(StagedRowFormatted row) {
        if (Arrays.asList(METHODS_TO_CHECK).contains(row.getMethod())) {

            // FIXME:
            List<Survey> existingSurveys = surveyRepository.findBySiteDepthSurveyNumDate(row.getSite(), row.getDepth(), row.getSurveyNum(), Date.from(row.getDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

            if (!existingSurveys.isEmpty()) {
                Survey existingSurvey = existingSurveys.stream().findFirst().get();
                String message = "Survey " + existingSurvey.getSurveyId() + " includes [" + row.getSite().getSiteCode() + ", " + row.getDate() + ", " + row.getDepth() + "]" + row.getMethod();
                return new ValidationError(ValidationCategory.DATA, ValidationLevel.BLOCKING, message, Arrays.asList(row.getId()), Arrays.asList("siteCode"));
            }

        }
        return null;
    }

    // VALIDATION: Survey coordinates match with DB
    private Collection<ValidationCell> validateWithin200M(StagedRowFormatted row) {
        Collection<ValidationCell> errors = new ArrayList<ValidationCell>();

        if(row.getSite() == null || row.getSite().getLatitude() == null || row.getSite().getLongitude() == null ||  row.getLatitude() == null || row.getLongitude() == null)
            return errors;

        double dist = getDistance(row.getSite().getLatitude(), row.getSite().getLongitude(), row.getLatitude(), row.getLongitude());
        if (dist > 0.2) {
            String message = "Coordinates are further than 0.2km from the Site (" + String.format("%.2f", dist) + "km)";
            errors.add(new ValidationCell(ValidationCategory.SPAN, ValidationLevel.WARNING, message, row.getId(), "latitude"));
            errors.add(new ValidationCell(ValidationCategory.SPAN, ValidationLevel.WARNING, message, row.getId(), "longitude"));
        }
        return errors;
    }

    private ValidationCell validateDateRange(LocalDate earliest, StagedRowFormatted row) {

        if(row.getDate() == null)
            return null;
            
        // Validation: Surveys Too Old
        if (row.getDate().isAfter(LocalDate.from(ZonedDateTime.now())))
            return new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Date is in the future", row.getId(), "date");

        // Validation: Future Survey Rule
        if (row.getDate().isBefore(earliest))
            return new ValidationCell(ValidationCategory.DATA, ValidationLevel.WARNING, "Date must be after " + earliest.toString(), row.getId(), "date");

        return null;
    }

    private ValidationError validateMethod3Quadrats(String transect, List<StagedRowFormatted> rows) {

        Set<String> columnNames = new HashSet<String>();
        Set<Long> rowIds = new HashSet<Long>();

        for (int measureIndex : Arrays.asList(1, 2, 3, 4, 5))
            if (rows.stream().mapToInt(row -> row.getMeasureJson().getOrDefault(measureIndex, 0)).sum() == 0) {
                rowIds.addAll(rows.stream().map(r -> r.getId()).collect(Collectors.toList()));
                columnNames.add(Integer.toString(measureIndex));
            }

        return rowIds.size() > 0 ? new ValidationError(ValidationCategory.SPAN, ValidationLevel.BLOCKING, "Missing quadrats in transect " + transect, rowIds, columnNames) : null;
    }

    private ValidationError validateMethod3QuadratsGT50(String transect, List<StagedRowFormatted> rows) {

        Set<String> columnNames = new HashSet<String>();

        for (int measureIndex : Arrays.asList(1, 2, 3, 4, 5))
            if (rows.stream().mapToInt(row -> row.getMeasureJson().getOrDefault(measureIndex, 0)).sum() < 50) {
                columnNames.add(Integer.toString(measureIndex));
            }
        List<Long> rowIds = rows.stream().map(r -> r.getId()).collect(Collectors.toList());
        return columnNames.size() > 0 ? new ValidationError(ValidationCategory.SPAN, ValidationLevel.BLOCKING, "Quadrats do not sum to at least 50 in transect " + transect, rowIds, columnNames) : null;
    }

    private ValidationError validateSurveyTransectNumber(String surveyKey, List<StagedRowFormatted> surveyRows) {
        List<StagedRowFormatted> invalidTransectRows = surveyRows.stream().filter(r -> !Arrays.asList(1, 2, 3, 4).contains(r.getSurveyNum())).collect(Collectors.toList());
        if (invalidTransectRows.size() > 0)
            return new ValidationError(ValidationCategory.SPAN, ValidationLevel.BLOCKING, "Survey group transect invalid", invalidTransectRows.stream().map(r -> r.getId()).collect(Collectors.toList()), Arrays.asList("depth"));
        return null;
    }

    private ValidationError validateSurveyComplete(String programName, String surveyKey, List<StagedRowFormatted> surveyRows) {

        if(surveyRows.stream().anyMatch(r -> r.getMethod() == null || r.getBlock() == null))
            return null;

        Map<Integer, List<StagedRowFormatted>> surveyByMethod = surveyRows.stream().filter(sr -> sr.getMethod() != null && sr.getBlock() != null)
                                                                .collect(Collectors.groupingBy(StagedRowFormatted::getMethod));
                                                                
        // VALIDATE: If method = 0 then Block should be 0, 1 or 2
        List<StagedRowFormatted> method0Rows = surveyByMethod.get(0);
        if(method0Rows != null && method0Rows.stream().anyMatch(r -> !Arrays.asList(0, 1, 2).contains(r.getBlock())))
            return new ValidationError(ValidationCategory.SPAN, ValidationLevel.WARNING, "Method 0 must have block 0, 1 or 2",
                        method0Rows.stream().map(r -> r.getId()).collect(Collectors.toList()), Arrays.asList("block"));

        // VALIDATE: Both M1 and M2 present except if ATRC and has at least one method of 3,4,5,7
        List<Integer> methods =  new ArrayList<Integer>(Arrays.asList(1,2));
        methods.removeAll(surveyByMethod.keySet());
        if(methods.size() > 0 && !(programName.equalsIgnoreCase("ATRC") && surveyRows.stream().filter(r -> Arrays.asList(3, 4, 5, 7).contains(r.getMethod())).count() > 0)) {
            List<String> missingMethods = methods.stream().map(m -> m.toString()).collect(Collectors.toList());
            return new ValidationError(ValidationCategory.SPAN, ValidationLevel.WARNING, "Survey incomplete: missing " + String.join(", ", missingMethods), 
                                        surveyRows.stream().map(r -> r.getId()).collect(Collectors.toList()), Arrays.asList("method"));
        }

        // VALIDATE: Each method has block 1,2 (RLS) or block 1 (ATRC)
        for (Integer method : Arrays.asList(1,2)) {
            List<StagedRowFormatted> methodRows = surveyByMethod.get(method);
            if(methodRows == null) continue;

            List<Integer> blocksRequired = programName.equalsIgnoreCase("RLS") ? new ArrayList<Integer>(Arrays.asList(1,2)) : new ArrayList<Integer>(Arrays.asList(1));

            List<Integer> hasBlocks = methodRows.stream().map(r -> r.getBlock()).distinct().filter(b -> b != 0).collect(Collectors.toList());
            List<Integer> missingBlocks = blocksRequired.stream().filter(b -> !hasBlocks.contains(b)).collect(Collectors.toList());
            
            if(missingBlocks.size() > 0){
                List<String> missingBlocksMessage = missingBlocks.stream().map(m -> m.toString()).collect(Collectors.toList());
                String missingMessage = missingBlocks.size() > 1 ? "blocks " + String.join(", ", missingBlocksMessage) : "block " + missingBlocksMessage.get(0);
                String message = "Survey incomplete: method " + method + " is missing " + missingMessage;
                return new ValidationError(ValidationCategory.SPAN, ValidationLevel.WARNING, message, 
                methodRows.stream().map(r -> r.getId()).collect(Collectors.toList()), Arrays.asList("block"));
            }
        }

        return null;
    }

    private ValidationError validateSurveyGroup(String surveyKey, List<StagedRowFormatted> surveyRows) {
        Map<Integer, List<StagedRowFormatted>> surveyGroup = surveyRows.stream().collect(Collectors.groupingBy(StagedRowFormatted::getSurveyNum));
        if(!surveyGroup.keySet().containsAll(Arrays.asList(1, 2, 3, 4))) {
            List<Integer> missingSurveys = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4));
            missingSurveys.removeAll(surveyGroup.keySet());
            List<String> missingSurveysMessage = missingSurveys.stream().map(s -> s.toString()).collect(Collectors.toList());
            String message =  "Survey group incomplete: missing " + String.join(", ", missingSurveysMessage);
            return new ValidationError(ValidationCategory.SPAN, ValidationLevel.BLOCKING, message, surveyRows.stream().map(r -> r.getId()).collect(Collectors.toList()), Arrays.asList("depth"));
        }
        return null;
    }

    public Collection<ValidationError> checkSurveys(String programName, Boolean isExtended, Map<String, List<StagedRowFormatted>> surveyMap) {
        Set<ValidationError> res = new HashSet<ValidationError>();
 
        for (Map.Entry<String, List<StagedRowFormatted>> survey : surveyMap.entrySet()) {
            List<StagedRowFormatted> surveyRows = survey.getValue();
            
            if (programName.equalsIgnoreCase("ATRC")){
                // VALIDATE: Survey group transect number valid
                res.add(validateSurveyTransectNumber(survey.getKey(), surveyRows));

                // VALIDATE: Survey Group Complete
                res.add(validateSurveyGroup(survey.getKey(), surveyRows));
            }

            // VALIDATE: Survey Complete
            res.add(validateSurveyComplete(programName, survey.getKey(), surveyRows));

            // VALIDATE: Is Existing Survey
            res.add(validateSurveyIsNew(surveyRows.get(0)));

        }

        res.remove(null);

        return res;
    }

    public Collection<ValidationError> checkMethod3Transects(String programName, Boolean isExtended, Map<String, List<StagedRowFormatted>> method3SurveyMap) {
        Set<ValidationError> res = new HashSet<ValidationError>();

        // Validate M3 transects
        for (String transectName : method3SurveyMap.keySet()) {
            res.add(validateMethod3Quadrats(transectName, method3SurveyMap.get(transectName)));
            res.add(validateMethod3QuadratsGT50(transectName, method3SurveyMap.get(transectName)));
        }

        res.remove(null);

        return res;
    }

    public Collection<ValidationError> checkData(String programName, Boolean isExtended, Collection<StagedRowFormatted> rows) {

        Set<ValidationError> res = new HashSet<ValidationError>();

        ValidationResultSet results = new ValidationResultSet();

        /** Row-level Checks */
        for (StagedRowFormatted row : rows) {

            if (row.getSpeciesAttributesOpt().isPresent()) {
                UiSpeciesAttributes speciesAttributes = row.getSpeciesAttributesOpt().get();

                // Measure l5, l95 and lMax
                if (validateMeasure(isExtended, row)) {
                    results.addAll(validateMeasureRange(isExtended, row, speciesAttributes), false);
                    results.addAll(validateMeasureUnderMax(isExtended, row, speciesAttributes), false);
                }

                // Abundance check
                res.addAll(validateAbundance(row, speciesAttributes));
            }

            // Total Checksum & Missing Data
            results.addAll(validateMeasurements(programName, row), false);

            // Row Method is valid for species
            results.add(validateSpeciesBelowToMethod(row), false);

            // Validate within 200M
            results.addAll(validateWithin200M(row), false);

            // Date is not in the future or too far in the past
            results.add(validateDateRange(programName.equalsIgnoreCase("RLS") ? DATE_MIN_RLS : DATE_MIN_ATRC, row), false);
        }

        res.addAll(results.getAll());
        res.remove(null);

        return res;
    }

    public Collection<ObservableItem> getSpeciesForRows(Collection<StagedRow> rows) {
        Collection<String> enteredSpeciesNames = rows.stream().map(s -> s.getSpecies()).collect(Collectors.toSet());
        return observableItemRepository.getAllSpeciesNamesMatching(enteredSpeciesNames);
    }

    public Collection<StagedRowFormatted> formatRowsWithSpecies(Collection<StagedRow> rows, Collection<ObservableItem> species) {
        Map<Long, StagedRow> rowMap = rows.stream().collect(Collectors.toMap(StagedRow::getId, r -> r));
        List<Integer> speciesIds = species.stream().map(s -> s.getObservableItemId()).collect(Collectors.toList());
        Map<String, UiSpeciesAttributes> speciesAttributesMap = observationRepository.getSpeciesAttributesByIds(speciesIds).stream().collect(Collectors.toMap(UiSpeciesAttributes::getSpeciesName, a -> a));
        Map<String, ObservableItem> speciesMap = species.stream().collect(Collectors.toMap(ObservableItem::getObservableItemName, o -> o));
        Collection<Diver> divers = diverRepository.getAll().stream().collect(Collectors.toList());
        Collection<Site> sites = siteRepository.getAll().stream().collect(Collectors.toList());
        
        StagedRowFormattedMapperConfig mapperConfig = new StagedRowFormattedMapperConfig();
        ModelMapper mapper = mapperConfig.getModelMapper(speciesMap, rowMap, speciesAttributesMap, divers, sites);
        return rows.stream().map(stagedRow -> mapper.map(stagedRow, StagedRowFormatted.class)).collect(Collectors.toList());
    }

    public ValidationResponse process(StagedJob job) {
        ValidationResponse response = new ValidationResponse();

        Collection<StagedRow> rows = rowRepository.findRowsByJobId(job.getId());
        Collection<ObservableItem> species = getSpeciesForRows(rows);

        String programName = job.getProgram().getProgramName();
        Collection<String> enteredSiteCodes = rows.stream().map(s -> s.getSiteCode()).collect(Collectors.toSet());
        Collection<String> siteCodes = siteRepository.getAllSiteCodesMatching(enteredSiteCodes);
        Collection<ValidationError> sheetErrors = new HashSet<ValidationError>();

        sheetErrors.addAll(checkFormatting(programName, job.getIsExtendedSize(), siteCodes, species, rows));

        Collection<StagedRowFormatted> validRows = formatRowsWithSpecies(rows, species);

        if (validRows != null) {
            response.setRowCount(validRows.size());
            response.setSiteCount(validRows.stream().map(r -> r.getSite()).distinct().count());
            response.setDiverCount(validRows.stream().map(r -> r.getDiver()).distinct().count());
            response.setObsItemCount(validRows.stream().map(r -> r.getSpecies()).filter(o -> o.isPresent()).distinct().count());

            sheetErrors.addAll(checkData(programName, job.getIsExtendedSize(), validRows));

            Map<String, List<StagedRowFormatted>> surveyMap = validRows.stream().filter(row -> row.getSurvey() != null).collect(Collectors.groupingBy(StagedRowFormatted::getSurvey));
            sheetErrors.addAll(checkSurveys(programName, job.getIsExtendedSize(), surveyMap));
            response.setIncompleteSurveyCount(sheetErrors.stream().filter(e -> e.getMessage().contains("Survey incomplete")).count());

            Map<String, List<StagedRowFormatted>> method3SurveyMap = validRows.stream().filter(row -> row.getMethod() != null && row.getMethod().equals(3) && row.getCode() != null && !row.getCode().equalsIgnoreCase("snd")).collect(Collectors.groupingBy(StagedRowFormatted::getSurveyGroup));
            sheetErrors.addAll(checkMethod3Transects(programName, job.getIsExtendedSize(), method3SurveyMap));
            response.setSurveyCount(surveyMap.keySet().size());
            response.setErrors(sheetErrors);
        }

        return response;
    }
}
