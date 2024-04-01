from django.http.response import JsonResponse

from rest_framework.decorators import api_view

from detection.pose_detection import detect_pose_coordinates
from detection.validators import validate_pose_estimator_request


@api_view(['POST'])
@validate_pose_estimator_request
def pose_estimate(request):
    video_file = request.FILES['video']

    try:
        pose_data = detect_pose_coordinates(video_file)
        return JsonResponse({'body': pose_data}, safe=False)
    except Exception as exception:
        return JsonResponse({'error': 'Pose detection error: ' + str(exception)}, status=400)
