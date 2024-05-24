INSERT INTO element(id, official_number, name, value, type_by_support_leg, type_by_execution)
VALUES
    -- Standing
    (1, '1', 'Свободная нога назад ниже горизонтали, с наклоном туловища назад', 0.1, 'BACK', 'STATIC_STANDING'),
    (2, '2.1', 'Пассе вперед', 0.1, 'FRONT', 'STATIC_STANDING'),
    (3, '2.2', 'Пассе в сторону (горизонтальная позиция)', 0.1, 'SIDE', 'STATIC_STANDING'),
    (4, '2.3', 'Пассе с наклоном верхней части спины и плеч назад', 0.1, 'FRONT', 'STATIC_STANDING'),
    (10, '3.1', 'Свободная нога горизонтально вперед (=согнутая на 30° – положение «Tire-Buchon»)', 0.1, 'FRONT', 'STATIC_STANDING'),
    (20, '3.2', 'Свободная нога горизонтально вперед (выпрямленная)', 0.2, 'FRONT', 'STATIC_STANDING'),
    (21, '3.3', 'Свободная нога горизонтально вперед и туловище назад в горизонтальном положении', 0.3, 'FRONT', 'STATIC_STANDING'),
    (22, '4.1', 'Передний шпагат с помощью', 0.3, 'FRONT', 'STATIC_STANDING'),
    (17, '4.2', 'Передний шпагат без помощи', 0.4, 'FRONT', 'STATIC_STANDING'),
    (23, '5.1', 'Передний шпагат с помощью, наклон туловища назад в горизонталь', 0.4, 'FRONT', 'STATIC_STANDING'),
    (5, '5.2', 'Передний шпагат без помощи, наклон туловища назад в горизонталь', 0.5, 'FRONT', 'STATIC_STANDING'),
    (24, '5.3', 'Передний шпагат без помощи, с ногой, согнутой на 30°, наклон туловища назад в горизонталь', 0.5, 'FRONT', 'STATIC_STANDING'),
    (13, '6.1', 'Передний шпагат без помощи, наклон туловища назад ниже горизонтали из положения стоя', 0.5, 'FRONT', 'STATIC_STANDING'),
    (25, '6.2', 'Равновесие Крaмаренко (KR)–КB с ногой, согнутой на 30°, наклон туловища назад ниже горизонтали из положения стоя', 0.5, 'FRONT', 'STATIC_STANDING'),
    (16, '6.3', 'Передний шпагат без помощи с касанием бедра', 0.5, 'FRONT', 'STATIC_STANDING'),
    (26, '8.1', 'Свободная нога в сторону в горизонталь ', 0.2, 'SIDE', 'STATIC_STANDING'),
    (27, '8.2', 'Свободная нога в сторону в горизонталь и туловище в сторону в горизонталь', 0.3, 'SIDE', 'STATIC_STANDING'),
    (28, '9.1', ' Боковой шпагат с помощью', 0.3, 'FRONT', 'STATIC_STANDING'),
    (11, '9.2', 'Боковой шпагат без помощи', 0.4, 'FRONT', 'STATIC_STANDING'),
    (6, '10.1', 'Боковой шпагат c помощью, туловище в сторону в горизонталь', 0.4, 'SIDE', 'STATIC_STANDING'),
    (15, '10.2', 'Боковой шпагат без помощи, туловище в сторону в горизонталь', 0.5, 'SIDE', 'STATIC_STANDING'),
    (29, '10.3', 'Боковой шпагат без помощи, с ногой, согнутой на 30°, туловище горизонтально', 0.5, 'SIDE', 'STATIC_STANDING'),
    (14, '11.1', 'Арабеск: свободная нога горизонтально назад и туловище ровно', 0.2, 'BACK', 'STATIC_STANDING'),
    (30, '11.2', 'Арабеск: свободная нога горизонтально назад и туловище вперед', 0.3, 'BACK', 'STATIC_STANDING'),
    (7, '11.3', 'Арабеск: свободная нога горизонтально назад и туловище назад в горизонтальном положении', 0.4, 'BACK', 'STATIC_STANDING'),
    (12, '12.1', 'Аттитюд', 0.2, 'BACK', 'STATIC_STANDING'),
    (31, '12.2', 'Аттитюд с наклоном туловища назад', 0.4, 'BACK', 'STATIC_STANDING'),
    (18, '13.1', 'Задний шпагат с помощью, стопа выше головы', 0.3, 'BACK', 'STATIC_STANDING'),
    (47, '13.2', 'Задний шпагат с помощью, стопа выше головы без помощи', 0.4, 'BACK', 'STATIC_STANDING'),
    (32, '14.1', 'Задний шпагат с помощью, наклон туловища вперед в горизонталь или ниже', 0.4, 'BACK', 'STATIC_STANDING'),
    (8, '14.2', 'Задний шпагат без помощи, наклон туловища вперед в горизонталь или ниже', 0.5, 'BACK', 'STATIC_STANDING'),
    (9, '15.1', 'Задний шпагат без помощи в кольцо, наклон туловища вперед в горизонталь', 0.5, 'BACK', 'STATIC_STANDING'),
    (33, '15.2', 'Кольцо с помощью, стопа касается головы', 0.3, 'BACK', 'STATIC_STANDING'),
    (34, '15.3', 'Кольцо с помощью, бедро касается головы', 0.4, 'BACK', 'STATIC_STANDING'),
    (19, '16.1', 'Кольцо без помощи, стопа касается головы', 0.4, 'BACK', 'STATIC_STANDING'),
    (35, '16.2', 'Кольцо без помощи, бедро касается головы, также с наклоном туловища назад', 0.4, 'BACK', 'STATIC_STANDING'),
    -- Sitting
    (36, '19.1', 'На колене, свободная нога назад в горизонталь, с наклоном туловища назад горизонтально', 0.2, 'BACK', 'STATIC_SITTING'),
    (37, '19.2', 'На колене, свободная нога назад в горизонталь, с наклоном туловища назад горизонтально', 0.3, 'BACK', 'STATIC_SITTING'),
    (38, '20.1', 'На колене, нога вперед, стопа выше головы с помощью', 0.1, 'FRONT', 'STATIC_SITTING'),
    (39, '20.2', 'На колене, нога вперед, стопа выше головы без помощи', 0.2, 'FRONT', 'STATIC_SITTING'),
    (40, '20.3', 'На колене, нога вперед, стопа выше головы  с наклоном туловища назад горизонтально или ниже', 0.4, 'FRONT', 'STATIC_SITTING'),
    (41, '21.1', 'На колене, свободная нога в сторону, стопа выше головы с помощью', 0.1, 'SIDE', 'STATIC_SITTING'),
    (42, '21.2', 'На колене, свободная нога в сторону, стопа выше головы без помощи, также с наклоном туловища в сторону горизонтально', 0.2, 'SIDE', 'STATIC_SITTING'),
    (43, '22.1', 'На колене, задний шпагат свободной ноги, стопа выше головы с помощью', 0.1, 'BACK', 'STATIC_SITTING'),
    (44, '22.2', 'На колене, задний шпагат свободной ноги, стопа выше головы без помощи', 0.2, 'BACK', 'STATIC_SITTING'),
    (45, '23.1', 'На колене, кольцо с помощью или без помощи', 0.1, 'BACK', 'STATIC_SITTING'),
    (46, '23.2', 'На колене, кольцо с помощью или без помощи', 0.2, 'BACK', 'STATIC_SITTING')
ON CONFLICT DO NOTHING;


INSERT INTO leg_split_criteria(element_id, min_degree, max_degree)
VALUES (2, 90, 110),
       (5, 180, 225),
       (17, 180, 225),
       (6, 180, 225),
       (7, 90, 120),
       (8, 180, 225),
       (9, 180, 225),
       (10, 90, 100),
       (11, 180, 225),
       (12, 90, 100),
       (13, 180, 225),
       (16, 180, 225),
       (14, 90, 120),
       (15, 180, 225),
       (18, 135, 225),
       (19, 80, 180)
ON CONFLICT DO NOTHING;


INSERT INTO body_posture_criteria(element_id, min_degree, max_degree)
VALUES (2, 170, 190),
       (5, 250, 290),
       (17, 170, 260),
       (6, 85, 95),
       (7, 225, 270),
       (8, 0, 100),
       (9, 70, 100),
       (10, 175, 185),
       (11, 175, 185),
       (12, 175, 185),
       (13, 315, 360),
       (16, 290, 360),
       (14, 150, 170),
       (15, 250, 290),
       (18, 120, 200),
       (19, 160, 200)
ON CONFLICT DO NOTHING;


INSERT INTO hand_to_leg_touch_criteria(element_id, posture_type, is_touch)
VALUES (5, 'LOW_POSTURE', false),
       (17, 'HIGH_POSTURE', false),
       (6, 'LOW_POSTURE', true),
       (8, 'LOW_POSTURE', false),
       (11, 'HIGH_POSTURE', false),
       (13, 'HIGH_POSTURE', false),
       (16, 'HIGH_POSTURE', false),
       (15, 'LOW_POSTURE', false),
       (18, 'HIGH_POSTURE', true),
       (19, 'HIGH_POSTURE', false)
ON CONFLICT DO NOTHING;


INSERT INTO leg_position_criteria(element_id, side, position_type, estimation_type)
VALUES (2, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (2, 'RIGHT', 'BENT_LESS_90', 'ONLY_VALIDNESS'),
       (3, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (3, 'RIGHT', 'BENT_LESS_90', 'ONLY_VALIDNESS'),
       (4, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (4, 'RIGHT', 'BENT_LESS_90', 'ONLY_VALIDNESS'),
       (5, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (5, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (17, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (17, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (6, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (6, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (7, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (8, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (9, 'LEFT', 'BENT', 'ONLY_VALIDNESS'),
       (10, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (10, 'RIGHT', 'BENT_LESS_90', 'ONLY_VALIDNESS'),
       (11, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (11, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (12, 'LEFT', 'BENT_MORE_90', 'ONLY_VALIDNESS'),
       (13, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (13, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (16, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (16, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (14, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (15, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (15, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (18, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (19, 'LEFT', 'BENT_LESS_90', 'ONLY_VALIDNESS')
ON CONFLICT DO NOTHING;


INSERT INTO head_to_leg_touch_criteria(element_id, posture_type, is_touch)
VALUES (9, 'SHALLOW_POSTURE', true),
       (16, 'DEEP_POSTURE', true),
       (18, 'DEEP_POSTURE', true),
       (19, 'SHALLOW_POSTURE', true)
ON CONFLICT DO NOTHING;


INSERT INTO leg_to_leg_touch_criteria(element_id, is_touch)
VALUES (2, true),
       (3, true),
       (4, true)
ON CONFLICT DO NOTHING;