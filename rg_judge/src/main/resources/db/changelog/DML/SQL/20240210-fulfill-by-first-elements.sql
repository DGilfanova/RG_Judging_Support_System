INSERT INTO element(id, official_number, name, value, type_by_support_leg)
VALUES (1, '1', 'Свободная нога назад ниже горизонтали, с наклоном туловища назад', 0.1, 'BACK'),
       (2, '2.1', 'Пассе вперед', 0.1, 'FRONT'),
       (3, '2.2', 'Пассе в сторону (горизонтальная позиция)', 0.1, 'SIDE'),
       (4, '2.3', 'Пассе с наклоном верхней части спины и плеч назад', 0.1, 'FRONT'),
       (5, '5.2', 'Передний шпагат без помощи, наклон туловища назад в горизонталь', 0.5, 'FRONT'),
       (6, '10.1', 'Боковой шпагат c помощью, туловище в сторону в горизонталь', 0.4, 'SIDE'),
       (7, '11.3', 'Арабеск: свободная нога горизонтально назад и туловище назад в горизонтальном положении', 0.4,
        'BACK'),
       (8, '14.2', 'Задний шпагат без помощи, наклон туловища вперед в горизонталь или ниже', 0.5, 'BACK'),
       (9, '15.1', 'Задний шпагат без помощи в кольцо, наклон туловища вперед в горизонталь', 0.5, 'BACK'),
       (10, '3.1', 'Свободная нога горизонтально вперед (=согнутая на 30° – положение «Tire-Buchon»)', 0.4, 'SIDE'),
       (11, '9.2', 'Боковой шпагат без помощи', 0.4, 'SIDE'),
       (12, '12.1', 'Аттитюд', 0.2, 'BACK'),
       (13, '6.1', 'Передний шпагат без помощи, наклон туловища назад ниже горизонтали из положения стоя', 0.5,
        'FRONT'),
       (14, '11.1', 'Арабеск: свободная нога горизонтально назад и туловище ровно', 0.2, 'BACK'),
       (15, '10.2', 'Боковой шпагат без помощи, туловище в сторону в горизонталь', 0.5, 'SIDE'),
       (16, '6.3', 'Передний шпагат без помощи с касанием бедра', 0.5, 'FRONT'),
       (17, '4.2', 'Передний шпагат без помощи', 0.4, 'FRONT'),
       (18, '13.1', 'Задний шпагат с помощью, стопа выше головы', 0.3, 'BACK'),
       (19, '16.1', 'Кольцо без помощи, стопа касается головы', 0.4, 'BACK')
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


INSERT INTO hand_to_leg_touch_criteria(element_id, type, is_touch)
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


INSERT INTO head_to_leg_touch_criteria(element_id, type, is_touch)
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