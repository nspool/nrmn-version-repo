package au.org.aodn.nrmn.restapi.repository;

import au.org.aodn.nrmn.restapi.model.db.AphiaRef;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
@Tag(name = "aphia refs")
public interface AphiaRefRepository extends JpaRepository<AphiaRef, Integer>, JpaSpecificationExecutor<AphiaRef> {

    @Override
    @RestResource
    <S extends AphiaRef> S save(S s);

    @Override
    @RestResource
    Optional<AphiaRef> findById(Integer integer);

    @Override
    @RestResource
    Page<AphiaRef> findAll(Pageable pageable);
}
