package au.org.aodn.nrmn.restapi.model.db;

import au.org.aodn.nrmn.restapi.repository.ObservableItemRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
    pattern = ".*TestData"))
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("cicd")
class ObservableItemIT {
    @Autowired
    private ObservableItemRepository observableItemRepository;
    
    @Autowired
    private ObservableItemTestData observableItemTestData;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testMapping() {
        val observableItem = observableItemTestData.persistedObservableItem();
        entityManager.clear();
        val persistedObservableItem = observableItemRepository.findById(observableItem.getObservableItemId()).get();
        assertEquals(observableItem, persistedObservableItem);
    }

}
