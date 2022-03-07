package st.old.isis.extraction.services.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.builder.StorageUrl;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

@Service
@RequiredArgsConstructor
public class StorageService {

  private final StorageUrl storageUrl;
  private final InternalRestService restService;
  private final ObjectUtils objectUtils;

  public ListDto<CommunicationChainDto> search(@NonNull final CommunicationChainDto filter)
      throws IsisException {

    final var url = storageUrl.communication().search();

    final var entity =
        HttpEntityBuilder.transactionalBuilder()
            .jsonContent()
            .body(objectUtils.toString(filter))
            .build();

    final var typeReference = new ParameterizedTypeReference<ListDto<CommunicationChainDto>>() {};

    return restService.post(url, entity, typeReference);
  }

  public ListDto<CommunicationChainDto> importChain(@NonNull final ListDto<CommunicationChainDto> request)
          throws IsisException {

    final var url = storageUrl.chain().importChain();

    final var entity =
            HttpEntityBuilder.transactionalBuilder()
                    .jsonContent()
                    .body(objectUtils.toString(request))
                    .build();

    final var typeReference = new ParameterizedTypeReference<ListDto<CommunicationChainDto>>() {};

    return restService.post(url, entity, typeReference);
  }


}
