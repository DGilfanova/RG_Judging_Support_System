INSERT INTO element(id, official_number, name, value, type_by_support_leg)
VALUES (1, '1', 'Свободная нога назад ниже горизонтали, с наклоном туловища назад', 0.1, 'BACK'),
       (2, '2.1', 'Пассе вперед', 0.1, 'FRONT'),
       (3, '2.2', 'Пассе в сторону (горизонтальная позиция)', 0.1, 'SIDE'),
       (4, '2.3', 'Пассе с наклоном верхней части спины и плеч назад', 0.1, 'FRONT'),
       (5, '5.2', 'Передний шпагат без помощи, наклон туловища назад в горизонталь', 0.5, 'FRONT'),
       (6, '10.1', 'Боковой шпагат без помощи, туловище в сторону в горизонталь', 0.5, 'SIDE'),
       (7, '11.3', 'Арабеск: свободная нога горизонтально назад и туловище назад в горизонтальном положении', 0.4, 'BACK'),
       (8, '14.2', 'Задний шпагат без помощи, наклон туловища вперед в горизонталь или ниже', 0.5, 'BACK'),
       (9, '15.1', 'Задний шпагат без помощи в кольцо, наклон туловища вперед в горизонталь', 0.5, 'BACK'),
       (10, '3.1', 'Свободная нога горизонтально вперед (=согнутая на 30° – положение «Tire-Buchon») ', 0.1, 'FRONT')
ON CONFLICT DO NOTHING;


INSERT INTO leg_split_criteria(element_id, min_degree, max_degree)
VALUES (5, 180, 225),
       (6, 180, 225),
       (7, 90, 100),
       (8, 180, 225),
       (9, 180, 225),
       (10, 90, 100)
ON CONFLICT DO NOTHING;


INSERT INTO body_posture_criteria(element_id, min_degree, max_degree)
VALUES (5, 225, 275),
       (6, 85, 95),
       (7, 225, 270),
       (8, 0, 100),
       (9, 85, 95),
       (10, 175, 185)
ON CONFLICT DO NOTHING;


INSERT INTO leg_position_criteria(element_id, side, position_type, estimation_type)
VALUES (5, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (5, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (6, 'LEFT', 'STRAIGHT', 'ONLY_PENALTY'),
       (6, 'RIGHT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (7, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (8, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (9, 'LEFT', 'BENT', 'ONLY_VALIDNESS'),
       (10, 'LEFT', 'STRAIGHT', 'ONLY_VALIDNESS'),
       (10, 'RIGHT', 'BENT_LESS_90', 'ONLY_VALIDNESS')
ON CONFLICT DO NOTHING;


INSERT INTO hand_to_leg_touch_criteria(element_id, type, is_touch)
VALUES (8, 'LOW_POSTURE', false)
ON CONFLICT DO NOTHING;
