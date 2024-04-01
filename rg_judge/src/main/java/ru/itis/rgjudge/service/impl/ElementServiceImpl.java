package ru.itis.rgjudge.service.impl;

import org.springframework.stereotype.Component;
import ru.itis.rgjudge.db.repository.ElementRepository;
import ru.itis.rgjudge.dto.ElementResponse;
import ru.itis.rgjudge.mapper.ElementMapper;
import ru.itis.rgjudge.service.ElementService;

import java.util.List;

@Component
public class ElementServiceImpl implements ElementService {

    private final ElementRepository elementRepository;
    private final ElementMapper mapper;

    public ElementServiceImpl(ElementRepository elementRepository, ElementMapper mapper) {
        this.elementRepository = elementRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ElementResponse> getAllElements() {
        return mapper.toResponseList(elementRepository.getAll());
    }
}
