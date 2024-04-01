import json
import logging
import uuid

from minio import Minio
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from django.conf import settings

logger = logging.getLogger(__name__)
client = Minio(settings.MINIO_URL,
               access_key=settings.MINIO_ACCESS_KEY,
               secret_key=settings.MINIO_SECRET_KEY,
               secure=False)


def upload_video_to_minio(video_path, object_name):
    # if you run via docker compose it's not necessary
    # found = client.bucket_exists(settings.MINIO_VIDEO_BUCKET_NAME)
    # if not found:
    #     client.make_bucket(settings.MINIO_VIDEO_BUCKET_NAME)
    # else:
    #     logger.info(f"Bucket {settings.MINIO_VIDEO_BUCKET_NAME} already exists")

    client.fput_object(settings.MINIO_VIDEO_BUCKET_NAME, object_name, video_path)
    logger.info(
        f"'{video_path}' is successfully uploaded as object '{object_name}' to bucket '{settings.MINIO_VIDEO_BUCKET_NAME}'.")


def temp_save_video(video):
    video_name = f'{uuid.uuid4()}.mp4'
    video_file = video.file
    temp_file_path = default_storage.save(video_name, ContentFile(video_file.read()))
    return default_storage.path(temp_file_path)


def clean_temp_saved_video(video_path):
    default_storage.delete(video_path)
