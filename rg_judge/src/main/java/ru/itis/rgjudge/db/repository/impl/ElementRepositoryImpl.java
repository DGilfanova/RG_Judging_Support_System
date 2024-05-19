package ru.itis.rgjudge.db.repository.impl;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.enums.HandToLegTouchType;
import ru.itis.rgjudge.db.enums.HeadToLegTouchType;
import ru.itis.rgjudge.db.enums.LegPositionType;
import ru.itis.rgjudge.db.enums.TypeByExecution;
import ru.itis.rgjudge.db.enums.TypeBySupportLeg;
import ru.itis.rgjudge.db.model.Element;
import ru.itis.rgjudge.db.model.ElementShortInfo;
import ru.itis.rgjudge.db.repository.ElementRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.itis.rgjudge.utils.EnumUtils.getByName;

@Repository
public class ElementRepositoryImpl implements ElementRepository {

    private static final String SELECT_FULL_BY_ID_SQL = """
        SELECT id,
               name,
               official_number,
               value,
               type_by_support_leg,
               type_by_execution,
               bpc.min_degree as bpc_min_degree,
               bpc.max_degree as bpc_max_degree,
               ldc.min_degree as ldc_min_degree,
               ldc.max_degree as ldc_max_degree,
               llpc.position_type as left_leg_position_type,
               llpc.estimation_type as left_leg_estimation_type,
               rlpc.position_type as right_leg_position_type,
               rlpc.estimation_type as right_leg_estimation_type,
               hltc.type as hltc_type,
               hltc.is_touch as hltc_is_touch,
               hdltc.type as hdltc_type,
               hdltc.is_touch as hdltc_is_touch,
               lltc.is_touch as lltc_is_touch
        FROM element e
                 LEFT JOIN body_posture_criteria bpc ON e.id = bpc.element_id
                 LEFT JOIN leg_split_criteria ldc ON e.id = ldc.element_id
                 LEFT JOIN leg_position_criteria llpc ON e.id = llpc.element_id AND llpc.side = 'LEFT'
                 LEFT JOIN leg_position_criteria rlpc ON e.id = rlpc.element_id AND rlpc.side = 'RIGHT'
                 LEFT JOIN hand_to_leg_touch_criteria hltc ON e.id = hltc.element_id
                 LEFT JOIN head_to_leg_touch_criteria hdltc ON e.id = hdltc.element_id
                 LEFT JOIN leg_to_leg_touch_criteria lltc ON e.id = lltc.element_id
        WHERE id = :id;
        """;

    private static final String SELECT_ALL_SQL = """
        SELECT * FROM element;
        """;

    private static final RowMapper<ElementShortInfo> MAPPER = (rs, rowNum) ->
        ElementShortInfo.builder()
            .id(rs.getInt("id"))
            .officialNumber(rs.getString("official_number"))
            .name(rs.getString("name"))
            .value(rs.getDouble("value"))
            .typeBySupportLeg(getByName(TypeBySupportLeg.class, rs.getString("type_by_support_leg")))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .updatedAt(rs.getObject("created_at", LocalDateTime.class))
            .build();

    private static final RowMapper<Element> FULL_MAPPER = (rs, rowNum) ->
        Element.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .value(rs.getDouble("value"))
            .typeBySupportLeg(getByName(TypeBySupportLeg.class, rs.getString("type_by_support_leg")))
            .typeByExecution(getByName(TypeByExecution.class, rs.getString("type_by_execution")))
            .bodyPostureCriteria(Element.BodyPostureCriteria.builder()
                .minDegree(rs.getObject("bpc_min_degree", Float.class))
                .maxDegree(rs.getObject("bpc_max_degree", Float.class))
                .build())
            .legDegreeCriteria(Element.LegDegreeCriteria.builder()
                .minDegree(rs.getObject("ldc_min_degree", Float.class))
                .maxDegree(rs.getObject("ldc_max_degree", Float.class))
                .build())
            .leftLegPositionCriteria(Element.LeftLegPositionCriteria.builder()
                .legPosition(getByName(LegPositionType.class, rs.getString("left_leg_position_type")))
                .estimationType(getByName(EstimationType.class, rs.getString("left_leg_estimation_type")))
                .build())
            .rightLegPositionCriteria(Element.RightLegPositionCriteria.builder()
                .legPosition(getByName(LegPositionType.class, rs.getString("right_leg_position_type")))
                .estimationType(getByName(EstimationType.class, rs.getString("right_leg_estimation_type")))
                .build())
            .handToLegTouchCriteria(Element.HandToLegTouchCriteria.builder()
                .type(getByName(HandToLegTouchType.class, rs.getString("hltc_type")))
                .isTouch(rs.getObject("hltc_is_touch", Boolean.class))
                .build())
            .headToLegTouchCriteria(Element.HeadToLegTouchCriteria.builder()
                .type(getByName(HeadToLegTouchType.class, rs.getString("hdltc_type")))
                .isTouch(rs.getObject("hdltc_is_touch", Boolean.class))
                .build())
            .legToLegTouchCriteria(Element.LegToLegTouchCriteria.builder()
                .isTouch(rs.getObject("lltc_is_touch", Boolean.class))
                .build())
            .build();

    private final NamedParameterJdbcOperations jdbc;

    public ElementRepositoryImpl(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ElementShortInfo> getAll() {
        return jdbc.query(SELECT_ALL_SQL, MAPPER);
    }

    @Override
    public Optional<Element> findByIdFull(Integer id) {
        return Optional.ofNullable(
            jdbc.queryForObject(SELECT_FULL_BY_ID_SQL, new MapSqlParameterSource().addValue("id", id), FULL_MAPPER)
        );
    }
}
