package st.old.isis.extraction.services.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.BatchResultType;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.builder.StorageUrl;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportRecordService {

  private final StorageUrl storageUrl;
  private final InternalRestService restService;
  private final ObjectUtils objectUtils;

  public DataImportRecordDto create(@NonNull final DataImportRecordDto request) throws IsisException {

    final var url = storageUrl.dataImportRecord().root();

    final var entity =
            HttpEntityBuilder.transactionalBuilder()
                    .jsonContent()
                    .body(objectUtils.toString(request))
                    .build();

    log.info("Creating Data Import Record {}. Sending to url {}", request, url);

    return restService.put(url, entity, DataImportRecordDto.class);
  }

}
