package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.EscalationHistoryDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.EscalationHistory;
import st.old.isis.storage.repositories.EscalationHistoryRepository;
import st.old.isis.storage.specifications.EscalationHistorySpecification;
import st.old.isis.storage.validators.EscalationHistoryValidator;

import java.util.ArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class EscalationHistoryService {

    private final EscalationHistoryRepository repository;

    private final EscalationHistoryValidator validator;


    public EscalationHistoryDto createRecord(EscalationHistoryDto dto) throws IsisException {

        log.info("Creating escalation history record {}" , dto);

        validator.validate(dto);

        var newRecord = mapToEntity(dto);

        var createdRecord = repository.save(newRecord);

        return mapToDto(createdRecord);
    }

    public EscalationHistoryDto updateRecord(EscalationHistoryDto dto) throws IsisException {

        log.info("Updating escalation history record {}" , dto);
        validator.validate(dto);

        //todo add logging service call

        var updatedRecord = mapToEntity(dto);
        var user = repository.save(updatedRecord);
        return mapToDto(user);
    }

    public EscalationHistoryDto getRecordById(String id) throws IsisException {
        return mapToDto(findById(id));

    }

    private EscalationHistory findById(String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> new IsisException("Unable To Find Valid Existing Escalation History Record", "InvalidIdException"));
    }

    public ListDto<EscalationHistoryDto> findWithFilter(EscalationHistoryDto filter){

        final var specification = new EscalationHistorySpecification(filter);
        final var pageRecords = repository.findAll(specification);
        ListDto<EscalationHistoryDto> results = new ListDto<>();
        results.setData(new ArrayList<>());

        pageRecords.stream().forEach(x-> {
            var dto = mapToDto(x);
            results.getData().add(dto);

        });

        results.setResponseStatus("success");
        return results;
    }

    private  EscalationHistoryDto mapToDto(EscalationHistory entity){

        if (entity == null){
            return null;
        }

        return EscalationHistoryDto.builder()
                .dateCreated(entity.getDateCreated())
                .id(entity.getId())
                .alertId(entity.getAlertId())
                .escalatedFrom(entity.getEscalatedFrom())
                .escalatedTo(entity.getEscalatedTo())
                .executorId(entity.getExecutorId())
                .dateModified(entity.getDateModified())
                .responseStatus("success")
                .build();
    }

    private EscalationHistory mapToEntity(EscalationHistoryDto dto){
        return EscalationHistory.builder()
                .dateCreated(dto.getDateCreated())
                .id(dto.getId())
                .alertId(dto.getAlertId())
                .escalatedFrom(dto.getEscalatedFrom())
                .escalatedTo(dto.getEscalatedTo())
                .executorId(dto.getExecutorId())
                .dateModified(dto.getDateModified())
                .build();
    }


}
