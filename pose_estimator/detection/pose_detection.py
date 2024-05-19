import mediapipe as mp
import logging
import os

import cv2
import uuid

from detection.body_parts import BodyParts
from detection.video_upload import upload_video_to_minio, temp_save_video, clean_temp_saved_video
from pose_estimator import settings

logger = logging.getLogger(__name__)


def prepare_coordinates(landmark, width, height):
    coordinates = []
    for member in BodyParts:
        coordinates.append({
            "x": round(landmark[member.value].x * width, 1),
            "y": round(height - landmark[member.value].y * height, 1),
            "z": round(landmark[member.value].z, 4)
        })
    return coordinates


def detect_pose_coordinates(video):
    saved_video_path = temp_save_video(video)
    mp_drawing = mp.solutions.drawing_utils
    mp_pose = mp.solutions.pose
    with mp_pose.Pose(model_complexity=2, min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
        video_name = f'{uuid.uuid4()}.mp4'
        cap = cv2.VideoCapture(saved_video_path)

        fps = cap.get(cv2.CAP_PROP_FPS)
        height = int(cap.get(3))
        width = int(cap.get(4))
        logger.info(f'Start to detect pose for {saved_video_path}. '
                    f'Video params: fps = {fps}, height = {height}, width = {width}')

        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        out = cv2.VideoWriter(video_name, fourcc, fps, (height, width))

        pose_data = []
        frame_count = 0
        time = 0
        while cap.isOpened():
            ret, frame = cap.read()
            frame_count += 1
            time = round(frame_count / fps, 5)

            if not ret:
                break

            if height == 0 and width == 0:
                height, width, _ = frame.shape

            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            pose_results = pose.process(frame_rgb)
            if not pose_results.pose_landmarks:
                break

            mp_drawing.draw_landmarks(frame, pose_results.pose_landmarks, mp_pose.POSE_CONNECTIONS)
            out.write(frame)

            coordinates = prepare_coordinates(pose_results.pose_landmarks.landmark, width, height)
            pose_data.append({
                "time": time,
                "coordinates": coordinates
            })

        out.release()
        cap.release()
        logger.info(f'End to detect pose for {video_name}')

        try:
            clean_temp_saved_video(saved_video_path)
            upload_video_to_minio(video_name, video_name)
        except Exception as e:
            logger.info(f'Can not to upload video {video_name} to minio storage: {e}')
        finally:
            if os.path.isfile(video_name):
                os.remove(video_name)
                logger.info(f"Video {video_name} was successfully deleted")
            else:
                logger.info(f"Video {video_name} not found")

        return {
            "pose_data": pose_data,
            "height": height,
            "width": width,
            "fps": fps,
            "duration": time,
            "video_link": f'{settings.MINIO_USER_URL}/{settings.MINIO_VIDEO_BUCKET_NAME}/{video_name}',
            "body_parts": [member.name for member in BodyParts]
        }
