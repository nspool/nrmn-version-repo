package au.org.aodn.nrmn.restapi.model.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Cache;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

@Entity
@Cache(region = "entities", usage = READ_WRITE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "location_ref")
@Audited(withModifiedFlag = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location {
    @Id
    @SequenceGenerator(name = "location_ref_location_id", sequenceName = "location_ref_location_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_ref_location_id")
    @Column(name = "location_id", unique = true, updatable = false, nullable = false)
    private Integer locationId;

    @Column(name = "location_name")
    @NotNull
    private String locationName;

    @Column(name = "is_active")
    @NotNull
    private Boolean isActive;
}
