package au.org.aodn.nrmn.restapi.model.db;

import au.org.aodn.nrmn.restapi.model.db.Observation.ObservationBuilder;

import java.util.HashMap;
import java.util.Map;

import au.org.aodn.nrmn.restapi.repository.ObservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObservationTestData {

    @Autowired
    private DiverTestData diverTestData;

    @Autowired
    private ObservableItemTestData observableItemTestData;

    @Autowired
    private MeasureTestData measureTestData;

    @Autowired
    private ObservationRepository observationRepository;

    public Observation persistedObservation(Observation observation) {
        observationRepository.saveAndFlush(observation);
        return observation;
    }

    public Observation buildWith(SurveyMethodEntity enitity, Diver diver, int itemNumber) {
        final Map<String, String> observationAttribute = new HashMap<String, String>();
        observationAttribute.put("Item Number", String.valueOf(itemNumber));
        return Observation.builder()
                .surveyMethod(enitity)
                .diver(diver)
                .observableItem(observableItemTestData.persistedObservableItem())
                .measure(measureTestData.persistedMeasure())
                .measureValue(itemNumber)
                .observationAttribute(observationAttribute)
                .build();

    }

    public ObservationBuilder defaultBuilder() {
        final Map<String, String> observationAttribute = new HashMap<String, String>();
        observationAttribute.put("Biomass", "0.7630353218");
        return Observation.builder()
            .diver(diverTestData.persistedDiver())
            .observableItem(observableItemTestData.persistedObservableItem())
            .measure(measureTestData.persistedMeasure())
            .measureValue(4)
            .observationAttribute(observationAttribute);
    }

}
