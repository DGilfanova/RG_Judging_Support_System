from django.urls import path
from detection import views

urlpatterns = [
    path('detect-pose', views.pose_estimate, name='detect_pose'),
]
