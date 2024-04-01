package ru.itis.rgjudge.mapper;

import org.mapstruct.Mapper;
import ru.itis.rgjudge.db.model.ElementShortInfo;
import ru.itis.rgjudge.dto.ElementResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ElementMapper {

    List<ElementResponse> toResponseList(List<ElementShortInfo> elementShortInfos);
}
