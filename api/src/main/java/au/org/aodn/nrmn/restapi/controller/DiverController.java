package au.org.aodn.nrmn.restapi.controller;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import au.org.aodn.nrmn.restapi.controller.exception.ResourceNotFoundException;
import au.org.aodn.nrmn.restapi.controller.validation.RowError;
import au.org.aodn.nrmn.restapi.dto.diver.DiverDto;
import au.org.aodn.nrmn.restapi.model.db.Diver;
import au.org.aodn.nrmn.restapi.repository.DiverRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Reference Data - Divers")
@RequestMapping(path = "/api/v1")
public class DiverController {

    @Autowired
    private DiverRepository diverRepository;

    @GetMapping("/divers")
    List<Diver> findAll() {
        return diverRepository.findAll();
    }

    @GetMapping("/diver/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public DiverDto findOne(@PathVariable Integer id) {
        Diver diver = diverRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new DiverDto(diver);
    }

    @PostMapping("/diver")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ResponseEntity<?> newDiver(@Valid @RequestBody DiverDto diverDto) {
        Diver diver = new Diver(diverDto.getInitials(), diverDto.getFullName());

        var errors = validateConstraints(diver);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Diver persistedDiver = diverRepository.save(diver);
        DiverDto persistedDiverDto = new DiverDto(persistedDiver);
        return ResponseEntity.status(HttpStatus.CREATED).body(persistedDiverDto);
    }

    @PutMapping("/divers")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ResponseEntity<?> updateDiver(@Valid @RequestBody List<DiverDto> diverDtos) {
        var updatedDivers = new ArrayList<Diver>();
        var errors = new ArrayList<RowError>();
        for (DiverDto diverDto : diverDtos) {
            Diver diver = diverRepository.findById(diverDto.getDiverId()).orElseThrow(ResourceNotFoundException::new);
            diver.setInitials(diverDto.getInitials());
            diver.setFullName(diverDto.getFullName());
            errors.addAll(validateConstraints(diver));
        }

        if (!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);

        var savedDivers = diverRepository.saveAll(updatedDivers);
        return ResponseEntity.ok(savedDivers.size());
    }

    private List<RowError> validateConstraints(Diver diver) {
        List<RowError> errors = new ArrayList<>();

        List<Diver> existingDiversWithInitials = diverRepository.findByInitials(diver.getInitials());

        if (diver.getDiverId() != null)
            existingDiversWithInitials = existingDiversWithInitials
                    .stream().filter(d -> !d.getDiverId().equals(diver.getDiverId())).collect(Collectors.toList());

        if (!existingDiversWithInitials.isEmpty())
            errors.add(RowError.builder().id(diver.getDiverId()).property("initials")
                    .message("A diver already has these initials.")
                    .build());

        String diverFullName = Normalizer.normalize(diver.getFullName(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        List<Diver> existingDiversWithSameName = diverRepository.findByFullName(diverFullName);

        if (diver.getDiverId() != null)
            existingDiversWithSameName = existingDiversWithSameName
                    .stream().filter(d -> !d.getDiverId().equals(diver.getDiverId())).collect(Collectors.toList());

        if (!existingDiversWithSameName.isEmpty())
            errors.add(RowError.builder().id(diver.getDiverId()).property("fullName")
                    .message("A diver with the same name already exists.")
                    .build());

        return errors;
    }

}
