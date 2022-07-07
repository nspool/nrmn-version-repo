package au.org.aodn.nrmn.restapi.model.db;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "survey_method")
@Audited(withModifiedFlag = true)
public class SurveyMethodEntity {
    @Id
    @SequenceGenerator(name = "survey_method_survey_method_id", sequenceName = "survey_method_survey_method_id",
        allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="survey_method_survey_method_id")
    @Column(name = "survey_method_id", unique = true, updatable = false, nullable = false)
    private Integer surveyMethodId;

    @Basic
    @Column(name = "block_num")
    private Integer blockNum;

    @Basic
    @Column(name = "survey_not_done")
    private Boolean surveyNotDone;

    @Basic
    @Column(name = "survey_method_attribute", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Map<String, String> surveyMethodAttribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", referencedColumnName = "survey_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", referencedColumnName = "method_id", nullable = false)
    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @JsonIgnore
    private Method method;

}