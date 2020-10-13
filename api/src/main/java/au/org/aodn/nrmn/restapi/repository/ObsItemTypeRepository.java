package au.org.aodn.nrmn.restapi.repository;

import au.org.aodn.nrmn.restapi.model.db.ObsItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
* Generated by Spring Data Generator on 10/01/2020
*/
@Repository
public interface ObsItemTypeRepository extends JpaRepository<ObsItemType, Integer>, JpaSpecificationExecutor<ObsItemType> {

}
