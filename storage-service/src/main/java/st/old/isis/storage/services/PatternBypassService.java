package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.EscalationHistoryDto;
import st.old.isis.dto.storage.PatternBypassDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.PatternBypass;
import st.old.isis.storage.repositories.PatternBypassRepository;
import st.old.isis.storage.specifications.PatternBypassSpecification;

import java.util.ArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class PatternBypassService {

    private final PatternBypassRepository repository;

    public PatternBypassDto createRecord(PatternBypassDto dto) throws IsisException {

        log.info("Creating escalation history record {}" , dto);

        var newRecord = mapToEntity(dto);

        var createdRecord = repository.save(newRecord);

        return mapToDto(createdRecord);
    }

    public PatternBypassDto updateRecord(PatternBypassDto dto) throws IsisException {

        log.info("Updating escalation history record {}" , dto);

        var updatedRecord = mapToEntity(dto);
        var user = repository.save(updatedRecord);
        return mapToDto(user);
    }

    public PatternBypassDto getRecordById(String id) throws IsisException {
        return mapToDto(findById(id));

    }

    private PatternBypass findById(String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> new IsisException("Unable To Find Valid Existing Pattern Bypass Record", "InvalidIdException"));
    }

    public ListDto<PatternBypassDto> findWithFilter(PatternBypassDto filter){

        final var specification = new PatternBypassSpecification(filter);
        final var pageRecords = repository.findAll(specification);
        ListDto<PatternBypassDto> results = new ListDto<>();
        results.setData(new ArrayList<>());

        pageRecords.stream().forEach(x-> {
            var dto = mapToDto(x);
            results.getData().add(dto);

        });

        results.setResponseStatus("success");
        return results;
    }

    private PatternBypassDto mapToDto(PatternBypass entity){

        if (entity == null){
            return null;
        }

        return PatternBypassDto.builder()
                .id(entity.getId())
                .value(entity.getValue())
                .patternType(entity.getPatternType())
                .bypassType(entity.getBypassType())
                .targetMessageTypes(entity.getTargetMessageTypes())
                .responseStatus("success")
                .build();
    }

    private PatternBypass mapToEntity(PatternBypassDto dto){
        return PatternBypass.builder()
                .id(dto.getId())
                .value(dto.getValue())
                .patternType(dto.getPatternType())
                .bypassType(dto.getBypassType())
                .targetMessageTypes(dto.getTargetMessageTypes())
                .build();
    }


}
