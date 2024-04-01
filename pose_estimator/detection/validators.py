from django.http import JsonResponse


def validate_pose_estimator_request(fn):
    def decorated(*args, **kwargs):
        request = args[0]
        if 'video' not in request.FILES:
            return JsonResponse({'error': 'No video file provided.'}, status=400)

        video_file = request.FILES['video']
        if not video_file.name.endswith('.mov'):
            return JsonResponse({'error': 'Invalid file format. Only MOV files are supported.'}, status=400)

        return fn(*args, **kwargs)
    return decorated
