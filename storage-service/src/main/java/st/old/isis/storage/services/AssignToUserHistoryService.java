package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AssignToUserHistoryDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AssignToUserHistory;
import st.old.isis.storage.repositories.AssignToUserHistoryRepository;
import st.old.isis.storage.specifications.AssignToUserHistorySpecification;
import st.old.isis.storage.validators.AssignToUserHistoryValidator;

import java.util.ArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class AssignToUserHistoryService {

    private final AssignToUserHistoryRepository repository;

    private final AssignToUserHistoryValidator validator;


    public AssignToUserHistoryDto createRecord(AssignToUserHistoryDto dto) throws IsisException {

        log.info("Creating assign to user history record {}" , dto);

        validator.validate(dto);

        var newRecord = mapToEntity(dto);

        var createdRecord = repository.save(newRecord);

        return mapToDto(createdRecord);
    }

    public AssignToUserHistoryDto updateRecord(AssignToUserHistoryDto dto) throws IsisException {

        log.info("Updating assign to user history record {}" , dto);
        validator.validate(dto);

        var updatedRecord = mapToEntity(dto);
        var user = repository.save(updatedRecord);
        return mapToDto(user);
    }

    public AssignToUserHistoryDto getRecordById(String id) throws IsisException {
        return mapToDto(findByAlertId(id));

    }

    private AssignToUserHistory findById(String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> new IsisException("Unable To Find Valid Existing Assign To User History Record", "InvalidIdException"));
    }

    private AssignToUserHistory findByAlertId(String alertId) {
        return repository.findFirstByAlertIdOrderByDateCreatedDesc(alertId);
    }

    public ListDto<AssignToUserHistoryDto> findWithFilter(AssignToUserHistoryDto filter){

        final var specification = new AssignToUserHistorySpecification(filter);
        final var pageRecords = repository.findAll(specification);
        ListDto<AssignToUserHistoryDto> results = new ListDto<>();
        results.setData(new ArrayList<>());

        pageRecords.stream().forEach(x-> {
            var dto = mapToDto(x);
            results.getData().add(dto);
        });

        results.setResponseStatus("success");
        return results;
    }

    private  AssignToUserHistoryDto mapToDto(AssignToUserHistory entity){

        if (entity == null){
            return null;
        }

        return AssignToUserHistoryDto.builder()
                .dateCreated(entity.getDateCreated())
                .id(entity.getId())
                .alertId(entity.getAlertId())
                .assignedFrom(entity.getAssignedFrom())
                .assignedTo(entity.getAssignedTo())
                .executorId(entity.getExecutorId())
                .dateModified(entity.getDateModified())
                .responseStatus("success")
                .build();
    }

    private AssignToUserHistory mapToEntity(AssignToUserHistoryDto dto){
        return AssignToUserHistory.builder()
                .dateCreated(dto.getDateCreated())
                .id(dto.getId())
                .alertId(dto.getAlertId())
                .assignedFrom(dto.getAssignedFrom())
                .assignedTo(dto.getAssignedTo())
                .executorId(dto.getExecutorId())
                .dateModified(dto.getDateModified())
                .build();
    }


}
