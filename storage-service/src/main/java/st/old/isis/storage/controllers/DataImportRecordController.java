package st.old.isis.storage.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.DataImportRecordService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data-import-record")
public class DataImportRecordController {

    private final DataImportRecordService service;

    @PutMapping()
    public DataImportRecordDto create(@NonNull final DataImportRecordDto dto) throws IsisException {
        return service.create(dto);
    }

}
