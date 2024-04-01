package ru.itis.rgjudge.db.repository;

import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.db.model.ElementShortInfo;

import java.util.List;
import java.util.Optional;

public interface ElementRepository {
    List<ElementShortInfo> getAll();
    Optional<Element> findByIdFull(Integer id);
}
