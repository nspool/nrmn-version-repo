package au.org.aodn.nrmn.restapi.model.db;

import au.org.aodn.nrmn.restapi.model.db.enums.UserSecStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sec",
        schema = "nrmn",
        catalog = "nrmn",
        uniqueConstraints ={@UniqueConstraint( name = "UNIQUE_EMAIL",columnNames = {"email_address"})} )
public class UserSecEntity {

    @Id
    @SequenceGenerator(name="user_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "user_id_seq")
    @Column(name="id", unique=true, updatable=false, nullable=false)
    @Getter @Setter private int userId;

    @Version
    @Column(name = "version", nullable = false)
    @Getter @Setter  private Integer version;



    @Column(name = "full_name")
    @Getter @Setter private String fullName;

    @Column(name = "email_address", nullable = false)
    @Getter @Setter private String email;

    @Column(name = "hashed_password")
    @Getter @Setter private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable=false)
    @Getter @Setter private UserSecStatus status;


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_sec_roles",
            joinColumns = @JoinColumn(name = "user_sec_id",foreignKey=@ForeignKey(name="FK_USER_SEC_ROLE")),
            inverseJoinColumns = @JoinColumn(name = "user_sec_role_id", foreignKey=@ForeignKey(name="FK_ROLE_USER_SEC")))
    @Getter @Setter private Set<UserSecRoleEntity> roles = new HashSet<>();

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (hashedPassword != null ? hashedPassword.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
