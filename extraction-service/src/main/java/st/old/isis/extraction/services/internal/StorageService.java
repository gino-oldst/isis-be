package st.old.isis.extraction.services.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.builder.StorageUrl;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

import java.util.List;

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

  public ListDto<CommunicationChainDto> importChain(
      @NonNull final ListDto<CommunicationChainDto> request) throws IsisException {

    final var url = storageUrl.chain().importChain();

    final var entity =
        HttpEntityBuilder.transactionalBuilder()
            .jsonContent()
            .body(objectUtils.toString(request))
            .build();

    final var typeReference = new ParameterizedTypeReference<ListDto<CommunicationChainDto>>() {};

    return restService.post(url, entity, typeReference);
  }

  public ListDto<ExternalUserDto> saveExternalUsers(@NonNull final List<ExternalUserDto> request)
      throws IsisException {

    final var url = storageUrl.externalUser().batch();

    final var entity =
        HttpEntityBuilder.transactionalBuilder()
            .jsonContent()
            .body(objectUtils.toString(request))
            .build();

    final var typeReference = new ParameterizedTypeReference<ListDto<ExternalUserDto>>() {};

    return restService.post(url, entity, typeReference);
  }

  public List<CommunicationMessageDto> saveMessages(@NonNull final List<CommunicationMessageDto> request)
          throws IsisException {

    final var url = storageUrl.message().batch();

    final var entity =
            HttpEntityBuilder.transactionalBuilder()
                    .jsonContent()
                    .body(objectUtils.toString(request))
                    .build();

    final var typeReference = new ParameterizedTypeReference<List<CommunicationMessageDto>>() {};

    return restService.post(url, entity, typeReference);
  }

  public List<CommunicationRecipientDto> saveRecipients(@NonNull final List<CommunicationRecipientDto> request)
          throws IsisException {

    final var url = storageUrl.recipient().batch();

    final var entity =
            HttpEntityBuilder.transactionalBuilder()
                    .jsonContent()
                    .body(objectUtils.toString(request))
                    .build();

    final var typeReference = new ParameterizedTypeReference<List<CommunicationRecipientDto>>() {};

    return restService.post(url, entity, typeReference);
  }
}
