# ExoPlayer Image-to-Video Transition Jank

## **Issue Overview**
This repository demonstrates an issue in ExoPlayer where transitioning from an image to a video causes a noticeable jank (~200ms delay). This issue is problematic for real-time video editing applications that require seamless playback.

## **Reproducing the Issue**
1. Clone this repository and run the app.
2. Check the logs to observe the playback delay.
3. Compare the transitions:
    - **Video to Video:** Smooth playback.
    - **Image to Video:** ~200ms jank.
    - **Video to Image:** No jank.
4. **Key Observation:** Removing the audio track from the videos eliminates the jank completely, even on the first playback.

## **Findings**
- The issue occurs when transitioning from an **image** (using `MediaItem.setImageDurationMs()`) to a **video**.
- Using **MediaCodec video renderer prewarming** helps with smooth video playback but does not fix the image-to-video transition issue.
- **Converting images to short videos** using the Media3 Transformer library does **not** resolve the issue.
- **Silencing the audio** in videos removes the jank entirely.

## **Expected Behavior**
- Image-to-video transitions should be as smooth as video-to-video transitions, regardless of whether the videos have an audio track.

## **Possible Cause**
- It appears that ExoPlayer handles image-to-video transitions differently when an audio track is present, causing a delay.

## **Minimal Reproduction**
- This repository contains a minimal example that isolates the problem. To test different scenarios, uncomment the relevant sections in `MainActivity.kt`.
