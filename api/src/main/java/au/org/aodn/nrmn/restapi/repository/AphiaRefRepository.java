package au.org.aodn.nrmn.restapi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import au.org.aodn.nrmn.restapi.model.db.AphiaRef;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "aphia refs")
public interface AphiaRefRepository extends JpaRepository<AphiaRef, Integer>, JpaSpecificationExecutor<AphiaRef> {

    @Override
    Optional<AphiaRef> findById(Integer integer);

    @Override
    Page<AphiaRef> findAll(Pageable pageable);
}
