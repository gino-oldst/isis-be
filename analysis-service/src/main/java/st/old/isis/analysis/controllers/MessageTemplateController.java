package st.old.isis.analysis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.analysis.services.MessageTemplateService;
import st.old.isis.dto.analysis.MessageTemplateDto;
import st.old.isis.exception.IsisException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/message-template")
public class MessageTemplateController {

    private final MessageTemplateService service;

    @PostMapping
    public MessageTemplateDto create(@RequestBody MessageTemplateDto template) throws IsisException {
        return service.create(template);
    }

    @PutMapping
    public MessageTemplateDto update(@RequestBody MessageTemplateDto template) throws IsisException {
        return service.update(template);
    }

    @GetMapping("/id/{id}")
    public MessageTemplateDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @GetMapping("/name/{name}")
    public MessageTemplateDto getByName(@PathVariable final String name) throws IsisException {
        return service.getByName(name);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
