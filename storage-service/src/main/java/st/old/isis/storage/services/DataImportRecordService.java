package st.old.isis.storage.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.DataImportRecord;
import st.old.isis.storage.repositories.DataImportRecordRepository;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportRecordService {

    private final DataImportRecordRepository repository;

    @Audited(event = "CREATE_DATA_IMPORT_RECORD")
    public DataImportRecordDto create(@NonNull final DataImportRecordDto dto) throws IsisException{

        log.info("Creating data import record {}", dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_DATA_IMPORT_RECORD")
    public DataImportRecordDto update(@NonNull final DataImportRecordDto dto) throws IsisException{

        log.info("Updating data import record {}", dto);

        final var oldEntity = findById(dto.getId());

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_DATA_IMPORT_RECORD_BY_ID")
    public DataImportRecordDto getById(@NonNull @Audited final String id) throws IsisException{

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_DATA_IMPORT_RECORD")
    public void delete(@NonNull @Audited final String id) throws IsisException{

        log.info("Deleting data import record id: {}", id);

        final var entity = findById(id);

        repository.delete(entity);
    }

    private DataImportRecord findById(@NonNull final String id) throws IsisException{
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                       String.format("Cannot find Policy with id: [%s]", id),
                       "NotFoundException"));
    }

    @Audited(event = "GET_DATA_IMPORT_RECORD_BY_STATUS")
    public ListDto<DataImportRecordDto> findByStatus(ProcessedStatus status){
        final var records = repository.findByProcessedStatusOrderByImportDateAsc(status);
        ListDto<DataImportRecordDto> results = new ListDto<>();
        results.setData(new ArrayList<>());

        records.stream().forEach(x-> {
            var dto = mapToResource(x);
            results.getData().add(dto);

        });

        results.setResponseStatus("success");
        return results;
    }

    private DataImportRecord mapToEntity(@NonNull final DataImportRecordDto dto){
        return DataImportRecord.builder()
                .id(dto.getId())
                .source(dto.getSource())
                .importDate(dto.getImportDate())
                .processedStatus(dto.getProcessedStatus())
                .build();
    }

    private DataImportRecordDto mapToResource(@NonNull final DataImportRecord entity){
        return DataImportRecordDto.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .importDate(entity.getImportDate())
                .processedStatus(entity.getProcessedStatus())
                .build();
    }

    //map read-only properties from old entity to new entity
    private DataImportRecord mapToEntity(@NonNull DataImportRecord oldEntity, @NonNull DataImportRecordDto newDto) {
        final var entity = mapToEntity(newDto);
        return entity;
    }

}
